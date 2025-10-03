package com.example.cab302a1.ui;

import com.example.cab302a1.result.QuizResultData;
import com.example.cab302a1.dao.ReviewDao;
import com.example.cab302a1.model.QuizReview;
import com.example.cab302a1.model.User;
import com.example.cab302a1.result.QuizResultController; // New Import
import com.example.cab302a1.result.QuizResultService; // New Import (for exception handling)
import com.example.cab302a1.util.Session;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the Student Review Page.
 * Displays all quiz attempts and feedback for the currently logged-in student.
 */
public class StudentReviewController implements Initializable, ReviewPageController {

    // FXML Fields (Existing)
    @FXML public TableView<QuizReview> studentQuizTable;
    @FXML public TableColumn<QuizReview, String> quizNameCol;
    @FXML public TableColumn<QuizReview, String> scoreCol;
    @FXML public TableColumn<QuizReview, Void> feedbackCol;
    @FXML public TableColumn<QuizReview, Void> resultCol;

    // Sidebar Buttons (Used in testing to prevent NPE, actual logic depends on your FXML)
    @FXML public Button dashboardBtn;
    @FXML public Button reviewBtn;
    @FXML public Button timetableBtn;
    @FXML public Button exitBtn;

    private final ObservableList<QuizReview> reviewData = FXCollections.observableArrayList();
    private final ReviewDao reviewDao = new ReviewDao();

    // Stage field (if needed by interface)
    private Stage stage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        loadReviewData(); // Load data immediately upon entering the scene
    }

    /**
     * Configures the TableView columns, including styling the action buttons.
     */
    private void setupTableColumns() {
        if (studentQuizTable == null) return;

        studentQuizTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        // 1. Quiz Name Column & 2. Score Column (No change)
        quizNameCol.setCellValueFactory(data -> data.getValue().quizNameProperty());
        scoreCol.setCellValueFactory(data -> data.getValue().scoreSummaryProperty());

        // 3. "View Feedback" Column (Button)
        feedbackCol.setText("View Feedback");
        feedbackCol.setCellFactory(col -> new TableCell<QuizReview, Void>() {
            private final Button btn = new Button("View Feedback");

            {
                btn.getStyleClass().add("action-button");
                btn.setPrefWidth(120.0);

                btn.setOnAction(e -> {
                    QuizReview item = getTableView().getItems().get(getIndex());

                    // Logic to display the teacher's text feedback
                    String feedback = item.getFeedback();

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Teacher Feedback for " + item.getQuizName());
                    alert.setHeaderText("Feedback Status: " + (feedback != null ? "Reviewed" : "Pending Review"));
                    alert.setContentText(feedback != null && !feedback.trim().isEmpty()
                            ? feedback : "No specific feedback has been assigned by the teacher yet.");

                    alert.showAndWait();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        // 4. "View Result" Column (Button)
        resultCol.setText("View Result");
        resultCol.setCellFactory(col -> new TableCell<QuizReview, Void>() {
            private final Button btn = new Button("View Result");

            {
                btn.getStyleClass().add("action-button");
                btn.setPrefWidth(120.0);

                btn.setOnAction(e -> {
                    QuizReview item = getTableView().getItems().get(getIndex());

                    // Navigation Logic to the Quiz Result Page
                    try {
                        int quizId = item.getQuizId();

                        if (quizId <= 0) {
                            throw new IllegalArgumentException("Invalid Quiz ID for result viewing. Data integrity issue.");
                        }

                        Stage stage = (Stage) btn.getScene().getWindow();

                        // Call the static method for the logged-in user
                        QuizResultController.showQuizResultFromDatabaseForCurrentUser(stage, quizId);

                    } catch (QuizResultService.QuizResultException | IOException ex) {
                        System.err.println("Error viewing quiz result: " + ex.getMessage());
                        ex.printStackTrace();
                        Alert alert = new Alert(Alert.AlertType.ERROR,
                                "Could not load quiz result. Error: " + ex.getMessage());
                        alert.showAndWait();
                    } catch (IllegalArgumentException ex) {
                        Alert alert = new Alert(Alert.AlertType.WARNING, ex.getMessage());
                        alert.showAndWait();
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
    }

    @Override
    public void setStage(Stage stage) {

    }

    /**
     * Loads quiz attempt data for the currently logged-in student.
     */
    @Override
    public void loadReviewData() {
        reviewData.clear();

        // Get the ID of the logged-in user from the corrected Session utility
        User currentUser = Session.getCurrentUser();

        if (currentUser != null) {
            int studentId = currentUser.getUser_id();

            try {
                // Load quizzes specifically for the logged-in student ID
                reviewData.addAll(reviewDao.getAllAttemptsById(studentId));
                System.out.println("Student ID " + studentId + " loaded " + reviewData.size() + " attempts.");
            } catch (Exception e) {
                System.err.println("Error fetching quiz attempts for current user: " + e.getMessage());
            }
        } else {
            System.err.println("Load data failed: No current user found in Session.");
        }

        if (reviewData.isEmpty() && studentQuizTable != null) {
            studentQuizTable.setPlaceholder(new Label("You have not completed any quizzes yet."));
        }

        if (studentQuizTable != null) {
            studentQuizTable.setItems(reviewData);
        }
    }
}