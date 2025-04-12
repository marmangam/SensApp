/**
 * Esta clase representa los elementos de la tabla EVENTOS de la base de datos.
 * 
 * Métodos:
 * - Constructor con parámetros para inicializar todos los atributos.
 * - Constructor sin parámetros.
 * - Getters y setters para cada uno de los atributos de la clase.
 */


package tfg;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.sql.Date;

public class Evento {
    private String dni_usuario;
    private String nombre;
    private String tipo;
    private boolean completado;
	private Date dia;

    public Evento(String dni_usuario, String nombre, String tipo, boolean completado, Date dia) {
	this.dni_usuario = dni_usuario;
	this.nombre = nombre;	
	this.tipo = tipo;
	this.completado = completado;
	this.dia = dia;
    }

    public Evento() {
       
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


    public boolean getCompletado() {
    return completado;
    }
    @JsonProperty
    public void setCompletado(Boolean completado ){
    this.completado = completado;
    }
    
    public Date getDia() {
    return dia;
    }
    @JsonProperty
    public void setDia(Date dia){
    this.dia = dia;
    }

}
