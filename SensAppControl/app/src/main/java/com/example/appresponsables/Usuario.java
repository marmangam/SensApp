/**
 * Esta clase representa un usuario en la aplicación.
 *
 * Métodos:
 * - Constructor con parámetros para inicializar todos los atributos.
 * - Constructor sin parámetros.
 * - Getters y setters para cada uno de los atributos de la clase.
 */


package com.example.appresponsables;

import java.time.LocalDate;
public class Usuario {
    private String nombre;
    private String apellidos;
    private LocalDate fecha_nacimiento;
    private String domicilio;
    private String enfermedades_previas;
    private String alergias;
    private String dni;
    private int telefono;
    private String contrasena;


    public Usuario(String nombre, String apellidos, LocalDate fecha_nacimiento, String domicilio,
                   String enfermedades_previas, String alergias, String dni, int telefono, String contrasena) {
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.fecha_nacimiento = fecha_nacimiento;
        this.domicilio = domicilio;
        this.enfermedades_previas = enfermedades_previas;
        this.alergias = alergias;
        this.dni = dni;
        this.telefono = telefono;
        this.contrasena = contrasena;
    }

    public Usuario() {

    }


    //NOMBRE
    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    //APELLIDOS
    public String getApellidos() {
        return apellidos;
    }
    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    //FECHA_NACIMIENTO
    public LocalDate getFecha_nacimiento() {
        return fecha_nacimiento;
    }
    public void setFecha_nacimiento(LocalDate fecha_nacimiento) {
        this.fecha_nacimiento = fecha_nacimiento;
    }

    //DOMICILIO
    public String getDomicilio() {
        return domicilio;
    }
    public void setDomicilio(String domicilio) {
        this.domicilio = domicilio;
    }

    //ENFERMEDADES_PREVIAS
    public String getEnfermedades_previas() {
        return enfermedades_previas;
    }
    public void setEnfermedades_previas(String enfermedades_previas) {
        this.enfermedades_previas = enfermedades_previas;
    }

    //ALERGIAS
    public String getAlergias() {
        return alergias;
    }
    public void setAlergias(String alergias) {
        this.alergias = alergias;
    }

    //DNI
    public String getDni() {
        return dni;
    }
    public void setDni(String dni) {
        this.dni = dni;
    }

    //TELÉFONO
    public int getTelefono() {
        return telefono;
    }
    public void setTelefono(int telefono) {
        this.telefono = telefono;
    }


    //CONTRASENA
    public String getContrasena() {
        return contrasena;
    }
    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

}