/**
 * Esta clase representa una alarma en la aplicación.
 *
 * Métodos:
 * - Constructor con parámetros para inicializar todos los atributos.
 * - Constructor sin parámetros.
 * - Getters y setters para cada uno de los atributos de la clase.
 */

package com.example.appusuarios;

import java.time.LocalTime;
import java.time.LocalDate;

public class Alarma {

    private String dni_usuario;
    private String nombre;
    private String tipo;
    private LocalTime hora;
    private LocalDate completado;

    public Alarma(String dni_usuario, String nombre, String tipo, LocalTime hora, LocalDate completado){
        this.dni_usuario = dni_usuario;
        this.nombre = nombre;
        this.tipo = tipo;
        this.hora = hora;
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

    //HORA
    public LocalTime getHora() {
        return hora;
    }
    public void setHora(LocalTime hora) {
        this.hora = hora;
    }

    //COMPLETADO
    public LocalDate getCompletado() {
        return completado;
    }
    public void setCompletado(LocalDate completado) {
        this.completado = completado;
    }
}
