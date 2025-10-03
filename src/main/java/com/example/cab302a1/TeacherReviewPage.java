package com.example.cab302a1;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class TeacherReviewPage extends Application {

    // Static block is a common place for initial setup
    static {
        // CRITICAL: Ensure this is called first
        DBconnection.migrate();
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/cab302a1/ReviewPage/teacher-review-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1000, 640);
        stage.setTitle("Interactive Quiz Creator");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
