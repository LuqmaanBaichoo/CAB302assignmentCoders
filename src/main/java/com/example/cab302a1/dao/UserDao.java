package com.example.cab302a1.dao;

import com.example.cab302a1.DBconnection;
import com.example.cab302a1.SignUp.SignUpController;
import com.example.cab302a1.model.Student;
import com.example.cab302a1.model.Teacher;
import com.example.cab302a1.model.User;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javax.sound.sampled.Control;
import javax.xml.transform.Result;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import org.mindrot.jbcrypt.BCrypt;

public class UserDao {

    public User getUserById(int _userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, _userId);

            try(ResultSet rs = pstmt.executeQuery()){
                if(rs.next()){
                    String role = rs.getString("role");
                    if ("Teacher".equals(role)) {
                        return new Teacher(
                                rs.getInt("user_id"),
                                rs.getString("username"),
                                rs.getString("email"),
                                role,
                                rs.getTimestamp("created_at")
                        );
                    } else {
                        return new Student(
                                rs.getInt("user_id"),
                                rs.getString("username"),
                                rs.getString("email"),
                                role,
                                rs.getTimestamp("created_at")
                        );
                    }
                }
            }
        }catch (SQLException e ){
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Retrieves a list of all users with the role 'Student'.
     * This is used by the Teacher Review Controller to populate the student list.
     */
    public List<User> getAllStudents() {
        List<User> students = new ArrayList<>();
        // Select all fields needed by the Student constructor
        String sql = "SELECT user_id, username, email, role, created_at FROM users WHERE role = 'Student'";

        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                // Create Student objects from the results
                students.add(new Student(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("role"),
                        rs.getTimestamp("created_at")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return students;
    }

    //for debugging
    public void printAllUsers() {
        String sql = "SELECT user_id, username, email, password, created_at, role FROM users";
        try (Connection conn = DBconnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                System.out.println(
                        rs.getInt("user_id") + " | " +
                                rs.getString("username") + " | " +
                                rs.getString("email") + " | " +
                                rs.getTimestamp("created_at") + " | " +
                                rs.getString("password") + " | " +
                                rs.getString("role")
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean existsByEmail(String _email){
        String sql = "SELECT 1 FROM users where email = ? LIMIT 1";

        try(Connection conn = DBconnection.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql))
        {
            pstmt.setString(1, _email);
            try(ResultSet rs = pstmt.executeQuery()){
                return rs.next();
            }

        } catch (SQLException e ){
            e.printStackTrace();
        }
        return false;
    }

    public User signUpUser(String _username, String _email, String _password, String _role){
        if(existsByEmail(_email)){
            System.out.printf("User already exists: " + _email);
            return null;
        }
        String sql = "INSERT INTO users(username, email, password, role) VALUES (?, ?, ?, ?)";
        String hashedPassword = BCrypt.hashpw(_password, BCrypt.gensalt());

        try(Connection conn = DBconnection.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, _username);
            pstmt.setString(2, _email);
            pstmt.setString(3, hashedPassword);
            pstmt.setString(4, _role);

            int affectedRows = pstmt.executeUpdate();

            if(affectedRows > 0){
                try(ResultSet rs = pstmt.getGeneratedKeys()){
                    if(rs.next()){
                        int newId = rs.getInt(1);
                        User currentUser = getUserById(newId);
                        return currentUser;
                    }
                }
            }else {
                System.out.println("Sign-up User failed.");
                return null;
            }
            System.out.printf("User: " + _username + " Email: " + _email + " Password: " + _password + " Role: " + _role + " Added");

        } catch (SQLException e){
            e.printStackTrace();
        }
        return null;
    }

    public User login(String _email, String _password){
        if(!existsByEmail(_email)){
            System.out.printf("User not found: " + _email);
            return null;
        }
        String sql = "SELECT * FROM users WHERE email = ?";
        try {Connection conn = DBconnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, _email);


            try(ResultSet rs = pstmt.executeQuery()){
                if(rs.next()){
                    String storedPassword = rs.getString("password");
                    if(BCrypt.checkpw(_password, storedPassword)){
                        String role = rs.getString("role");

                        if("Teacher".equals(role)){
                            return new Teacher(
                                    rs.getInt("user_id"),
                                    rs.getString("username"),
                                    rs.getString("email"),
                                    role,
                                    rs.getTimestamp("created_at")
                            );
                        }else if("Student".equals(role)){
                            return new Student(
                                    rs.getInt("user_id"),
                                    rs.getString("username"),
                                    rs.getString("email"),
                                    role,
                                    rs.getTimestamp("created_at")
                            );
                        }
                    }
                }

            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        return null;
    }



}
