package com.example.internmanagement.dao;

import com.example.internmanagement.model.Candidate;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CandidateDAOTest {

    @Test
    void savesAndUpdatesOneApplicationForTheCandidateAccount() throws Exception {
        try (Connection connection = DriverManager.getConnection(
                "jdbc:h2:mem:candidate-dao-test;MODE=MySQL;DB_CLOSE_DELAY=-1")) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("CREATE TABLE candidates (id BIGINT AUTO_INCREMENT PRIMARY KEY, "
                        + "user_id BIGINT UNIQUE, full_name VARCHAR(100) NOT NULL, email VARCHAR(100) NOT NULL, "
                        + "phone VARCHAR(20), university_name VARCHAR(150), major_name VARCHAR(100), "
                        + "desired_position VARCHAR(100), cv_file_url VARCHAR(255), "
                        + "application_status VARCHAR(30) NOT NULL DEFAULT 'NEW', hr_notes TEXT, "
                        + "applied_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP)");
            }

            CandidateDAO dao = new CandidateDAO();
            Candidate candidate = application();
            dao.save(connection, candidate);
            int firstId = candidate.getId();

            Candidate saved = dao.findByUserId(connection, 42);
            assertNotNull(saved);
            assertEquals(firstId, saved.getId());
            assertEquals("NEW", saved.getApplicationStatus());
            assertEquals("/candidate-files/first.pdf", saved.getCvFileUrl());

            candidate.setUniversityName("Đại học mới");
            candidate.setCvFileUrl("/candidate-files/updated.pdf");
            dao.save(connection, candidate);

            Candidate updated = dao.findByUserId(connection, 42);
            assertNotNull(updated);
            assertEquals(firstId, updated.getId());
            assertEquals("Đại học mới", updated.getUniversityName());
            assertEquals("/candidate-files/updated.pdf", updated.getCvFileUrl());
        }
    }

    private Candidate application() {
        Candidate candidate = new Candidate();
        candidate.setUserId(42);
        candidate.setFullName("Nguyễn An");
        candidate.setEmail("an@example.com");
        candidate.setPhone("0912345678");
        candidate.setUniversityName("Đại học A");
        candidate.setMajorName("Công nghệ thông tin");
        candidate.setDesiredPosition("Java Intern");
        candidate.setCvFileUrl("/candidate-files/first.pdf");
        return candidate;
    }
}
