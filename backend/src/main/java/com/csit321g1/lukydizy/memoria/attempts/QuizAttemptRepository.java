package com.csit321g1.lukydizy.memoria.attempts;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizAttemptRepository extends JpaRepository<QuizAttemptEntity, String> {

    /** Finds an attempt only if it belongs to the given user. */
    Optional<QuizAttemptEntity> findByIdAndUser_Id(String id, String userId);

    /** Lists a user's attempts, newest first. */
    List<QuizAttemptEntity> findByUser_IdOrderByCompletedAtDesc(String userId);

    /** Lists a user's attempts for one quiz, newest first. */
    List<QuizAttemptEntity> findByUser_IdAndQuiz_IdOrderByCompletedAtDesc(String userId, String quizId);
    
    /** True once anyone has attempted the quiz; used to lock its questions and block deletion. */
    boolean existsByQuiz_Id(String quizId);

}
