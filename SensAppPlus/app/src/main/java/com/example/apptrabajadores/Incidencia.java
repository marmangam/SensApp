/**
 * Esta clase representa una incidencia en la aplicación.
 *
 * Métodos:
 * - Constructor con parámetros para inicializar todos los atributos.
 * - Constructor sin parámetros.
 * - Getters y setters para cada uno de los atributos de la clase.
 */

package com.example.apptrabajadores;

// Imports
import java.time.LocalDate;
import java.time.LocalTime;

public class Incidencia {
    private long id;
    private String dni_usuario;
    private String operador;
    private String descripcion;
    private String procedimiento;
    private Boolean resuelta;
    private LocalDate fecha;
    private LocalTime hora;



    public Incidencia(long id, String dni_usuario, String operador, String descripcion, String procedimiento, boolean resuelta, LocalDate fecha, LocalTime hora) {
        this.id = id;
        this.dni_usuario = dni_usuario;
        this.operador = operador;
        this.descripcion = descripcion;
        this.procedimiento = procedimiento;
        this.resuelta = resuelta;
        this.fecha = fecha;
        this.hora = hora;
    }

    public Incidencia() {

    }

    //ID
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }

    //DNI_USUARIO
    public String getDni_usuario() {
        return dni_usuario;
    }
    public void setDni_usuario(String dni_usuario) {
        this.dni_usuario = dni_usuario;
    }

    //OPERADOR
    public String getOperador() {
        return operador;
    }
    public void setOperador(String operador) {
        this.operador = operador;
    }

    //DESCRIPCIÓN
    public String getDescripcion() {
        return descripcion;
    }
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    //PROCEDIMIENTO
    public String getProcedimiento() {
        return procedimiento;
    }
    public void setProcedimiento(String procedimiento) {
        this.procedimiento = procedimiento;
    }

    //RESUELTA
    public boolean getResuelta() {
        return resuelta;
    }
    public void setResuelta(boolean resuelta) {
        this.resuelta = resuelta;
    }

    //FECHA
    public LocalDate getFecha() {
        return fecha;
    }
    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    //HORA
    public LocalTime getHora() {
        return hora;
    }
    public void setHora(LocalTime hora) {
        this.hora = hora;
    }

}
