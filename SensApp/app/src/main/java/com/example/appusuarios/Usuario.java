/**
 * Esta clase representa un usuario en la aplicación.
 *
 * Métodos:
 * - Constructor con parámetros para inicializar todos los atributos.
 * - Constructor sin parámetros.
 * - Getters y setters para cada uno de los atributos de la clase.
 */

package com.example.appusuarios;

public class Usuario {
    private String dni;
    private String contrasena;


    public Usuario(String dni, String contrasena) {
        this.dni = dni;
        this.contrasena = contrasena;
    }

    public Usuario() {

    }


    //DNI
    public String getDni() {
        return dni;
    }
    public void setDni(String dni) {
        this.dni = dni;
    }


    //CONTRASENA
    public String getContrasena() {
        return contrasena;
    }
    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

}