package com.example.cab302a1.dao;

import com.example.cab302a1.DBconnection;
import com.example.cab302a1.model.QuizReview;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReviewDao {
    public List<QuizReview> getAllAttemptsById(int _user_id){
        List<QuizReview> reviews =  new ArrayList<>();
        ResponseDao responseDao = new ResponseDao();

        String sql = "SELECT qa.attempt_id, qa.quiz_id, qa.answered_by, q.title, qa.score, qa.feedback\n" +
                "FROM quiz_attempts qa\n" +
                "JOIN quizzes q ON qa.quiz_id = q.quiz_id\n" +
                "WHERE qa.answered_by = ?\n";

        try(Connection conn = DBconnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setInt(1, _user_id);

            try(ResultSet rs = pstmt.executeQuery()){
                while (rs.next()) {
                    int attemptId = rs.getInt("attempt_id");
                    int quizId = rs.getInt("quiz_id");
                    int score = rs.getInt("score");
                    String feedback = rs.getString("feedback");
                    String title = rs.getString("title");

                    int totalQuestions = responseDao.calculateScoreFromResponses(attemptId);

                    QuizReview review = new QuizReview(
                            attemptId,
                            quizId,
                            _user_id,
                            title,
                            score,
                            totalQuestions,
                            feedback
                    );

                    reviews.add(review);
                }
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        return reviews;
    }

    /**
     * Updates the feedback column for a specific quiz attempt ID.
     * @param attemptId The ID of the quiz attempt to update.
     * @param feedbackText The feedback provided by the teacher.
     * @return true if the update was successful, false otherwise.
     */
    public boolean assignFeedback(int attemptId, String feedbackText) {
        String sql = "UPDATE quiz_attempts SET feedback = ? WHERE attempt_id = ?";

        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, feedbackText);
            pstmt.setInt(2, attemptId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error assigning feedback for attempt ID " + attemptId + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

}
