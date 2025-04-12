/**
 * Esta clase representa los elementos de la tabla ADMINISTRATIVOS de la base de datos.
 *
 * Métodos:
 * - Constructor con parámetros para inicializar todos los atributos.
 * - Constructor sin parámetros.
 * - Getters y setters para cada uno de los atributos de la clase.
 */

package com.example.apptrabajadores;

public class Administrativo {
    private String usuario;
    private String nombre;
    private String apellidos;
    private String contrasena;

    public Administrativo(String usuario, String nombre, String apellidos, String contrasena) {
        this.usuario= usuario;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.contrasena = contrasena;
    }

    public Administrativo() {

    }

    //USUARIO
    public String getUsuario() {
        return usuario;
    }
    public void setUsuario(String usuario){
        this.usuario = usuario;
    }


    //NOMBRE
    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre){
        this.nombre = nombre;
    }


    //APELLIDOS
    public String getApellidos() {
        return apellidos;
    }
    public void setApellidos(String apellidos){
        this.apellidos = apellidos;
    }


    //CONTRASEÑA
    public String getContrasena() {
        return contrasena;
    }
    public void setContrasena(String contrasena){
        this.contrasena = contrasena;
    }
}