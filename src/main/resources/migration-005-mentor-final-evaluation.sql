-- Run once on an existing internship_management database before deploying this version.
USE internship_management;

CREATE TABLE IF NOT EXISTS mentor_final_evaluations (
    id                       BIGINT AUTO_INCREMENT PRIMARY KEY,
    intern_profile_id        BIGINT       NOT NULL,
    mentor_id                BIGINT       NOT NULL,
    status                   VARCHAR(30)  NOT NULL DEFAULT 'DRAFT',
    knowledge_score          TINYINT      NULL,
    problem_solving_score    TINYINT      NULL,
    initiative_score         TINYINT      NULL,
    teamwork_score           TINYINT      NULL,
    skill_score              DECIMAL(3,1) NULL,
    attitude_score           DECIMAL(3,1) NULL,
    overall_score            DECIMAL(3,1) NULL,
    overall_comments         VARCHAR(2000) NOT NULL DEFAULT '',
    revision_no              INT          NOT NULL DEFAULT 1,
    finalization_count       INT          NOT NULL DEFAULT 0,
    finalized_at             DATETIME     NULL,
    created_at               DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at               DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_mentor_final_evaluation_intern UNIQUE (intern_profile_id),
    CONSTRAINT fk_mentor_final_evaluation_profile FOREIGN KEY (intern_profile_id) REFERENCES intern_profiles(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_mentor_final_evaluation_mentor FOREIGN KEY (mentor_id) REFERENCES mentors(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_mentor_final_evaluation_status CHECK (status IN ('DRAFT','FINALIZED','FINALIZED_AGAIN')),
    CONSTRAINT chk_mentor_final_evaluation_knowledge CHECK (knowledge_score IS NULL OR knowledge_score BETWEEN 1 AND 5),
    CONSTRAINT chk_mentor_final_evaluation_problem CHECK (problem_solving_score IS NULL OR problem_solving_score BETWEEN 1 AND 5),
    CONSTRAINT chk_mentor_final_evaluation_initiative CHECK (initiative_score IS NULL OR initiative_score BETWEEN 1 AND 5),
    CONSTRAINT chk_mentor_final_evaluation_teamwork CHECK (teamwork_score IS NULL OR teamwork_score BETWEEN 1 AND 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Đánh giá tổng kết theo tiêu chí của mentor, có lưu trạng thái nháp/chốt';

CREATE TABLE IF NOT EXISTS mentor_final_evaluation_history (
    id                       BIGINT AUTO_INCREMENT PRIMARY KEY,
    evaluation_id            BIGINT       NOT NULL,
    revision_no              INT          NOT NULL,
    actor_user_id            BIGINT       NULL,
    action_code              VARCHAR(30)  NOT NULL,
    status                   VARCHAR(30)  NOT NULL,
    knowledge_score          TINYINT      NULL,
    problem_solving_score    TINYINT      NULL,
    initiative_score         TINYINT      NULL,
    teamwork_score           TINYINT      NULL,
    skill_score              DECIMAL(3,1) NULL,
    attitude_score           DECIMAL(3,1) NULL,
    overall_score            DECIMAL(3,1) NULL,
    overall_comments         VARCHAR(2000) NOT NULL DEFAULT '',
    archived_at              DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_mentor_final_evaluation_revision UNIQUE (evaluation_id, revision_no),
    CONSTRAINT fk_mentor_final_evaluation_history_evaluation FOREIGN KEY (evaluation_id)
        REFERENCES mentor_final_evaluations(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_mentor_final_evaluation_history_actor FOREIGN KEY (actor_user_id) REFERENCES users(id)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Lịch sử bất biến các phiên bản đánh giá tổng kết';
