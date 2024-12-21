create database MapApp2_DB;
use MapApp2_DB;

CREATE TABLE map_snapshots (
    id INT AUTO_INCREMENT PRIMARY KEY,
    image_path VARCHAR(255) NOT NULL,
    latitude DOUBLE NOT NULL,
    longitude DOUBLE NOT NULL,
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
);

