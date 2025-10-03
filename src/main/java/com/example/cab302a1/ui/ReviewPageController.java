package com.example.cab302a1.ui;

import javafx.stage.Stage;

/**
 * Interface defining a common contract for all review page controllers
 * (Student Review, Teacher Review).
 * This enforces consistency in setup and data loading.
 */
public interface ReviewPageController {

    /**
     * Sets the JavaFX Stage associated with this controller's scene.
     * Useful for managing window operations like closing or scene transitions.
     * @param stage The primary Stage of the application window.
     */
    void setStage(Stage stage);

    /**
     * Loads or updates the primary data displayed on the review page.
     * (e.g., fetching quiz results from a database or a service).
     */
    void loadReviewData();
}