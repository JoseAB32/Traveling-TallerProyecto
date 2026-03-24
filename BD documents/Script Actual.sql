CREATE DATABASE tallerlocal;  

USE tallerlocal;

DROP TABLE IF EXISTS reviews; --
DROP TABLE IF EXISTS favorites; --
DROP TABLE IF EXISTS trip_items;
DROP TABLE IF EXISTS trips;
DROP TABLE IF EXISTS places;  -- DOne
DROP TABLE IF EXISTS users;    -- 
DROP TABLE IF EXISTS cities; ---



-- 1. CIUDADES
CREATE TABLE IF NOT EXISTS cities (
    id INT AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL UNIQUE,
    state BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id)
);

-- 2. USUARIOS
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT,
    correo VARCHAR(100) NOT NULL UNIQUE, 
    user_name VARCHAR(50) NOT NULL,
    pass VARCHAR(255) NOT NULL,          
    birthday DATE,                        
    city_id INT,
    state BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    CONSTRAINT fk_user_city FOREIGN KEY (city_id) REFERENCES cities(id)
);

-- 3. LUGARES
CREATE TABLE IF NOT EXISTS places (
    id INT AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description TEXT, -- Correcto para textos largos
    address VARCHAR(255),
    rating DECIMAL(2,1) DEFAULT 5.0,
    price DECIMAL(2,1), 
    latitude DECIMAL(10, 8),
	longitude DECIMAL(11, 8),
    place_type VARCHAR(50),
    city_id INT,
    is_event BOOLEAN DEFAULT FALSE,
    start_date DATE NULL, 
    end_date DATE NULL,   
    image_url VARCHAR(300),
    state BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    CONSTRAINT fk_place_city FOREIGN KEY (city_id) REFERENCES cities(id)
);

-- 4. RESEÑAS (Entidad Débil/Intermedia entre Users y Places)
CREATE TABLE IF NOT EXISTS reviews (
    id INT AUTO_INCREMENT,
    user_id INT NOT NULL,
    place_id INT NOT NULL,
    parent_id INT DEFAULT NULL, 
    comment TEXT,
    score INT NULL, 
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    state BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    CONSTRAINT fk_review_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_review_place FOREIGN KEY (place_id) REFERENCES places(id),
    CONSTRAINT fk_review_parent FOREIGN KEY (parent_id) REFERENCES reviews(id),
    -- Mantenemos la validación de rango solo si el score NO es nulo
    CONSTRAINT check_score CHECK (score IS NULL OR (score BETWEEN 1 AND 5))
);

-- 5. VIAJES 
CREATE TABLE IF NOT EXISTS trips (
    id INT AUTO_INCREMENT,
    user_id INT,
    name VARCHAR(100) NOT NULL, 
    start_date DATE,
    end_date DATE,
    state BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    CONSTRAINT fk_trip_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 6. DETALLES DEL ITINERARIO, QUE LUGARES VIAJA EN QUE ORDEN
CREATE TABLE IF NOT EXISTS trip_items (
    id INT AUTO_INCREMENT,
    trip_id INT,
    place_id INT,
    visit_order INT, 
    state BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    CONSTRAINT fk_item_trip FOREIGN KEY (trip_id) REFERENCES trips(id),
    CONSTRAINT fk_item_place FOREIGN KEY (place_id) REFERENCES places(id)
);

-- 7. FAVORITOS (Entidad intermedia entre Users y Places)
CREATE TABLE IF NOT EXISTS favorites (
    id INT AUTO_INCREMENT,
    user_id INT NOT NULL,
    place_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    state BOOLEAN NOT NULL DEFAULT TRUE, 
    PRIMARY KEY (id),
    CONSTRAINT fk_fav_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_fav_place FOREIGN KEY (place_id) REFERENCES places(id),
    UNIQUE KEY unique_user_place (user_id, place_id) 
);


DELIMITER //

CREATE TRIGGER update_place_rating_after_insert
AFTER INSERT ON reviews
FOR EACH ROW
BEGIN
    -- Calculamos el promedio de todos los scores para ese lugar
    UPDATE places 
    SET rating = (SELECT AVG(score) FROM reviews WHERE place_id = NEW.place_id AND state = TRUE)
    WHERE id = NEW.place_id;
END //

DELIMITER ;


INSERT INTO cities (name) VALUES 
('La Paz'), 
('Cochabamba'), 
('Santa Cruz'), 
('Oruro'), 
('Potosí'), 
('Tarija'), 
('Chuquisaca'), 
('Beni'), 
('Pando');




INSERT INTO users (correo, user_name, pass, birthday, city_id) 
VALUES ('admin@gmail.com', 'admin', '1234', '2004-12-11', 1);

SELECT * from users;