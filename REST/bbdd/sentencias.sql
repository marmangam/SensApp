-- Script .sql encargado de borrar las tablas existentes y de crear nuevas tablas para almacenar datos del proyecto.


-- Borramos las tablas si existen
DROP TABLE IF EXISTS USUARIOS;
DROP TABLE IF EXISTS RESPONSABLES;
DROP TABLE IF EXISTS ALARMAS;
DROP TABLE IF EXISTS EVENTOS;
DROP TABLE IF EXISTS ADMINISTRATIVOS;
DROP TABLE IF EXISTS OPERADORES;
DROP TABLE IF EXISTS INCIDENCIAS;

-- Creamos la tabla USUARIOS:
-- donde se almacenan todos los datos de los usuarios registrados
CREATE TABLE USUARIOS (
	dni VARCHAR(9) PRIMARY KEY,
    nombre VARCHAR(25) NOT NULL,
    apellidos VARCHAR(50) NOT NULL,
	telefono INT NOT NULL,
	fecha_nacimiento DATE,
	domicilio VARCHAR(50),
	enfermedades_previas TEXT,
	alergias TEXT,
	contrasena VARCHAR(20) NOT NULL
);

-- Creamos la tabla RESPONSABLES:
-- donde se almacenan los datos de los responsables registrados
CREATE TABLE RESPONSABLES (
	usuario VARCHAR(20) PRIMARY KEY,
	nombre VARCHAR(25) NOT NULL,
    apellidos VARCHAR(50) NOT NULL,
	dni VARCHAR(9) NOT NULL,
	dni_usuario VARCHAR(9) REFERENCES USUARIOS(dni) 
		ON DELETE RESTRICT ON UPDATE RESTRICT NOT NULL,
	telefono INT NOT NULL,
	contrasena VARCHAR(20) NOT NULL
);

-- Creamos la tabla ALARMAS:
-- donde se almacenan los datos de las alarmas de los usuarios
CREATE TABLE ALARMAS (
    dni_usuario VARCHAR(9) REFERENCES USUARIOS (dni)
    	    ON DELETE RESTRICT ON UPDATE RESTRICT,
	nombre VARCHAR(25),
	tipo VARCHAR(25),
    hora TIME NOT NULL,
	completado DATE,
    PRIMARY KEY (DNI_USUARIO, NOMBRE)
);

-- Creamos la tabla EVENTOS:
-- donde se almacenan los datos de los eventos de los usuarios
CREATE TABLE EVENTOS (
    dni_usuario VARCHAR(9) REFERENCES USUARIOS (dni)
    	    ON DELETE RESTRICT ON UPDATE RESTRICT,
	nombre VARCHAR(25),
	tipo VARCHAR(25),
    dia DATE NOT NULL,
	completado BOOLEAN NOT NULL,
    PRIMARY KEY (DNI_USUARIO, NOMBRE)
);
-- Creamos la tabla ADMINISTRATIVOS:
-- donde se almacenan los datos de los administrativos
CREATE TABLE ADMINISTRATIVOS (
	usuario VARCHAR(20) PRIMARY KEY,
	nombre VARCHAR(25) NOT NULL,
	apellidos VARCHAR(50) NOT NULL,
	contrasena VARCHAR(20) NOT NULL
);

-- Creamos la tabla OPERADORES:
-- donde se almacenan los datos de los operadores
CREATE TABLE OPERADORES (
    usuario VARCHAR(20) PRIMARY KEY,
	nombre VARCHAR(25) NOT NULL,
	apellidos VARCHAR(50) NOT NULL,
	contrasena VARCHAR(20) NOT NULL,
	activo BOOLEAN NOT NULL,
	ocupado BOOLEAN NOT NULL
);

-- Creamos la tabla INCIDENCIAS:
-- donde se almacenan los datos de las incidencias registradas
CREATE TABLE INCIDENCIAS (
	id SERIAL PRIMARY KEY,
    dni_usuario VARCHAR(9) REFERENCES USUARIOS (dni)
    	    ON DELETE RESTRICT ON UPDATE RESTRICt,
	operador VARCHAR(20) REFERENCES OPERADORES(usuario)
			ON DELETE SET NULL ON UPDATE RESTRICT,
    descripcion TEXT,
    procedimiento TEXT,
	resuelta BOOLEAN NOT NULL,
	fecha DATE NOT NULL, 
	hora TIME NOT NULL
);


