/**
 * Esta clase representa los elementos de la tabla OPERADORES de la base de datos.
 * 
 * Métodos:
 * - Constructor con parámetros para inicializar todos los atributos.
 * - Constructor sin parámetros.
 * - Getters y setters para cada uno de los atributos de la clase.
 */


package tfg;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Operador {
    private String usuario;
    private String nombre;
    private String apellidos;
	private String contrasena;
	private boolean activo;
	private boolean ocupado;

    public Operador(String usuario, String nombre, String apellidos, String contrasena, boolean activo, boolean ocupado) {
	this.usuario= usuario;
	this.nombre = nombre;	
	this.apellidos = apellidos;
	this.contrasena = contrasena;
	this.activo = activo;
	this.ocupado = ocupado;
    }

    public Operador() {
       
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


    public boolean getActivo() {
    return activo;
    }
    @JsonProperty
    public void setActivo(Boolean activo ){
    this.activo = activo;
    }


    public boolean getOcupado() {
    return ocupado;
    }
    @JsonProperty
    public void setOcupado(Boolean ocupado ){
    this.ocupado = ocupado;
    }
}
