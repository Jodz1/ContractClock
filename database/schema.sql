CREATE DATABASE IF NOT EXISTS time_tracker_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE time_tracker_db;

CREATE TABLE IF NOT EXISTS clients (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  email VARCHAR(255),
  company_name VARCHAR(255),
  notes TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS contracts (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  client_id BIGINT NOT NULL,
  title VARCHAR(255) NOT NULL,
  description TEXT,
  status ENUM('ACTIVE', 'PAUSED', 'COMPLETED', 'ARCHIVED') DEFAULT 'ACTIVE',
  current_hourly_rate DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  currency_code VARCHAR(10) DEFAULT 'USD',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_contract_client
    FOREIGN KEY (client_id) REFERENCES clients(id)
    ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS contract_rates (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  contract_id BIGINT NOT NULL,
  hourly_rate DECIMAL(10,2) NOT NULL,
  currency_code VARCHAR(10) DEFAULT 'USD',
  valid_from DATETIME NOT NULL,
  valid_to DATETIME NULL,
  note TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_rate_contract
    FOREIGN KEY (contract_id) REFERENCES contracts(id)
    ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS activity_tags (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL UNIQUE,
  color_hex VARCHAR(20),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS work_sessions (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  contract_id BIGINT NOT NULL,
  rate_id BIGINT NULL,
  activity_tag_id BIGINT NULL,
  start_time DATETIME NOT NULL,
  end_time DATETIME NULL,
  duration_seconds BIGINT DEFAULT 0,
  hourly_rate_snapshot DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  currency_code VARCHAR(10) DEFAULT 'USD',
  calculated_amount DECIMAL(10,2) DEFAULT 0.00,
  override_amount DECIMAL(10,2) NULL,
  final_amount DECIMAL(10,2) DEFAULT 0.00,
  chargeable BOOLEAN DEFAULT TRUE,
  description TEXT,
  source ENUM('TRACKER', 'MANUAL') DEFAULT 'TRACKER',
  status ENUM('RUNNING', 'STOPPED', 'DISCARDED') DEFAULT 'STOPPED',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_session_contract
    FOREIGN KEY (contract_id) REFERENCES contracts(id)
    ON DELETE CASCADE,
  CONSTRAINT fk_session_rate
    FOREIGN KEY (rate_id) REFERENCES contract_rates(id)
    ON DELETE SET NULL,
  CONSTRAINT fk_session_activity_tag
    FOREIGN KEY (activity_tag_id) REFERENCES activity_tags(id)
    ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS payments (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  contract_id BIGINT NOT NULL,
  amount DECIMAL(10,2) NOT NULL,
  currency_code VARCHAR(10) DEFAULT 'USD',
  payment_date DATE NOT NULL,
  note TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_payment_contract
    FOREIGN KEY (contract_id) REFERENCES contracts(id)
    ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS app_settings (
  setting_key VARCHAR(100) PRIMARY KEY,
  setting_value TEXT,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS window_state (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  window_name VARCHAR(100) NOT NULL UNIQUE,
  x DOUBLE,
  y DOUBLE,
  width DOUBLE,
  height DOUBLE,
  maximized BOOLEAN DEFAULT FALSE,
  last_opened_contract_id BIGINT NULL,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE INDEX idx_sessions_contract_time ON work_sessions(contract_id, start_time, end_time);
CREATE INDEX idx_sessions_status ON work_sessions(status);
CREATE INDEX idx_rates_contract_dates ON contract_rates(contract_id, valid_from, valid_to);
CREATE INDEX idx_payments_contract_date ON payments(contract_id, payment_date);
