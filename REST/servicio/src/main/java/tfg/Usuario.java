/**
 * Esta clase representa los elementos de la tabla USUARIOS de la base de datos.
 * 
 * Métodos:
 * - Constructor con parámetros para inicializar todos los atributos.
 * - Constructor sin parámetros.
 * - Getters y setters para cada uno de los atributos de la clase.
 */


package tfg;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.sql.Date;

public class Usuario {
    private String nombre;
    private String apellidos;
    private Date fecha_nacimiento;
    private String domicilio;
    private String enfermedades_previas;
    private String alergias;
    private String dni;
    private int telefono;
    private String contrasena;


    public Usuario(String nombre, String apellidos, Date fecha_nacimiento, String domicilio,
    String enfermedades_previas, String alergias, String dni, int telefono, String contrasena) {
    this.nombre = nombre;
    this.apellidos = apellidos;
    this.fecha_nacimiento = fecha_nacimiento;
    this.domicilio = domicilio;
    this.enfermedades_previas = enfermedades_previas;
    this.alergias = alergias;
    this.dni = dni;
    this.telefono = telefono;
    this.contrasena = contrasena;
    }

    public Usuario() {
       
    }
    
   
    
    public String getNombre() {
        return nombre;
    }
    @JsonProperty
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }
    @JsonProperty
    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public Date getFecha_nacimiento() {
        return fecha_nacimiento;
    }
    @JsonProperty
    public void setFecha_nacimiento(Date fecha_nacimiento) {
        this.fecha_nacimiento = fecha_nacimiento;
    }

    public String getDomicilio() {
        return domicilio;
    }
    @JsonProperty
    public void setDomicilio(String domicilio) {
        this.domicilio = domicilio;
    }

    public String getEnfermedades_previas() {
        return enfermedades_previas;
    }
    @JsonProperty
    public void setEnfermedades_previas(String enfermedades_previas) {
        this.enfermedades_previas = enfermedades_previas;
    }

    public String getAlergias() {
        return alergias;
    }
    @JsonProperty
    public void setAlergias(String alergias) {
        this.alergias = alergias;
    }

    public String getDni() {
        return dni;
    }
    @JsonProperty
    public void setDni(String dni) {
        this.dni = dni;
    }

    public int getTelefono() {
        return telefono;
    }
    @JsonProperty
    public void setTelefono(int telefono) {
        this.telefono = telefono;
    }

    public String getContrasena() {
        return contrasena;
    }
    @JsonProperty
    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

}
