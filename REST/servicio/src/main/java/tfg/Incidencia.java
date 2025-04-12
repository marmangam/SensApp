/**
 * Esta clase representa los elementos de la tabla INCIDENCIAS de la base de datos.
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

public class Incidencia {
    private long id; 
    private String dni_usuario;
    private String operador;
    private boolean resuelta;
    private Date fecha;
    private Time hora;
   
    private String descripcion;
    private String procedimiento;

    public Incidencia(long id, String dni_usuario, String operador, boolean resuelta, Date fecha, Time hora, String descripcion, String procedimiento) {
	this.id = id;
    this.dni_usuario = dni_usuario;
    this.operador = operador;
    this.resuelta = resuelta;
    this.fecha = fecha;
    this.hora = hora;
    this.descripcion = descripcion;
    this.procedimiento = procedimiento;
    }

    public Incidencia() {
       
    }
    
   
    public long getId() {
        return id;
    }
    @JsonProperty
    public void setId(long id) {
        this.id = id;
    }

    public String getDni_usuario() {
        return dni_usuario;
    }
    @JsonProperty
    public void setDni_usuario(String dni_usuario) {
        this.dni_usuario = dni_usuario;
    }

    public String getOperador() {
        return operador;
    }
    @JsonProperty
    public void setOperador(String operador) {
        this.operador = operador;
    }

    public boolean getResuelta() {
        return resuelta;
    }
    @JsonProperty
    public void setResuelta(boolean resuelta) {
        this.resuelta = resuelta;
    }

    public Date getFecha() {
        return fecha;
    }
    @JsonProperty
    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public Time getHora() {
        return hora;
    }
    @JsonProperty
    public void setHora(Time hora) {
        this.hora = hora;
    }

    public String getDescripcion() {
        return descripcion;
    }
    @JsonProperty
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getProcedimiento() {
        return procedimiento;
    }
    @JsonProperty
    public void setProcedimiento(String procedimiento) {
        this.procedimiento = procedimiento;
    }

}
