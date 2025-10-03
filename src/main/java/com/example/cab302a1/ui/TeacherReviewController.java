package com.example.cab302a1.ui;

import com.example.cab302a1.dao.ReviewDao;
import com.example.cab302a1.dao.UserDao;
import com.example.cab302a1.model.QuizReview;
import com.example.cab302a1.model.Student;
import com.example.cab302a1.model.User;
import com.example.cab302a1.result.QuizResultController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for the Teacher Review Page.
 * Displays a list of student quiz attempts and provides actions for review.
 */
public class TeacherReviewController implements Initializable, ReviewPageController {

    // FXML Fields
    @FXML public TableView<QuizReview> quizTable;
    @FXML public TableColumn<QuizReview, String> quizNameCol;
    @FXML public TableColumn<QuizReview, String> scoreCol;
    @FXML public TableColumn<QuizReview, Void> resultCol;
    @FXML public Button assignReviewBtn;

    // Student List FXML Container and Label
    @FXML public VBox studentListContainer;
    @FXML public Label studentNameLabel;

    private final ObservableList<QuizReview> reviewData = FXCollections.observableArrayList();
    private Stage stage;

    private final ReviewDao reviewDao = new ReviewDao();
    private final UserDao userDao = new UserDao();

    private int currentSelectedStudentId = -1; // Tracks the ID of the student whose quizzes are displayed

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        setupActionButtons();
        loadStudentList(); // Load students dynamically

        // Initial setup text
        studentNameLabel.setText("Student Name: (Please select a student)");
    }

    /**
     * Loads the list of students from the database and populates the sidebar with styled buttons.
     */
    private void loadStudentList() {
        if (studentListContainer == null) return;
        studentListContainer.getChildren().clear();

        // Fetch list of all Users with role 'Student'
        List<User> students = userDao.getAllStudents();

        for (User user : students) {
            // Use the Student Object Fields (by safe casting)
            if (!(user instanceof Student student)) continue;

            Button studentBtn = new Button(student.getUsername());

            // Apply the CSS style: student-list-item
            studentBtn.getStyleClass().add("action-button");
            studentBtn.setMaxWidth(Double.MAX_VALUE);

            studentBtn.setOnAction(e -> {
                // Set the label and load the quiz data for the selected student
                System.out.println("Teacher selected student: " + student.getUsername() + " (ID: " + student.getUser_id() + ")");
                studentNameLabel.setText("Student Name: " + student.getUsername());
                currentSelectedStudentId = student.getUser_id();
                loadReviewData(); // Reloads table with quizzes for this student
            });

            studentListContainer.getChildren().add(studentBtn);
        }
    }


    private void setupActionButtons() {
        if (assignReviewBtn != null) {
            assignReviewBtn.setOnAction(e -> {
                QuizReview selectedQuiz = quizTable.getSelectionModel().getSelectedItem();

                if (selectedQuiz == null || currentSelectedStudentId == -1) {
                    // Provide feedback if no selection is made
                    Alert alert = new Alert(Alert.AlertType.WARNING,
                            "Please select a quiz attempt from the table to assign a review.");
                    alert.showAndWait();
                    return;
                }

                // 1. Launch Input Dialog to get feedback
                TextInputDialog dialog = new TextInputDialog(selectedQuiz.getFeedback() != null ? selectedQuiz.getFeedback() : "");
                dialog.setTitle("Assign Review and Feedback");
                dialog.setHeaderText("Provide feedback for: " + selectedQuiz.getQuizName());
                dialog.setContentText("Enter feedback text:");

                dialog.showAndWait().ifPresent(feedbackText -> {

                    // 2. Call DAO to update the database
                    int attemptId = selectedQuiz.getAttemptId();
                    boolean success = reviewDao.assignFeedback(attemptId, feedbackText);

                    if (success) {
                        // 3. Update the UI and inform the user
                        Alert successAlert = new Alert(Alert.AlertType.INFORMATION, "Feedback successfully assigned.");
                        successAlert.showAndWait();

                        // Reload the data to update the local QuizReview object with the new feedback
                        loadReviewData();
                    } else {
                        Alert errorAlert = new Alert(Alert.AlertType.ERROR, "Failed to assign feedback. Please check database connection.");
                        errorAlert.showAndWait();
                    }
                });
            });
        }
    }

    @Override
    public void setStage(Stage stage) {

    }

    @Override
    public void loadReviewData() {
        reviewData.clear();

        if (currentSelectedStudentId != -1) {
            try {
                // Load quizzes for the currently selected student
                reviewData.addAll(reviewDao.getAllAttemptsById(currentSelectedStudentId));
                System.out.println("Loaded " + reviewData.size() + " attempts for student ID: " + currentSelectedStudentId);
            } catch (Exception e) {
                System.err.println("Error fetching quiz attempts for student ID " + currentSelectedStudentId + ": " + e.getMessage());
            }
        }

        if (reviewData.isEmpty()) {
            // Show placeholder data if no attempts are found for the selected student
            System.out.println("No quiz attempts found or selected student ID is invalid.");
            reviewData.add(new QuizReview("No attempts available", 0, 0, "Select a student from the list.", -1));
        }

        if (quizTable != null) {
            quizTable.setItems(reviewData);
        }
    }

    private void setupTableColumns() {

        if (quizTable == null) return;

        quizTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        // ===============================================
        // 1. Setup Quiz Name Column (Styled Link)
        // ===============================================

        quizNameCol.setCellValueFactory(data -> data.getValue().quizNameProperty());

        // Renders the quiz name as a styled button (using view-answer-button style)
        quizNameCol.setCellFactory(col -> new TableCell<QuizReview, String>() {
            private final Button btn = new Button();
            {
                // Ensure correct CSS is applied here
                btn.getStyleClass().add("action-button");
                btn.setPrefWidth(120.0);

                btn.setOnAction(e -> {
                    QuizReview item = getTableView().getItems().get(getIndex());
                    System.out.println("Viewing Quiz Attempt ID: " + item.getAttemptId());
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    btn.setText(item);
                    setGraphic(btn);
                }
            }
        });

        // ===============================================
        // 2. Setup Score Column
        // ===============================================

        if (scoreCol != null) {
            scoreCol.setCellValueFactory(data -> data.getValue().scoreSummaryProperty());
        }

        // ===============================================
        // 3. Setup View Result Column (Button)
        // ===============================================
        if (resultCol != null) {
            resultCol.setCellFactory(col -> new TableCell<QuizReview, Void>() {

                private final Button btn = new Button("View Result");

                {
                    btn.getStyleClass().add("action-button");
                    btn.setPrefWidth(120.0);

                    btn.setOnAction(e -> {
                        QuizReview item = getTableView().getItems().get(getIndex());
                        System.out.println("Showing full result for Attempt ID: " + item.getAttemptId());

                        try {
                            Stage currentStage = (Stage) quizTable.getScene().getWindow();
                            // Use the factory method in QuizResultController to show the result
                            QuizResultController.showQuizResultFromDatabase(currentStage, item.getQuizId(), currentSelectedStudentId);
                        } catch (Exception ex) {
                            System.err.println("Error opening quiz result page: " + ex.getMessage());
                            ex.printStackTrace();
                            Alert alert = new Alert(Alert.AlertType.ERROR,
                                    "Unable to load quiz result page. Please try again.");
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
    }

    private void handleExit(javafx.event.ActionEvent event) {
        Stage currentStage = (Stage)((Node)event.getSource()).getScene().getWindow();
        currentStage.close();
    }
}