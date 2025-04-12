/**
 * Esta clase representa un responsable en la aplicación
 *
 * Métodos:
 * - Constructor con parámetros para inicializar todos los atributos.
 * - Constructor sin parámetros.
 * - Getters y setters para cada uno de los atributos de la clase.
 */

package com.example.apptrabajadores;

public class Responsable {
    private String usuario;
    private String nombre;
    private String apellidos;
    private int telefono;
    private String dni;
    private String dni_usuario;
    private String contrasena;


    public Responsable(String usuario, String nombre, String apellidos, String dni, String dni_usuario, int telefono,
                       String contrasena) {
        this.usuario = usuario;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.telefono = telefono;
        this.dni = dni;
        this.dni_usuario = dni_usuario;
        this.contrasena = contrasena;
    }

    public Responsable() {

    }

    //USUARIO
    public String getUsuario() {
        return usuario;
    }
    public void setUsuario(String usuario) {
        this.usuario = usuario;
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

    //TELEFONO
    public int getTelefono() {
        return telefono;
    }
    public void setTelefono(int telefono) {
        this.telefono = telefono;
    }

    //DNI
    public String getDni() {
        return dni;
    }
    public void setDni(String dni) {
        this.dni = dni;
    }

    //DNI_USUARIO
    public String getDni_usuario() {
        return dni_usuario;
    }
    public void setDni_usuario(String dni_usuario) {
        this.dni_usuario = dni_usuario;
    }

    //CONTRASEÑA
    public String getContrasena() {
        return contrasena;
    }
    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }


}