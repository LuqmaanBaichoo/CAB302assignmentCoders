package com.example.cab302a1.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Data model for displaying quiz review information in TableViews.
 */
public class QuizReview {
    private final int attemptId;
    private final int quizId;
    private final int userId;

    private final StringProperty quizName;
    private final int score; // Stored as a primitive int
    private final int total; // Stored as a primitive int
    private final String feedback; // Stored as a primitive String

    /**
     * Primary constructor for real database rows.
     * Used by ReviewDao to load actual quiz attempts.
     */
    public QuizReview(int attemptId, int quizId, int userId,
                      String quizName, int score, int total, String feedback) {
        this.attemptId = attemptId;
        this.quizId = quizId;
        this.userId = userId;
        this.quizName = new SimpleStringProperty(quizName);
        this.score = score;
        this.total = total;
        this.feedback = feedback;
    }

    /**
     * Constructor for mock/demo data (no attemptId or userId).
     * Prevents confusion between quizId and attemptId.
     */
    public QuizReview(String quizName, int score, int total, String feedback, int quizId) {
        this(0, quizId, 0, quizName, score, total, feedback);
    }

    // --- Getters for Navigation ---

    public int getQuizId() {
        return quizId;
    }

    public int getUserId() {
        return userId;
    }

    public int getAttemptId() {
        return attemptId;
    }

    // --- Existing Getters and Properties ---

    public String getQuizName() {
        return quizName.get();
    }

    public StringProperty quizNameProperty() {
        return quizName;
    }

    // Returns score in "X/Y" format for the TableView
    public String getScoreSummary() {
        return score + "/" + total;
    }

    public StringProperty scoreSummaryProperty() {
        return new SimpleStringProperty(getScoreSummary());
    }

    public int getScore() {
        return score;
    }

    public int getTotal() {
        return total;
    }

    public String getFeedback() {
        return feedback;
    }
}
