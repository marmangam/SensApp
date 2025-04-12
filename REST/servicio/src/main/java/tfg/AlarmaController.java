package tfg;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.*;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.sql.*;
import org.json.JSONObject;

/**
 * Clase controladora para la gestión de ALARMAS.
 * 
 * Proporciona endpoints REST para crear, obtener, actualizar y eliminar alarmas.
 * 
 * Métodos:
 * - creaAlarma: Crea una nueva alarma a partir del body de la petición.
 * - obtenerAlarmas: Obtiene una lista de todas las alarmas de un usuario con dni dado por parámetros.
 * - getAlarma: Obtiene una alarma a partir del dni y del nombre de la alarma dado por parámetros.
 * - updateAlarma: Actualiza una alarma con dni y nombre dados por parámetros a partir del body de la petición.
 * - deleteAlarma: Elimina una alarma a partir del dni y del nombre.
 *
 */

@RestController
public class AlarmaController {
    //Se definen las variables para las conexiones a la base de datos
    private static final String dbUser ="sensapp";
    private static final String dbPassword = "sensapptfg66&";
    private static final String dbUrl = "jdbc:postgresql://database-sensapp.cz4i4mewyc6r.us-east-1.rds.amazonaws.com:5432/databasesensapp";
 



    // Crea una nueva alarma
    // Método HTTP: POST 
    // URL: http://localhost:8080/alarma
    // Body: Alarma a crear
	@ApiOperation(value = "Crea una nueva alarma", notes = "Este endpoint permite crear una nueva alarma a partir del body de la petición")
	@PostMapping(path = "/alarma")
    public ResponseEntity<String> creaAlarma(@RequestBody Alarma nuevaAlarma){
	
		int resultadoExe=0;
		ResponseEntity<String> respuesta = null;
		Connection conn = null;
		PreparedStatement ps = null;
		
		try{
			//Acceso a la BBDD
			Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection(dbUrl, dbUser,dbPassword);
			//Se crea la nueva alarma en la BBDD
			ps = conn.prepareStatement("INSERT INTO alarmas (nombre, tipo, dni_usuario, completado, hora) VALUES (?, ?, ?, ?, ?)");
			ps.setString(1, nuevaAlarma.getNombre());
			ps.setString(2, nuevaAlarma.getTipo());
			ps.setString(3, nuevaAlarma.getDni_usuario());
			ps.setDate(4, nuevaAlarma.getCompletado());
			ps.setTime(5, nuevaAlarma.getHora());	

			resultadoExe=ps.executeUpdate();
	
			
		} catch (SQLException | ClassNotFoundException e){ //Manejo de excepciones
			System.out.println("alarmaController: Excepción:" + e.getMessage());
		} finally {
			try {
				if ( ps != null ) ps.close();
				if ( conn != null ) conn.close(); 
				} catch (SQLException e){
					System.out.println("alarmaController: Excepción SQLException cerrando recursos:" + e.getMessage());
			}
		}

		//Por compatibilidad con las aplicaciones móviles desarrolladas se devuelve un objeto JSON
		// convertido a string en lugar de un string directamente, tanto en caso de éxito como en caso de error
		JSONObject json = new JSONObject();
		if (resultadoExe >= 1){
			json.put("message", "Insertado");
			respuesta = new ResponseEntity<>(json.toString(), HttpStatus.CREATED);
			System.out.println("Se ha insertado la alarma con resultado: " + resultadoExe);
	
		}
		else{	
			json.put("message", "No insertado");
			respuesta = new ResponseEntity<>(json.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return respuesta; 
    }




	// Obtiene una lista de alarmas con dni_usuario pasado por parámetros
    // Método HTTP: GET
    // URL: http://localhost:8080/alarma/lista?dni_usuario=dni
    // Body:
	@ApiOperation(value = "Obtiene una lista de alarmas con dni_usuario pasado por parámetros", notes = "Este endpoint permite obtener una lista de todas las alarmas de un usuario con dni dado por parámetros")
    @GetMapping("/alarma/lista")
    public List<Alarma> obtenerAlarmas(@RequestParam String dni_usuario, @RequestParam(defaultValue = "0") int offset, @RequestParam(defaultValue = "100") int limit) {

		Connection conn = null;
		PreparedStatement ps = null;
		List<Alarma> alarmas = new ArrayList<>();

		try {
			// Acceso a la BBDD
			Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
			// Se buscan las alarmas en la BBDD
			ps = conn.prepareStatement("SELECT * FROM alarmas WHERE dni_usuario =? ORDER BY hora ASC LIMIT ? OFFSET ?");
			ps = conn.prepareStatement("SELECT * FROM alarmas WHERE dni_usuario = ? ORDER BY " +
				"  CASE " +
				"    WHEN completado <> CURRENT_DATE AND hora <= CURRENT_TIME THEN 0 " +
				"    WHEN completado <> CURRENT_DATE AND hora > CURRENT_TIME THEN 1 " +
				"    ELSE 2 " +
				"  END, " +
				"  hora ASC " +
				"LIMIT ? OFFSET ?"
			);
			ps.setString(1, dni_usuario);
			ps.setInt(2, limit);    
        	ps.setInt(3, offset);
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {//Añadimos a la lista las que se hayan encontrado
				Alarma alarma = new Alarma(rs.getString("dni_usuario"),rs.getString("nombre"), 
				rs.getString("tipo"), rs.getDate("completado"), rs.getTime("hora"));
				alarmas.add(alarma);
			}

		} catch (SQLException | ClassNotFoundException e){ //Manejo de excepciones
		System.out.println("alarmaController: Excepción: " + e.getMessage());
		} finally {
			try {
				if ( ps != null ) ps.close();
				if ( conn != null ) conn.close(); 
			} catch (SQLException e){
				System.out.println("alarmaController: Excepción SQLException cerrando recursos: " + e.getMessage());
			}
		}

		if (!alarmas.isEmpty()){
			System.out.println("Encontradas alarmas.");
		} else{
			System.out.println("No se ha encontrado ninguna alarma" );
		}
 


		return alarmas; //Se devuelve la lista de alarmas solicitada
}


	// Obtiene una alarma con el nombre y dni del usuario dados por parámetros
    // Método HTTP: GET
    // URL: http://localhost:8080/alarma?nombre=nom1&dni_usuario=dni
    // Body:
	@ApiOperation(value = "Obtiene una alarma con el nombre y dni del usuario dados por parámetros", notes = "Este endpoint permite obtener una alarma a partir del dni y del nombre de la alarma dado por parámetros")
    @GetMapping("/alarma")
    public ResponseEntity<Alarma> getAlarma(@RequestParam String nombre, @RequestParam String dni_usuario) {
	
		Connection conn = null;
		PreparedStatement ps = null;
		Alarma alarma = null;
		ResponseEntity<Alarma> respuesta = null;

		try {
			// Acceso a la BBDD
			Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
			// Se busca en la BBDD la alarma con el nombre y dni_usuario dados
			ps = conn.prepareStatement("SELECT * FROM alarmas WHERE nombre=? AND dni_usuario=?");
			ps.setString(1, nombre);
			ps.setString(2, dni_usuario);
			ResultSet rs = ps.executeQuery();

			if (rs.next()) { //Comprobamos si se ha encontrado
				alarma = new Alarma(rs.getString("dni_usuario"),rs.getString("nombre"),
				 rs.getString("tipo"), rs.getDate("completado"), rs.getTime("hora"));
			}

		} catch (SQLException | ClassNotFoundException e){ //Manejo de excepciones
			System.out.println("alarmaController: Excepción:" + e.getMessage());
		} finally {
			try {
				if ( ps != null ) ps.close();
				if ( conn != null ) conn.close(); 
				} catch (SQLException e){
					System.out.println("alarmaController: Excepción SQLException cerrando recursos:" + e.getMessage());
			}
		}

		if (alarma != null) {//Se devuelve la alarma solicitada o un error
			System.out.println("Encontrada la alarma con nombre: " + nombre + " y dni del usuario: "+ dni_usuario);
			respuesta=  new ResponseEntity<>(alarma, HttpStatus.OK);
		} else{
			respuesta=  new ResponseEntity<>( HttpStatus.INTERNAL_SERVER_ERROR);    
		}

		return respuesta;

    }

 
    // Actualiza una alarma con nombre y dni_usuario dado por parámetros
    // Método HTTP: PUT
    // URL: http:localhost:8080/alarma?nombre=nom&dni_usuario=dni
    // Body: Datos nuevos de la alarma
	@ApiOperation(value = "Actualiza una alarma con nombre y dni_usuario dado por parámetros", notes = "Este endpoint permite actualizar una alarma con dni y nombre dados por parámetros a partir del body de la petición")
    @PutMapping("/alarma")
    public ResponseEntity<String> updateAlarma(@RequestParam String nombre, @RequestParam String dni_usuario, @RequestBody Alarma nuevaAlarma) {
	
		Connection conn = null;
		PreparedStatement ps = null;
		int resultado = 0;
		ResponseEntity<String> respuesta = null;


		try {
			// Acceso a la BBDD
			Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
			// Se actualiza la alarma correspondiente al nombre y dni en la BBDD
			ps = conn.prepareStatement("UPDATE alarmas SET nombre = ?, tipo = ?, dni_usuario = ?, completado = ?, hora = ? WHERE nombre = ? AND dni_usuario = ?");
			ps.setString(1, nuevaAlarma.getNombre());
			ps.setString(2, nuevaAlarma.getTipo());
			ps.setString(3, nuevaAlarma.getDni_usuario());
			ps.setDate(4, nuevaAlarma.getCompletado());
			ps.setTime(5, nuevaAlarma.getHora());
			ps.setString(6, nombre);
			ps.setString(7, dni_usuario);
			resultado = ps.executeUpdate();
			
		}catch (SQLException | ClassNotFoundException e){ //Manejo de excepciones
			System.out.println("alarmaController: Excepción:" + e.getMessage());
		} finally {
			try {
				if ( ps != null ) ps.close();
				if ( conn != null ) conn.close(); 
				} catch (SQLException e){
					System.out.println("alarmaController: Excepción SQLException cerrando recursos:" + e.getMessage());
			}
		}
		
		//Por compatibilidad con las aplicaciones móviles desarrolladas se devuelve un objeto JSON
		// convertido a string en lugar de un string directamente, tanto en caso de éxito como en caso de error
		JSONObject json = new JSONObject();
		if (resultado >= 1) {
			json.put("message", "Actualizado");
			System.out.println("Actualizada la alarma con nombre " + nombre + " y dni del usuario: " + dni_usuario);
			respuesta =  new ResponseEntity<>(json.toString(), HttpStatus.OK);
		} else {
			json.put("message", "No actualizado");
			System.out.println("No se ha podido actualizar la alarma con nombre " + nombre + " y dni del usuario: " + dni_usuario);
			respuesta = new ResponseEntity<>(json.toString(), HttpStatus.NOT_MODIFIED);
		}

		return respuesta;
    }




	// Elimina una alarma dado su nombre y dni_usuario
	// Metodo HTTP: DELETE 
	// Ejemplo URI: http://localhost:8080/alarma?nombre=nom&dni_usuario=dni
	@ApiOperation(value = "Elimina una alarma dado su nombre y dni_usuario", notes = "Este endpoint permite eliminar una alarma a partir del dni y del nombre")
	@DeleteMapping("/alarma")
	public ResponseEntity<String> 
		deleteAlarma(@RequestParam String nombre, @RequestParam String dni_usuario) {

		ResponseEntity<String> resultado = null;
		Connection conn = null;
		PreparedStatement ps = null;
		int numRegBorrados = 0;
		
		try{
			// Acceso a la BBDD
			Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
			// Borrado de la  alarma
			ps = conn.prepareStatement("DELETE FROM alarmas  WHERE nombre = ? AND dni_usuario = ?");
			ps.setString(1, nombre);
			ps.setString(2, dni_usuario);
			numRegBorrados = ps.executeUpdate();

		} catch (SQLException | ClassNotFoundException e){ //Manejo de excepciones
		System.out.println("alarmaController: Excepción:" + e.getMessage());
		} finally {
			try{
				if (ps != null) ps.close();
				if (conn != null) conn.close(); 
			} catch (SQLException e){
				System.out.println("alarmaController: Excepción SQLException cerrando recursos:" + e.getMessage());
			}
		}

		//Por compatibilidad con las aplicaciones móviles desarrolladas se devuelve un objeto JSON
		// convertido a string en lugar de un string directamente, tanto en caso de éxito como en caso de error
		JSONObject json = new JSONObject();
		if (numRegBorrados >= 1){
			json.put("message", "Eliminado");
			System.out.println("Eliminada la alarma con  nombre:  " + nombre + " y dni del usuario: " + dni_usuario);
			resultado = new ResponseEntity<>(json.toString(), HttpStatus.OK);
		}
		else {
			json.put("message", "No eliminado");
			System.out.println("NO eliminada la alarma con  nombre:  " + nombre + " y dni del usuario: " + dni_usuario);
			resultado = new ResponseEntity<>(json.toString(),HttpStatus.NOT_FOUND);
		}

		return resultado;
	}


}
