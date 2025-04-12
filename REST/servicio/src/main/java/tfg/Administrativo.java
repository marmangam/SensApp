/**
 * Esta clase representa los elementos de la tabla ADMINISTRATIVOS de la base de datos.
 * 
 * Métodos:
 * - Constructor con parámetros para inicializar todos los atributos.
 * - Constructor sin parámetros.
 * - Getters y setters para cada uno de los atributos de la clase.
 */


package tfg;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Administrativo {
    private String usuario;
    private String nombre;
    private String apellidos;
	private String contrasena;

    public Administrativo(String usuario, String nombre, String apellidos, String contrasena) {
	this.usuario= usuario;
	this.nombre = nombre;	
	this.apellidos = apellidos;
	this.contrasena = contrasena;
    }

    public Administrativo() {
       
    }
    
   
    public String getUsuario() {
	return usuario;
    }
    @JsonProperty
    public void setUsuario(String usuario){
	this.usuario = usuario;
    }

	public String getNombre() {
	return nombre;
    }
    @JsonProperty
    public void setNombre(String nombre){
	this.nombre = nombre;
    }

 	public String getApellidos() {
	return apellidos;
    }
    @JsonProperty
    public void setApellidos(String apellidos){
	this.apellidos = apellidos;
    }

  	public String getContrasena() {
	return contrasena;
    }
    @JsonProperty
    public void setContrasena(String contrasena){
	this.contrasena = contrasena;
    }
}
