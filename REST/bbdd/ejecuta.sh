#! /bin/sh

## Script para iniciar y configurar la base de datos del proyecto.
## Se borra la base de datos existente y se crea una nueva. Luego se ejecutan dos
## scripts .sql para insertar tablas y datos iniciales.


## Borramos la base de datos
echo Borramos la base de datos
dropdb -U dit tfg

## Creamos la base de datos
echo Creamos la base de datos
createdb -U dit tfg

## Ejecutamos el script para insertar las tablas en la base de datos
psql -U dit -d tfg -f sentencias.sql -a

## Ejecutamos el script para insertar los datos iniciales en las tablas
psql -U dit -d tfg -f sentenciasInsert.sql -a 
