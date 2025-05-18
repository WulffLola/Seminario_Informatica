-- Crear base de datos y usarla
DROP DATABASE IF EXISTS gestion_flotas;
CREATE DATABASE gestion_flotas;
USE gestion_flotas;

-- Crear tabla Ranks
CREATE TABLE Ranks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    level INT NOT NULL UNIQUE
);

-- Crear tabla Person
CREATE TABLE Person (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    dni VARCHAR(8) NOT NULL UNIQUE,
    birthdate DATE NOT NULL,
    rank_id INT NOT NULL,
    FOREIGN KEY (rank_id) REFERENCES Ranks(id)
);

-- Crear tabla Fleet
CREATE TABLE Fleet (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    size INT NOT NULL,
    admiral_id INT NOT NULL,
    FOREIGN KEY (admiral_id) REFERENCES Person(id)
);

-- Crear tabla Ship
CREATE TABLE Ship (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    size INT NOT NULL,
    fleet_id INT NOT NULL,
    FOREIGN KEY (fleet_id) REFERENCES Fleet(id)
);

-- Crear tabla Station
CREATE TABLE Station (
    id INT AUTO_INCREMENT PRIMARY KEY,
    person_id INT NOT NULL,
    ship_id INT NOT NULL,
    rank_id INT NOT NULL,
    start_date DATE NOT NULL DEFAULT CURRENT_DATE,
    end_date DATE,
    FOREIGN KEY (person_id) REFERENCES Person(id),
    FOREIGN KEY (ship_id) REFERENCES Ship(id),
    FOREIGN KEY (rank_id) REFERENCES Ranks(id)
);

-- Insertar datos en Ranks
INSERT INTO Ranks (name, level) 
VALUES 
    ('Admiral', 1), 
    ('Captain', 2), 
    ('First Officer', 3), 
    ('Engineer', 4), 
    ('Crewman', 5);

-- Insertar datos en Person
INSERT INTO Person (name, last_name, dni, birthdate, rank_id) 
VALUES 
    ('Ludwig', 'Fischer', '12345', '1975-04-12', 1),
    ('Maria', 'Torres', '54321', '1982-09-20', 2),
    ('Erik', 'Zimmer', '67890', '1990-06-30', 3),
    ('Sofía', 'Bianchi', '09876', '1988-02-14', 4),
    ('Carlos', 'Vega', '11223', '1995-12-01', 5);

-- Insertar datos en Fleet
INSERT INTO Fleet (name, size, admiral_id) 
VALUES 
    ('Flota del Atlántico Sur', 10, 1),
    ('Flota del Pacífico Norte', 8, 2);

-- Insertar datos en Ship
INSERT INTO Ship (name, size, fleet_id) 
VALUES 
    ('Buque Libertad', 150, 1),
    ('Santa María', 120, 1),
    ('Poseidón', 100, 2),
    ('Nautilus', 90, 2);

-- Insertar datos en Station
INSERT INTO Station (person_id, ship_id, rank_id, start_date) 
VALUES 
    (1, 1, 1, CURRENT_DATE),  -- Admiral on Buque Libertad
    (2, 1, 2, CURRENT_DATE),  -- Captain on Buque Libertad
    (3, 1, 3, CURRENT_DATE),  -- First Officer on Buque Libertad
    (4, 2, 4, CURRENT_DATE),  -- Engineer on Santa María
    (5, 2, 5, CURRENT_DATE),  -- Crewman on Santa María
    (2, 3, 2, CURRENT_DATE);  -- Captain on Poseidón
