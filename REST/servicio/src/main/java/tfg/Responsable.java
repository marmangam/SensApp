/**
 * Esta clase representa los elementos de la tabla RESPONSABLES de la base de datos.
 * 
 * Métodos:
 * - Constructor con parámetros para inicializar todos los atributos.
 * - Constructor sin parámetros.
 * - Getters y setters para cada uno de los atributos de la clase.
 */


package tfg;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Responsable {
    private String usuario;
    private String nombre;
    private String apellidos;
    private int telefono;
    private String dni;
    private String dni_usuario;
    private String contrasena;

   
    public Responsable(String usuario, String nombre, String apellidos, int telefono, String dni, String dni_usuario,
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
    
   
    public String getUsuario() {
        return usuario;
    }
    @JsonProperty
    public void setUsuario(String usuario) {
        this.usuario = usuario;
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

    public int getTelefono() {
        return telefono;
    }
    @JsonProperty
    public void setTelefono(int telefono) {
        this.telefono = telefono;
    }

    public String getDni() {
        return dni;
    }
    @JsonProperty
    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getDni_usuario() {
        return dni_usuario;
    }
    @JsonProperty
    public void setDni_usuario(String dni_usuario) {
        this.dni_usuario = dni_usuario;
    }

    public String getContrasena() {
        return contrasena;
    }
    @JsonProperty
    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

   
}
