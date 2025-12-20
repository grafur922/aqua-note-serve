CREATE TABLE refresh_token (
  token_hash VARCHAR(64) NOT NULL,
  user_id VARCHAR(36) NOT NULL,
  expires_at DATETIME NOT NULL,
  created_at DATETIME NOT NULL,
  revoked_at DATETIME DEFAULT NULL,
  replaced_by_hash VARCHAR(64) DEFAULT NULL,
  PRIMARY KEY (token_hash),
  KEY idx_user_id (user_id),
  KEY idx_expires_at (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
