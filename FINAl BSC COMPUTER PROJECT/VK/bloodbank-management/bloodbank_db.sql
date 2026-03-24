-- Create database
CREATE DATABASE IF NOT EXISTS bloodbank_db;
USE bloodbank_db;

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(20),
    blood_group VARCHAR(5),
    age INT,
    gender VARCHAR(10),
    address TEXT,
    city VARCHAR(50),
    state VARCHAR(50),
    role VARCHAR(20) NOT NULL,
    enabled BOOLEAN DEFAULT TRUE,
    registration_date DATETIME,
    last_login DATETIME,
    INDEX idx_username (username),
    INDEX idx_blood_group (blood_group)
);

-- Blood stock table
CREATE TABLE IF NOT EXISTS blood_stock (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    blood_group VARCHAR(5) UNIQUE NOT NULL,
    units INT DEFAULT 0,
    quantity INT DEFAULT 0,
    storage_location VARCHAR(100),
    last_updated DATETIME,
    expiry_date DATETIME,
    INDEX idx_blood_group_stock (blood_group)
);

-- Blood requests table
CREATE TABLE IF NOT EXISTS blood_requests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    patient_name VARCHAR(100) NOT NULL,
    blood_group VARCHAR(5) NOT NULL,
    units INT NOT NULL,
    hospital_name VARCHAR(200),
    hospital_address TEXT,
    doctor_name VARCHAR(100),
    contact_phone VARCHAR(20),
    reason TEXT,
    status VARCHAR(20) DEFAULT 'PENDING',
    request_date DATETIME,
    required_by DATETIME,
    processed_date DATETIME,
    processed_by BIGINT,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (processed_by) REFERENCES users(id),
    INDEX idx_status (status),
    INDEX idx_blood_group_request (blood_group)
);

-- Donations table
CREATE TABLE IF NOT EXISTS donations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    blood_group VARCHAR(5) NOT NULL,
    quantity INT NOT NULL,
    donation_date DATETIME,
    next_eligible_date DATETIME,
    hospital_name VARCHAR(200),
    donation_type VARCHAR(50),
    health_status VARCHAR(50),
    tested BOOLEAN DEFAULT FALSE,
    test_results TEXT,
    status VARCHAR(20) DEFAULT 'PENDING',
    notes TEXT,
    created_date DATETIME,
    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_donation_status (status),
    INDEX idx_donation_date (donation_date)
);

-- Insert default blood groups
INSERT INTO blood_stock (blood_group, units, quantity, storage_location) VALUES
('A+', 10, 4500, 'Section A-1'),
('A-', 5, 2250, 'Section A-2'),
('B+', 15, 6750, 'Section B-1'),
('B-', 8, 3600, 'Section B-2'),
('O+', 20, 9000, 'Section O-1'),
('O-', 12, 5400, 'Section O-2'),
('AB+', 7, 3150, 'Section AB-1'),
('AB-', 4, 1800, 'Section AB-2')
ON DUPLICATE KEY UPDATE 
units = VALUES(units),
quantity = VALUES(quantity);

-- Create default admin (password: admin123@)
INSERT INTO users (username, password, full_name, email, role, enabled, registration_date)
VALUES (
    'admin1', 
    '$2a$10$rTpJqZQ8xKqQ5Q5Q5Q5Q5uQ5Q5Q5Q5Q5Q5Q5Q5Q5Q5Q5Q5Q5Q5Q5', -- BCrypt hash of 'admin123@'
    'System Administrator',
    'admin@bloodbank.com',
    'ADMIN',
    TRUE,
    NOW()
) ON DUPLICATE KEY UPDATE username = username;