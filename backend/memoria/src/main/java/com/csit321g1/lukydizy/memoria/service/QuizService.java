package com.csit321g1.lukydizy.memoria.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.csit321g1.lukydizy.memoria.dto.AttemptResponse;
import com.csit321g1.lukydizy.memoria.dto.AttemptSubmissionRequest;
import com.csit321g1.lukydizy.memoria.dto.QuizQuestion;
import com.csit321g1.lukydizy.memoria.dto.QuizRequest;
import com.csit321g1.lukydizy.memoria.dto.QuizResponse;
import com.csit321g1.lukydizy.memoria.dto.QuizTakeResponse;
import com.csit321g1.lukydizy.memoria.entity.QuizEntity;
import com.csit321g1.lukydizy.memoria.entity.QuizReviewerEntity;
import com.csit321g1.lukydizy.memoria.entity.ReviewerEntity;
import com.csit321g1.lukydizy.memoria.exception.InvalidRequestException;
import com.csit321g1.lukydizy.memoria.exception.ResourceInUseException;
import com.csit321g1.lukydizy.memoria.exception.ResourceNotFoundException;
import com.csit321g1.lukydizy.memoria.repository.QuizAttemptRepository;
import com.csit321g1.lukydizy.memoria.repository.QuizRepository;
import com.csit321g1.lukydizy.memoria.repository.QuizReviewerRepository;
import com.csit321g1.lukydizy.memoria.repository.ReviewerRepository;
import com.csit321g1.lukydizy.memoria.repository.UserRepository;

import tools.jackson.databind.ObjectMapper;

/**
 * Create, read, update, and delete quizzes for the logged-in student.
 * Every lookup is filtered by owner, so another student's quiz returns 404.
 */
@Service
public class QuizService {

    private final QuizRepository quizzes;
    private final QuizReviewerRepository quizReviewers;
    private final ReviewerRepository reviewers;
    private final QuizAttemptRepository attempts;
    private final QuizAttemptService attemptService;
    private final UserRepository users;
    // Spring Boot's JSON mapper (Jackson 3), used to store questions as text.
    private final ObjectMapper mapper;

    public QuizService(
        QuizRepository quizzes,
        QuizReviewerRepository quizReviewers,
        ReviewerRepository reviewers,
        QuizAttemptRepository attempts,
        QuizAttemptService attemptService,
        UserRepository users,
        ObjectMapper mapper
    ) {
        this.quizzes = quizzes;
        this.quizReviewers = quizReviewers;
        this.reviewers = reviewers;
        this.attempts = attempts;
        this.attemptService = attemptService;
        this.users = users;
        this.mapper = mapper;
    }

    /** Saves the quiz and its reviewer links in one transaction: all or nothing. */
    @Transactional
    public QuizResponse create(QuizRequest request) {
        String userId = CurrentUser.id();
        List<ReviewerEntity> selected = findOwnedReviewers(request.reviewerIds(), userId);
        QuizEntity quiz = quizzes.save(new QuizEntity(
            users.getReferenceById(userId),
            request.title().trim(),
            request.mode(),
            toJson(request.questions()),
            LocalDateTime.now(ZoneOffset.UTC)
        ));
        selected.forEach(reviewer -> quizReviewers.save(new QuizReviewerEntity(quiz, reviewer)));
        return toResponse(quiz);
    }

    /** Lists the student's quizzes, newest first. */
    @Transactional(readOnly = true)
    public List<QuizResponse> list() {
        return quizzes.findByUser_IdOrderByCreatedAtDesc(CurrentUser.id()).stream()
            .map(this::toResponse)
            .toList();
    }

    /** One quiz with its questions and answers (owner only). */
    @Transactional(readOnly = true)
    public QuizResponse get(String id) {
        return toResponse(findOwned(id, CurrentUser.id()));
    }

    /** Returns the owner's student-facing quiz view without answer keys. */
    @Transactional(readOnly = true)
    public QuizTakeResponse take(String id) {
        QuizEntity quiz = findOwned(id, CurrentUser.id());
        List<QuizTakeResponse.Question> questions = fromJson(quiz.getQuestions()).stream()
            .map(question -> new QuizTakeResponse.Question(question.text(), question.choices()))
            .toList();
        return new QuizTakeResponse(quiz.getTitle(), questions);
    }

    /** Grades and saves an attempt atomically; omitted questions count as incorrect. */
    @Transactional
    public AttemptResponse submit(String id, AttemptSubmissionRequest request) {
        QuizEntity quiz = findOwned(id, CurrentUser.id());
        List<QuizQuestion> questions = fromJson(quiz.getQuestions());
        if (questions.isEmpty()) {
            throw new InvalidRequestException("This quiz has no questions to grade.");
        }
        if (request == null || request.answers() == null) {
            throw new InvalidRequestException("Answers are required. Use an empty list for an unanswered quiz.");
        }

        Set<Integer> answeredQuestions = new LinkedHashSet<>();
        int score = 0;
        for (AttemptSubmissionRequest.AnswerSelection answer : request.answers()) {
            int questionIndex = answer.questionIndex();
            int choiceIndex = answer.choiceIndex();
            if (questionIndex >= questions.size()) {
                throw new InvalidRequestException("Question index " + questionIndex + " does not exist.");
            }
            if (choiceIndex >= questions.get(questionIndex).choices().size()) {
                throw new InvalidRequestException(
                    "Choice index " + choiceIndex + " does not exist for question " + questionIndex + "."
                );
            }
            if (!answeredQuestions.add(questionIndex)) {
                throw new InvalidRequestException("Question " + questionIndex + " has more than one answer.");
            }
            if (questions.get(questionIndex).correctIndex() == choiceIndex) {
                score++;
            }
        }

        return attemptService.create(id, score, questions.size());
    }

    /**
     * Updates title, questions, and reviewer links.
     * Once the quiz has attempts, only the title and reviewers may change.
     */
    @Transactional
    public QuizResponse update(String id, QuizRequest request) {
        String userId = CurrentUser.id();
        QuizEntity quiz = findOwned(id, userId);
        List<ReviewerEntity> selected = findOwnedReviewers(request.reviewerIds(), userId);

        boolean questionsChanged = !request.questions().equals(fromJson(quiz.getQuestions()));
        if (questionsChanged && attempts.existsByQuiz_Id(id)) {
            throw new ResourceInUseException(
                "This quiz already has attempts, so its questions can't be changed. You can still edit the title."
            );
        }
        quiz.setTitle(request.title().trim());
        quiz.setQuestions(toJson(request.questions()));

        // Keep links that are still selected, remove the rest, add the new ones.
        // Deleting everything and re-adding fails: Hibernate inserts before it deletes, causing a duplicate key.
        Set<String> selectedIds = new LinkedHashSet<>(request.reviewerIds());
        Set<String> keptIds = new LinkedHashSet<>();
        List<QuizReviewerEntity> toRemove = new ArrayList<>();
        for (QuizReviewerEntity link : quizReviewers.findByQuiz_Id(id)) {
            String reviewerId = link.getReviewer().getId();
            if (selectedIds.contains(reviewerId)) {
                keptIds.add(reviewerId);
            } else {
                toRemove.add(link);
            }
        }
        quizReviewers.deleteAll(toRemove);
        selected.stream()
            .filter(reviewer -> !keptIds.contains(reviewer.getId()))
            .forEach(reviewer -> quizReviewers.save(new QuizReviewerEntity(quiz, reviewer)));
        return toResponse(quiz);
    }

    /** Deletes a quiz and its reviewer links. Blocked once the quiz has attempts. */
    @Transactional
    public void delete(String id) {
        QuizEntity quiz = findOwned(id, CurrentUser.id());
        if (attempts.existsByQuiz_Id(id)) {
            throw new ResourceInUseException("This quiz has attempts and can't be deleted.");
        }
        quizReviewers.deleteAll(quizReviewers.findByQuiz_Id(id));
        quizzes.delete(quiz);
    }

    /** Loads a quiz only if the user owns it; otherwise 404 (we don't reveal that it exists). */
    private QuizEntity findOwned(String id, String userId) {
        return quizzes.findByIdAndUser_Id(id, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Quiz not found."));
    }

    /** Loads the selected reviewers, rejecting any that are missing or belong to someone else. */
    private List<ReviewerEntity> findOwnedReviewers(List<String> reviewerIds, String userId) {
        Set<String> uniqueIds = new LinkedHashSet<>(reviewerIds);
        List<ReviewerEntity> found = reviewers.findByIdInAndUser_Id(uniqueIds, userId);
        if (found.size() != uniqueIds.size()) {
            Set<String> foundIds = new LinkedHashSet<>();
            found.forEach(reviewer -> foundIds.add(reviewer.getId()));
            List<String> invalidIds = uniqueIds.stream().filter(rid -> !foundIds.contains(rid)).toList();
            throw new InvalidRequestException(
                "These reviewers do not exist or are not yours: " + String.join(", ", invalidIds)
            );
        }
        return found;
    }

    /** Questions list -> JSON text for the database. */
    private String toJson(List<QuizQuestion> questions) {
        return mapper.writeValueAsString(questions);
    }

    /** JSON text from the database -> questions list. Older rows may be null. */
    private List<QuizQuestion> fromJson(String json) {
        return json == null ? List.of() : List.of(mapper.readValue(json, QuizQuestion[].class));
    }

    /** Builds the API response, including linked reviewers and the read-only flag. */
    private QuizResponse toResponse(QuizEntity quiz) {
        List<QuizResponse.LinkedReviewer> linked = quizReviewers.findByQuiz_Id(quiz.getId()).stream()
            .map(link -> new QuizResponse.LinkedReviewer(link.getReviewer().getId(), link.getReviewer().getTitle()))
            .toList();
        return new QuizResponse(
            quiz.getId(),
            quiz.getTitle(),
            quiz.getMode(),
            fromJson(quiz.getQuestions()),
            quiz.getCreatedAt(),
            attempts.existsByQuiz_Id(quiz.getId()),
            linked
        );
    }
}
