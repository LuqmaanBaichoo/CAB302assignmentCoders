package com.example.cab302a1.ReviewPage;

import com.example.cab302a1.dao.ReviewDao;
import com.example.cab302a1.dao.UserDao;
import com.example.cab302a1.model.QuizReview;
import com.example.cab302a1.model.Student;
import com.example.cab302a1.model.User;
import com.example.cab302a1.ui.TeacherReviewController;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TeacherReviewControllerTest {

    private TeacherReviewController controller;
    private UserDao mockUserDao;
    private ReviewDao mockReviewDao;

    // Test data
    private static final int TEST_STUDENT_ID = 5001;
    private Student testStudent;
    private List<QuizReview> mockQuizzes;

    @BeforeAll
    static void initToolkit() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Already started
        }
    }

    /**
     * Utility method to inject mocks into private final fields.
     */
    private static void injectMock(Object target, String fieldName, Object mock) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, mock);
    }

    @BeforeEach
    void setUp() throws Exception {
        controller = new TeacherReviewController();

        // 1. Setup Mock Data
        // IMPORTANT: Assuming Student class has getUserID() method (capitalized)
        testStudent = new Student(TEST_STUDENT_ID, "Alice Smith", "alice@test.com", "Student", new Timestamp(System.currentTimeMillis()));

        // Mock data to be returned when Alice is selected
        mockQuizzes = List.of(
                new QuizReview("Intro to Java", 15, 20, "Good attempt.", 101),
                new QuizReview("Adv Databases", 8, 10, null, 102)
        );

        // 2. Setup DAO Mocks (Mockito)
        mockUserDao = mock(UserDao.class);
        mockReviewDao = mock(ReviewDao.class);

        // Mock UserDAO: Return one student for the sidebar list
        when(mockUserDao.getAllStudents()).thenReturn(List.of(testStudent));

        // Mock ReviewDAO: Return quiz data ONLY when the test student's ID is requested
        when(mockReviewDao.getAllAttemptsById(TEST_STUDENT_ID)).thenReturn(mockQuizzes);
        when(mockReviewDao.getAllAttemptsById(Mockito.anyInt())).thenReturn(List.of()); // Fail safe

        // 3. Inject Mocks into the Controller (Uses reflection for private final fields)
        injectMock(controller, "userDao", mockUserDao);
        injectMock(controller, "reviewDao", mockReviewDao);


        // 4. Inject ALL FXML-mapped UI controls
        controller.assignReviewBtn = new Button();
        controller.studentListContainer = new VBox();
        controller.studentNameLabel = new Label();
        controller.quizTable = new TableView<>();
        controller.quizNameCol = new TableColumn<>("Quiz");
        controller.scoreCol = new TableColumn<>("Score");
        controller.resultCol = new TableColumn<>("View Result");

        controller.quizTable.getColumns().addAll(
                controller.quizNameCol,
                controller.scoreCol,
                controller.resultCol
        );

        // 5. Call initialize(). This populates studentListContainer but leaves quizTable empty.
        controller.initialize(null, null);
    }

    /**
     * Utility to fire the button click and run the Platform task synchronously.
     * This is needed because button actions are handled on the JavaFX thread.
     */
    private void selectStudent() {
        // Fire the click event on the first student button
        Button studentBtn = (Button) controller.studentListContainer.getChildren().get(0);

        // Run the action on the JavaFX thread and wait for it to complete
        Platform.runLater(studentBtn::fire);

        // Wait for the Platform.runLater task to complete.
        // This is a common hack in non-FX test runners.
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }


    @Test
    void testStudentListPopulated() {
        // Verify the student list sidebar is populated using the mocked UserDao
        assertFalse(controller.studentListContainer.getChildren().isEmpty(),
                "Student list container should be populated after initialization.");
        assertEquals(1, controller.studentListContainer.getChildren().size());

        Button studentBtn = (Button) controller.studentListContainer.getChildren().get(0);
        assertEquals(testStudent.getUsername(), studentBtn.getText());
        assertEquals("student-list-item", studentBtn.getStyleClass().get(0), "CSS class should be applied.");
    }

    @Test
    void testQuizTablePopulatedAfterStudentSelection() {
        // 1. Initially, the table should be empty
        assertTrue(controller.quizTable.getItems().isEmpty() ||
                        controller.quizTable.getItems().get(0).getQuizName().contains("No attempts"),
                "Quiz table should be empty before student selection.");

        // 2. Simulate student selection
        selectStudent();

        // 3. Verify data loaded
        assertEquals(2, controller.quizTable.getItems().size(),
                "Quiz table should now contain 2 mock quiz reviews after student selection.");

        QuizReview result0 = controller.quizTable.getItems().get(0);
        assertEquals("Intro to Java", result0.getQuizName());
    }

    @Test
    void testFirstRowScoreSummary() {
        // 1. Simulate student selection
        selectStudent();

        // 2. Get the first row
        QuizReview result = controller.quizTable.getItems().get(0);

        // 3. Verify the score summary property (15/20)
        assertEquals("15/20", result.scoreSummaryProperty().get(), "First row score summary should match mock data.");
    }
}