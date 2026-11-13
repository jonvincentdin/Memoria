package com.csit321g1.lukydizy.memoria.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.csit321g1.lukydizy.memoria.entity.QuizAttemptEntity;
import com.csit321g1.lukydizy.memoria.dto.QuizAttemptDto.AttemptResponse;
import com.csit321g1.lukydizy.memoria.entity.QuizEntity;
import com.csit321g1.lukydizy.memoria.exception.InvalidRequestException;
import com.csit321g1.lukydizy.memoria.exception.ResourceNotFoundException;
import com.csit321g1.lukydizy.memoria.repository.QuizAttemptRepository;
import com.csit321g1.lukydizy.memoria.repository.QuizRepository;
import com.csit321g1.lukydizy.memoria.repository.UserRepository;

@Service
public class QuizAttemptService {

    private final QuizAttemptRepository attempts;
    private final QuizRepository quizzes;
    private final UserRepository users;

    /** Creates the service with its repositories. */
    public QuizAttemptService(QuizAttemptRepository attempts, QuizRepository quizzes, UserRepository users) {
        this.attempts = attempts;
        this.quizzes = quizzes;
        this.users = users;
    }

    /**
     * Saves a graded attempt for the logged-in user. Called by BE-06 after grading.
     * The quiz must belong to the same user; the attempt is stored as COMPLETED with the current time.
     */
    @Transactional
    public AttemptResponse create(String quizId, int score, int totalQuestions) {
        // Owner comes from the logged-in session, never from the request body.
        String userId = CurrentUser.id();
        // Reject impossible results, such as a score higher than the number of questions.
        if (totalQuestions < 1 || score < 0 || score > totalQuestions) {
            throw new InvalidRequestException("Score must be between 0 and the total number of questions.");
        }
        // The quiz must exist and belong to the same student; otherwise 404.
        QuizEntity quiz = quizzes.findByIdAndUser_Id(quizId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Quiz not found."));

        // A graded attempt is saved as COMPLETED with the current UTC time.
        QuizAttemptEntity attempt = new QuizAttemptEntity(
            users.getReferenceById(userId),
            quiz,
            score,
            totalQuestions,
            QuizAttemptEntity.Status.COMPLETED,
            LocalDateTime.now(ZoneOffset.UTC)
        );
        // INSERT into quiz_attempts, then convert the saved row to the JSON response.
        return AttemptResponse.from(attempts.save(attempt));
    }

    /** Lists the logged-in user's attempts, newest first, optionally for one quiz. */
    @Transactional(readOnly = true)
    public List<AttemptResponse> list(String quizId) {
        String userId = CurrentUser.id();
        // No quizId: all of the user's attempts. With quizId: only that quiz's attempts.
        List<QuizAttemptEntity> found = (quizId == null || quizId.isBlank())
            ? attempts.findByUser_IdOrderByCompletedAtDesc(userId)
            : attempts.findByUser_IdAndQuiz_IdOrderByCompletedAtDesc(userId, quizId);
        return found.stream().map(AttemptResponse::from).toList();
    }

    /** Returns one of the logged-in user's attempts. */
    @Transactional(readOnly = true)
    public AttemptResponse get(String id) {
        return AttemptResponse.from(findOwned(id));
    }

    /** Deletes one of the logged-in user's attempts. */
    @Transactional
    public void delete(String id) {
        // findOwned throws 404 first if the attempt is missing or belongs to someone else.
        attempts.delete(findOwned(id));
    }

    /** Finds an attempt owned by the logged-in user; another user's attempt is treated as missing. */
    private QuizAttemptEntity findOwned(String id) {
        // Searching by ID AND owner means another student's attempt is never found.
        return attempts.findByIdAndUser_Id(id, CurrentUser.id())
            .orElseThrow(() -> new ResourceNotFoundException("Attempt not found."));
    }
}
