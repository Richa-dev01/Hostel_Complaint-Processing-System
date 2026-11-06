CREATE DATABASE IF NOT EXISTS hostel_db;
USE hostel_db;

CREATE TABLE student (
  student_id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  email VARCHAR(100) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  room_no VARCHAR(10)
);

CREATE TABLE admin (
  admin_id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  email VARCHAR(100) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL
);

CREATE TABLE category (
  category_id INT AUTO_INCREMENT PRIMARY KEY,
  category_name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE complaint (
  complaint_id VARCHAR(20) PRIMARY KEY,
  student_id INT NOT NULL,
  category_id INT NOT NULL,
  room_no VARCHAR(10) NOT NULL,
  description VARCHAR(1000) NOT NULL,
  image_url VARCHAR(500),
  priority VARCHAR(10) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'SUBMITTED',
  created_at DATETIME NOT NULL,
  updated_at DATETIME,
  resolved_at DATETIME,
  resolution_remarks VARCHAR(500),
  assigned_to VARCHAR(100),
  FOREIGN KEY (student_id) REFERENCES student(student_id),
  FOREIGN KEY (category_id) REFERENCES category(category_id)
);

INSERT INTO category (category_name) VALUES
 ('Electrical'),('Plumbing'),('Cleaning'),('Wi-Fi/Internet'),
 ('Furniture'),('Water supply'),('Maintenance'),('Other');

INSERT INTO student (name, email, password_hash, room_no)
VALUES ('Test Student', 'test@nie.ac.in', 'temp', '204');
