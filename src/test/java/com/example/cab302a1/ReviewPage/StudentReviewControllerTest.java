package com.example.cab302a1.ui;

import com.example.cab302a1.dao.ReviewDao;
import com.example.cab302a1.model.QuizReview;
import com.example.cab302a1.model.User;
import com.example.cab302a1.util.Session;
import javafx.application.Platform;
import javafx.scene.control.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import java.lang.reflect.Field;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StudentReviewControllerTest {

    private StudentReviewController controller;
    private ReviewDao mockReviewDao;

    // Test data constants
    private static final int TEST_STUDENT_ID = 5001;
    private static final int MOCK_QUIZ_ID_1 = 101;
    private static final int MOCK_QUIZ_ID_2 = 102;

    private List<QuizReview> mockQuizzes;
    private User testUser;

    @BeforeAll
    static void initToolkit() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Already started
        }
    }

    /**
     * Utility method to inject mocks into private fields using Reflection.
     */
    private static void injectMock(Object target, String fieldName, Object mock) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, mock);
    }

    @BeforeEach
    void setUp() throws Exception {
        controller = new StudentReviewController();
        mockReviewDao = mock(ReviewDao.class);

        // 1. Setup Mock Data
        testUser = new User(TEST_STUDENT_ID, "Alice", "alice@test.com", "student", null);

        mockQuizzes = List.of(
                // Constructor: attemptId, quizId, userId, quizName, score, total, feedback
                new QuizReview(1, MOCK_QUIZ_ID_1, TEST_STUDENT_ID, "Intro to Java", 15, 20, "Excellent work."),
                new QuizReview(2, MOCK_QUIZ_ID_2, TEST_STUDENT_ID, "Adv Databases", 8, 10, null)
        );

        // Mock DAO: Return quiz data ONLY for the test student's ID
        when(mockReviewDao.getAllAttemptsById(TEST_STUDENT_ID)).thenReturn(mockQuizzes);
        when(mockReviewDao.getAllAttemptsById(anyInt())).thenReturn(List.of()); // Fail safe

        // 2. Inject Mocks into the Controller
        injectMock(controller, "reviewDao", mockReviewDao);


        // 3. Inject ALL FXML-mapped UI controls
        controller.studentQuizTable = new TableView<>();
        controller.quizNameCol = new TableColumn<>("Quiz");
        controller.scoreCol = new TableColumn<>("Score");

        // Match the FXML field names and types
        controller.feedbackCol = new TableColumn<>("View Feedback");
        controller.resultCol = new TableColumn<>("View Result");

        controller.studentQuizTable.getColumns().addAll(
                controller.quizNameCol,
                controller.scoreCol,
                controller.feedbackCol,
                controller.resultCol
        );

        // Use Mockito to mock the static Session.getCurrentUser() call
        // This is necessary because the controller calls Session.getCurrentUser() during initialize()
        try (MockedStatic<Session> sessionMock = mockStatic(Session.class)) {
            sessionMock.when(Session::getCurrentUser).thenReturn(testUser);

            // 4. Call initialize() which triggers data load
            controller.initialize(null, null);
        }
    }

    @Test
    void testTablePopulatedAfterInitialization() {
        assertEquals(2, controller.studentQuizTable.getItems().size(),
                "Quiz table should contain 2 mock quiz reviews after initialization.");

        QuizReview result0 = controller.studentQuizTable.getItems().get(0);
        assertEquals("Intro to Java", result0.getQuizName());
    }

    @Test
    void testFeedbackColSetupShowsButton() {
        // This test ensures the cell factory runs and creates a button

        // We must run the UI update logic on the JavaFX thread
        Platform.runLater(() -> {
            // Get the first item
            controller.studentQuizTable.getSelectionModel().select(0);

            // Get the cell object for the feedback column of the first row
            TableCell<?, ?> cell = (TableCell<?, ?>) controller.feedbackCol.getCellFactory().call(controller.feedbackCol);
            cell.updateIndex(0); // Update the index to force cell population

            assertNotNull(cell.getGraphic(), "Feedback column cell should contain a graphic (Button).");
            assertTrue(cell.getGraphic() instanceof Button, "Graphic should be a Button.");
            assertEquals("View Feedback", ((Button)cell.getGraphic()).getText());
        });

        // Wait for the Platform.runLater task to complete (not strictly necessary for this simple test, but good practice)
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    void testResultColSetupShowsButton() {
        // This test ensures the cell factory runs and creates a button for results

        // We must run the UI update logic on the JavaFX thread
        Platform.runLater(() -> {
            controller.studentQuizTable.getSelectionModel().select(0);

            // Get the cell object for the result column of the first row
            TableCell<?, ?> cell = (TableCell<?, ?>) controller.resultCol.getCellFactory().call(controller.resultCol);
            cell.updateIndex(0);

            assertNotNull(cell.getGraphic(), "Result column cell should contain a graphic (Button).");
            assertTrue(cell.getGraphic() instanceof Button, "Graphic should be a Button.");
            assertEquals("View Result", ((Button)cell.getGraphic()).getText());
        });

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    void testFirstRowScoreSummary() {
        QuizReview result = controller.studentQuizTable.getItems().get(0);
        assertEquals("15/20", result.scoreSummaryProperty().get(), "First row score summary should match mock data.");
    }
}