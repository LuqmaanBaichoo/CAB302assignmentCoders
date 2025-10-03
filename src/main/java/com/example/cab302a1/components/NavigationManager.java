package com.example.cab302a1.components;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Stack;

/**
 * Navigation Manager for handling page navigation and history in the Interactive Quiz Creator.
 * This class provides a centralized way to manage navigation between pages and maintain
 * navigation history for proper back navigation functionality.
 * 
 * Features:
 * - Navigation history stack
 * - Back navigation support
 * - Scene management
 * - Integration with logout confirmation
 * 
 * @author CAB302 Assignment Team
 * @version 1.0
 */
public class NavigationManager {
    
    // Singleton instance
    private static NavigationManager instance;
    
    // Navigation history stack to track previous pages
    private final Stack<PageInfo> navigationHistory = new Stack<>();
    
    // Current page information
    private PageInfo currentPage;
    
    /**
     * Information about a page for navigation purposes.
     */
    public static class PageInfo {
        private final String fxmlPath;
        private final String cssPath;
        private final String title;
        private final double width;
        private final double height;
        private final boolean resizable;
        
        public PageInfo(String fxmlPath, String cssPath, String title, double width, double height, boolean resizable) {
            this.fxmlPath = fxmlPath;
            this.cssPath = cssPath;
            this.title = title;
            this.width = width;
            this.height = height;
            this.resizable = resizable;
        }
        
        public PageInfo(String fxmlPath, String cssPath, String title, double width, double height) {
            this(fxmlPath, cssPath, title, width, height, true);
        }
        
        // Getters
        public String getFxmlPath() { return fxmlPath; }
        public String getCssPath() { return cssPath; }
        public String getTitle() { return title; }
        public double getWidth() { return width; }
        public double getHeight() { return height; }
        public boolean isResizable() { return resizable; }
        
        @Override
        public String toString() {
            return String.format("PageInfo{title='%s', fxml='%s'}", title, fxmlPath);
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            PageInfo pageInfo = (PageInfo) obj;
            return fxmlPath != null ? fxmlPath.equals(pageInfo.fxmlPath) : pageInfo.fxmlPath == null;
        }
        
        @Override
        public int hashCode() {
            return fxmlPath != null ? fxmlPath.hashCode() : 0;
        }
    }
    
    /**
     * Private constructor for singleton pattern.
     */
    private NavigationManager() {
        // Initialize with default page if needed
    }
    
    /**
     * Gets the singleton instance of NavigationManager.
     * @return The NavigationManager instance
     */
    public static NavigationManager getInstance() {
        if (instance == null) {
            instance = new NavigationManager();
        }
        return instance;
    }
    
    /**
     * Navigates to a new page and adds current page to history.
     * 
     * @param stage The stage to update
     * @param pageInfo Information about the page to navigate to
     * @throws IOException if the page cannot be loaded
     */
    public void navigateTo(Stage stage, PageInfo pageInfo) throws IOException {
        // Add current page to history if it exists
        if (currentPage != null) {
            navigationHistory.push(currentPage);
            System.out.println("Added to history: " + currentPage.toString());
        }
        
        // Load and display the new page
        loadPage(stage, pageInfo);
        
        // Update current page
        currentPage = pageInfo;
        
        System.out.println("Navigated to: " + pageInfo.toString());
        System.out.println("History size: " + navigationHistory.size());
    }
    
    /**
     * Navigates to a page without adding to history (replacement navigation).
     * 
     * @param stage The stage to update
     * @param pageInfo Information about the page to navigate to
     * @throws IOException if the page cannot be loaded
     */
    public void navigateToReplace(Stage stage, PageInfo pageInfo) throws IOException {
        // Load and display the new page without adding to history
        loadPage(stage, pageInfo);
        
        // Update current page
        currentPage = pageInfo;
        
        System.out.println("Replaced current page with: " + pageInfo.toString());
    }
    
    /**
     * Navigates back to the previous page in history.
     * 
     * @param stage The stage to update
     * @return true if navigation was successful, false if no history available
     * @throws IOException if the previous page cannot be loaded
     */
    public boolean navigateBack(Stage stage) throws IOException {
        if (navigationHistory.isEmpty()) {
            System.out.println("No navigation history available");
            return false;
        }
        
        // Get previous page from history
        PageInfo previousPage = navigationHistory.pop();
        
        // Load the previous page
        loadPage(stage, previousPage);
        
        // Update current page
        currentPage = previousPage;
        
        System.out.println("Navigated back to: " + previousPage.toString());
        System.out.println("Remaining history size: " + navigationHistory.size());
        
        return true;
    }
    
    /**
     * Loads a page with the specified information.
     * 
     * @param stage The stage to update
     * @param pageInfo Information about the page to load
     * @throws IOException if the page cannot be loaded
     */
    private void loadPage(Stage stage, PageInfo pageInfo) throws IOException {
        // Load FXML
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(pageInfo.getFxmlPath()));
        Scene scene = new Scene(fxmlLoader.load(), pageInfo.getWidth(), pageInfo.getHeight());
        
        // Apply CSS if provided
        if (pageInfo.getCssPath() != null && !pageInfo.getCssPath().isEmpty()) {
            try {
                URL cssUrl = getClass().getResource(pageInfo.getCssPath());
                if (cssUrl != null) {
                    scene.getStylesheets().add(cssUrl.toExternalForm());
                    System.out.println("CSS loaded: " + pageInfo.getCssPath());
                } else {
                    System.out.println("Warning: CSS not found: " + pageInfo.getCssPath());
                }
            } catch (Exception e) {
                System.err.println("Error loading CSS: " + e.getMessage());
            }
        }
        
        // Update navbar active state based on destination page (no polling, event-driven)
        try {
            String fxml = pageInfo.getFxmlPath();
            String pageType = "home";
            if (fxml != null) {
                String lower = fxml.toLowerCase();
                if (lower.contains("teacher-review-view") || lower.contains("student-review-view")) {
                    pageType = "review";
                } else if (lower.contains("timetable")) {
                    pageType = "timetable";
                } else if (lower.contains("homepage.fxml")) {
                    pageType = "home";
                }
            }
            // Call after FXML load so the new NavbarController instance has initialized
            com.example.cab302a1.components.NavbarController.updateNavbarState(pageType);
        } catch (Exception ignore) { }
        
        // Update stage
        stage.setTitle(pageInfo.getTitle());
        stage.setScene(scene);
        stage.setResizable(pageInfo.isResizable());
        stage.centerOnScreen();
    }
    
    /**
     * Clears the navigation history.
     * Useful when starting a new session or after logout.
     */
    public void clearHistory() {
        navigationHistory.clear();
        currentPage = null;
        System.out.println("Navigation history cleared");
    }
    
    /**
     * Gets the current page information.
     * @return Current page info or null if no current page
     */
    public PageInfo getCurrentPage() {
        return currentPage;
    }
    
    /**
     * Gets the size of navigation history.
     * @return Number of pages in history
     */
    public int getHistorySize() {
        return navigationHistory.size();
    }
    
    /**
     * Checks if there is navigation history available.
     * @return true if back navigation is possible
     */
    public boolean hasHistory() {
        return !navigationHistory.isEmpty();
    }
    
    /**
     * Sets the current page without navigation (useful for initialization).
     * This method is typically called during page initialization to register the page
     * with NavigationManager for proper history tracking.
     * 
     * @param pageInfo The page info to set as current
     */
    public void setCurrentPage(PageInfo pageInfo) {
        // Only update if it's actually different to avoid unnecessary operations
        if (this.currentPage == null || !this.currentPage.equals(pageInfo)) {
            this.currentPage = pageInfo;
            System.out.println("Current page set to: " + pageInfo.toString());
            System.out.println("Navigation history size: " + navigationHistory.size());
        }
    }
    
    // Predefined page constants for common pages
    public static class Pages {

        public static final PageInfo HOME = new PageInfo(
                "/com/example/cab302a1/HomePage.fxml",
                "/com/example/cab302a1/HomePage.css",
                "Interactive Quiz Creator - Quiz Home",
                1000, 640, true
        );

        // --- ADDED NEW PAGE CONSTANTS FOR REVIEW PAGES ---
        public static final PageInfo TEACHER_REVIEW = new PageInfo(
                "/com/example/cab302a1/ReviewPage/teacher-review-view.fxml",
                "/com/example/cab302a1/ReviewPage/ReviewPage.css",
                "Interactive Quiz Creator - Teacher Review",
                1000, 640, true
        );
        public static final PageInfo STUDENT_REVIEW = new PageInfo(
                "/com/example/cab302a1/ReviewPage/student-review-view.fxml",
                "/com/example/cab302a1/ReviewPage/ReviewPage.css",
                "Interactive Quiz Creator - Student Review",
                1000, 640, true
        );

        public static final PageInfo NAVBAR_DEMO = new PageInfo(
            "/com/example/cab302a1/demo-navbar-integration.fxml",
            "/com/example/cab302a1/styles.css",
            "Interactive Quiz Creator - Navbar Demo",
            1000, 650, true
        );
        
        public static final PageInfo QUIZ_RESULT = new PageInfo(
            "/com/example/cab302a1/result/QuizResult.fxml",
            "/com/example/cab302a1/result/QuizResult.css",
            "Interactive Quiz Creator - Quiz Results",
            1000, 650, true
        );
        
        public static final PageInfo LOGIN = new PageInfo(
            "/com/example/cab302a1/Login/Login-view.fxml",
            "/com/example/cab302a1/Login/Login.css",
            "Interactive Quiz Creator - Login",
            800, 600, true
        );
        
        public static final PageInfo LOGOUT_CONFIRMATION = new PageInfo(
            "/com/example/cab302a1/logout/LogoutConfirmation.fxml",
            "/com/example/cab302a1/logout/LogoutConfirmation.css",
            "Interactive Quiz Creator - Logout Confirmation",
            400, 300, false
        );
    }
}
