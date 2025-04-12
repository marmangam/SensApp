-- Script .sql encargado de insertar unos datos iniciales en las tablas del proyecto


-- Introducimos unos valores iniciales en la tabla USUARIOS
INSERT INTO USUARIOS (dni, nombre, apellidos, telefono, fecha_nacimiento, domicilio, enfermedades_previas, alergias, contrasena) 
VALUES 	('12345678A', 'Juan', 'Pérez García', 600123456, '1980-01-01', 'Calle Falsa 123', 'Hipertensión', 'Polen', 'password1'),	
		('23456789B', 'María', 'López Martínez', 600234567, '1990-02-02', 'Avenida Siempreviva 456', 'Diabetes', 'Ninguna', 'password2'),
		('34567890C', 'Carlos', 'Sánchez Fernández', 600345678, '2000-03-03', 'Plaza Mayor 789', 'Asma', 'Polvo', 'password3');

-- Introducimos unos valores iniciales en la tabla RESPONSABLES
INSERT INTO RESPONSABLES (usuario, nombre, apellidos, dni, dni_usuario, telefono, contrasena) 
VALUES	('resp1', 'Ana', 'García López', '45678901D', '12345678A', 600456789, 'resp1pass'),
		('resp2', 'Luis', 'Martín Gómez', '56789012E', '23456789B', 600567890, 'resp2pass'),
		('resp3', 'Elena', 'Ruiz Sánchez', '67890123F', '34567890C', 600678901, 'resp3pass');

-- Introducimos unos valores iniciales en la tabla ALARMAS
INSERT INTO ALARMAS (dni_usuario, nombre, tipo, hora, completado) 
VALUES	('12345678A', 'Alarma1', 'Medicamento', '08:00:00', '2020-01-01'),
		('23456789B', 'Alarma2', 'Ejercicio', '09:00:00', '2020-01-01'),
		('34567890C', 'Alarma3', 'Consulta', '10:00:00', '2020-01-01');

-- Introducimos unos valores iniciales en la tabla EVENTOS
INSERT INTO EVENTOS (dni_usuario, nombre, tipo, dia, completado) 
VALUES	('12345678A', 'Evento1', 'Cita médica', '2024-07-01', TRUE),
		('23456789B', 'Evento2', 'Revisión', '2024-07-02', FALSE),
		('34567890C', 'Evento3', 'Vacunación', '2024-07-03', TRUE);

-- Introducimos unos valores iniciales en la tabla ADMINISTRATIVOS
INSERT INTO ADMINISTRATIVOS (usuario, nombre, apellidos, contrasena) 
VALUES	('admin1', 'Pedro', 'Gómez Pérez', 'admin1pass'),
		('admin2', 'Laura', 'Hernández Ruiz', 'admin2pass'),
		('admin3', 'Marta', 'Díaz Fernández', 'admin3pass');

-- Introducimos unos valores iniciales en la tabla OPERADORES
INSERT INTO OPERADORES (usuario, nombre, apellidos, contrasena, activo, ocupado) 
VALUES	('oper1', 'Jorge', 'López García', 'oper1pass', FALSE, FALSE),
		('oper2', 'Sara', 'Martínez Sánchez', 'oper2pass', FALSE, FALSE),
		('oper3', 'Raúl', 'González Díaz', 'oper3pass', FALSE, FALSE);

-- Introducimos unos valores iniciales en la tabla  INCIDENCIAS
INSERT INTO INCIDENCIAS (dni_usuario, operador, descripcion, procedimiento, resuelta, fecha, hora) 
VALUES	('12345678A', 'oper1', 'Incidencia1', 'Procedimiento1', FALSE, '2024-07-01', '08:00:00'),
		('23456789B', 'oper2', 'Incidencia2', 'Procedimiento2', TRUE, '2024-07-02', '09:00:00'),
		('34567890C', 'oper3', 'Incidencia3', 'Procedimiento3', FALSE, '2024-07-03', '10:00:00');


