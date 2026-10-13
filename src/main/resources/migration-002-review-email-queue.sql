CREATE TABLE IF NOT EXISTS review_email_queue (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  document_id BIGINT NOT NULL,
  recipient_email VARCHAR(254) NOT NULL,
  recipient_name VARCHAR(150) NOT NULL,
  document_name VARCHAR(255) NOT NULL,
  decision VARCHAR(20) NOT NULL,
  rejection_reason TEXT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  attempt_count INT NOT NULL DEFAULT 0,
  scheduled_at DATETIME NOT NULL,
  next_attempt_at DATETIME NOT NULL,
  sent_at DATETIME NULL,
  last_error VARCHAR(1000) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT uq_review_email_document UNIQUE (document_id),
  CONSTRAINT fk_review_email_document FOREIGN KEY (document_id) REFERENCES intern_documents(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
