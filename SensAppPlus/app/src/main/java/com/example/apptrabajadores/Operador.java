/**
 * Esta clase representa los elementos de la tabla OPERADORES de la base de datos.
 *
 * Métodos:
 * - Constructor con parámetros para inicializar todos los atributos.
 * - Constructor sin parámetros.
 * - Getters y setters para cada uno de los atributos de la clase.
 */

package com.example.apptrabajadores;


public class Operador {
    private String usuario;
    private String nombre;
    private String apellidos;
    private String contrasena;
    private Boolean activo;
    private Boolean ocupado;

    public Operador(String usuario, String nombre, String apellidos, String contrasena, Boolean activo, Boolean ocupado) {
        this.usuario= usuario;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.contrasena = contrasena;
        this.activo = activo;
        this.ocupado = ocupado;
    }

    public Operador() {

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

    //ACTIVO
    public Boolean getActivo() {
        return activo;
    }
    public void setActivo(Boolean activo){
        this.activo = activo;
    }

    //OCUPAOD
    public Boolean getOcupado() {
        return ocupado;
    }
    public void setOcupado(Boolean ocupado){
        this.ocupado = ocupado;
    }
}