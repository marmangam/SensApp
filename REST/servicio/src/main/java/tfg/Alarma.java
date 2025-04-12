/**
 * Esta clase representa los elementos de la tabla ALARMAS de la base de datos.
 * 
 * Métodos:
 * - Constructor con parámetros para inicializar todos los atributos.
 * - Constructor sin parámetros.
 * - Getters y setters para cada uno de los atributos de la clase.
 */


package tfg;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.sql.Time;
import java.sql.Date;

public class Alarma {
    private String dni_usuario;
    private String nombre;
    private String tipo;
    private Date completado;
	private Time hora;

    public Alarma(String dni_usuario, String nombre, String tipo, Date completado, Time hora) {
	this.dni_usuario = dni_usuario;
	this.nombre = nombre;	
	this.tipo = tipo;
	this.completado = completado;
	this.hora = hora;
    }

    public Alarma() {
       
    }
    
   
    public String getDni_usuario() {
	return dni_usuario;
    }
    @JsonProperty
    public void setDni_usuario(String dni_usuario){
	this.dni_usuario = dni_usuario;
    }

    public String getNombre() {
	return nombre;
    }
    @JsonProperty
    public void setNombre(String nombre){
	this.nombre = nombre;
    }

    public String getTipo() {
    return tipo;
    }
    @JsonProperty
    public void setTipo(String tipo){
    this.tipo = tipo;
    }


    public Date getCompletado() {
    return completado;
    }
    @JsonProperty
    public void setCompletado(Date completado ){
    this.completado = completado;
    }
    
    public Time getHora() {
    return hora;
    }
    @JsonProperty
    public void setHora(Time hora){
    this.hora = hora;
    }

}
