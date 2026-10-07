package com.example.internmanagement.dao; 

import org.junit.jupiter.api.AfterEach; 
import org.junit.jupiter.api.BeforeEach; 
import org.junit.jupiter.api.Test; 

import java.sql.Connection; 
import java.sql.DriverManager; 
import java.sql.ResultSet; 
import java.sql.Statement; 

import static org.junit.jupiter.api.Assertions.assertEquals; 
import static org.junit.jupiter.api.Assertions.assertFalse; 
import static org.junit.jupiter.api.Assertions.assertThrows; 
import static org.junit.jupiter.api.Assertions.assertTrue; 

class ApplicationDAOTest { 
    private Connection connection; 
    private final ApplicationDAO dao = new ApplicationDAO(); 

    @BeforeEach 
    void setUp() throws Exception { 
        connection = DriverManager.getConnection("jdbc:h2:mem:" + System.nanoTime() + ";MODE=MySQL;DB_CLOSE_DELAY=-1"); 
        try (Statement statement = connection.createStatement()) { 
            statement.execute("CREATE TABLE internship_programs(id INT PRIMARY KEY, status VARCHAR(20))"); 
            statement.execute("CREATE TABLE internship_applications(id BIGINT AUTO_INCREMENT PRIMARY KEY, applicant_user_id INT, program_id INT, university_name VARCHAR(150), major_name VARCHAR(100), study_year INT, gpa DECIMAL(4,2), cover_letter CLOB, status VARCHAR(20), submitted_at TIMESTAMP, updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, UNIQUE(applicant_user_id, program_id))"); 
            statement.execute("CREATE TABLE application_decision_history(id BIGINT AUTO_INCREMENT PRIMARY KEY, application_id BIGINT, reviewer_user_id INT, previous_status VARCHAR(20), decision_status VARCHAR(20), reason CLOB)"); 
            statement.execute("INSERT INTO internship_programs(id,status) VALUES(1,'ACTIVE')"); 
        } 
    } 

    @AfterEach 
    void tearDown() throws Exception { 
        connection.close(); 
    } 

    @Test 
    void draftsCanBeEditedButSubmittedApplicationsCannotBeOverwritten() throws Exception { 
        dao.save(connection, 10, 1, "University A", "Computer Science", 3, 3.5, "First draft", false); 
        dao.save(connection, 10, 1, "University B", "Software Engineering", 4, 3.8, "Updated draft", false); 

        assertEquals("DRAFT", scalar("SELECT status FROM internship_applications")); 
        assertEquals("Updated draft", scalar("SELECT cover_letter FROM internship_applications")); 

        dao.save(connection, 10, 1, "University B", "Software Engineering", 4, 3.8, "Submitted", true); 
        assertEquals("SUBMITTED", scalar("SELECT status FROM internship_applications")); 
        assertThrows(IllegalStateException.class, () -> dao.save(connection, 10, 1, 
                "Changed", "Changed", 2, 2.0, "Must not replace", false)); 
        assertEquals("University B", scalar("SELECT university_name FROM internship_applications")); 
    } 

    @Test 
    void decisionIsRecordedOnceAndCannotBeOverwritten() throws Exception { 
        dao.save(connection, 10, 1, "University A", "Computer Science", 3, 3.5, "Submitted", true); 
        long applicationId = Long.parseLong(scalar("SELECT id FROM internship_applications")); 

        assertThrows(IllegalArgumentException.class, 
            () -> dao.decide(connection, applicationId, 20, "REJECTED", "  ")); 
        assertEquals("0", scalar("SELECT COUNT(*) FROM application_decision_history")); 
        assertTrue(dao.decide(connection, applicationId, 20, "REJECTED", "Thiếu bảng điểm")); 
        assertFalse(dao.decide(connection, applicationId, 21, "APPROVED", null)); 
        assertEquals("REJECTED", scalar("SELECT status FROM internship_applications")); 
        assertEquals("1", scalar("SELECT COUNT(*) FROM application_decision_history")); 
        assertEquals("Thiếu bảng điểm", scalar("SELECT reason FROM application_decision_history")); 
    } 

    private String scalar(String sql) throws Exception { 
        try (Statement statement = connection.createStatement(); ResultSet results = statement.executeQuery(sql)) { 
            assertTrue(results.next()); 
            return results.getString(1); 
        } 
    } 
}