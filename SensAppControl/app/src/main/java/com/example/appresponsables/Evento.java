/**
 * Esta clase representa un evento en la aplicación.
 *
 * Métodos:
 * - Constructor con parámetros para inicializar todos los atributos.
 * - Constructor sin parámetros.
 * - Getters y setters para cada uno de los atributos de la clase.
 */

package com.example.appresponsables;

import java.time.LocalDate;

public class Evento {

    private String dni_usuario;
    private String nombre;
    private String tipo;
    private LocalDate dia;
    private Boolean completado;

    public Evento(String dni_usuario, String nombre, String tipo, LocalDate dia, Boolean completado){
        this.dni_usuario = dni_usuario;
        this.nombre = nombre;
        this.tipo = tipo;
        this.dia = dia;
        this.completado = completado;
    }

    //DNI_USUARIO
    public String getDni_usuario() {
        return dni_usuario;
    }
    public void setDni_usuario(String dni_usuario) {
        this.dni_usuario = dni_usuario;
    }

    //NOMBRE
    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    //TIPO
    public String getTipo() {
        return tipo;
    }
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    //DIA
    public LocalDate getDia() {
        return dia;
    }
    public void setDia(LocalDate dia) {
        this.dia = dia;
    }

    //COMPLETADO
    public Boolean getCompletado() {
        return completado;
    }
    public void setCompletado(Boolean completado) {
        this.completado = completado;
    }
}
