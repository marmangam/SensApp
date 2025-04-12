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
 * Clase controladora para la gestión de EVENTOS.
 * 
 * Proporciona endpoints REST para crear, obtener, actualizar y eliminar eventos.
 * 
 * Métodos:
 * - creaEvento: Crea un nuevo evento a partir del body de la petición.
 * - obtenerEventos: Obtiene una lista de todos los eventos de un usuario con dni dado por parámetros.
 * - getEvento: Obtiene un evento a partir del dni y del nombre del evento dado por parámetros.
 * - updateEvento: Actualiza un evento con dni y nombre dados por parámetros a partir del body de la petición.
 * - deleteEvento: Elimina un evento a partir del dni y del nombre.
 *
 */

@RestController
public class EventoController {
    //Se definen las variables para las conexiones a la base de datos
    private static final String dbUser ="sensapp";
    private static final String dbPassword = "sensapptfg66&";
    private static final String dbUrl = "jdbc:postgresql://database-sensapp.cz4i4mewyc6r.us-east-1.rds.amazonaws.com:5432/databasesensapp";
 



    // Crea un nuevo evento
    // Método HTTP: POST 
    // URL: http://localhost:8080/evento
    // Body: Evento a crear
	@ApiOperation(value = "Crea un nuevo evento", notes = "Este endpoint permite crear nuevo evento a partir del body de la petición")
    @PostMapping(path = "/evento")
    public ResponseEntity<String> creaEvento(@RequestBody Evento nuevoEvento){

		int resultadoExe=0;
		ResponseEntity<String> respuesta = null;
		Connection conn = null;
		PreparedStatement ps = null;

		try{
			//Acceso a la BBDD
			Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection(dbUrl, dbUser,dbPassword);
			//Se crea el nuevo evento en la BBDD
			ps = conn.prepareStatement("INSERT INTO eventos (nombre, tipo, dni_usuario, completado, dia) VALUES (?, ?, ?, ?, ?)");
			ps.setString(1, nuevoEvento.getNombre());
			ps.setString(2, nuevoEvento.getTipo());
			ps.setString(3, nuevoEvento.getDni_usuario());
			ps.setBoolean(4, nuevoEvento.getCompletado());
			ps.setDate(5, nuevoEvento.getDia());

			resultadoExe = ps.executeUpdate();

		} catch (SQLException | ClassNotFoundException e){ //Manejo de excepciones
			System.out.println("eventoController: Excepción: " + e.getMessage());
		} finally {
			try {
				if ( ps!= null ) ps.close();
				if ( conn != null ) conn.close(); 
				} catch (SQLException e){
					System.out.println("eventoController: Excepción SQLException cerrando recursos:" + e.getMessage());
			}
		}
		
		//Por compatibilidad con las aplicaciones móviles desarrolladas se devuelve un objeto JSON
		// convertido a string en lugar de un string directamente, tanto en caso de éxito como en caso de error
		JSONObject json = new JSONObject();
		if (resultadoExe >= 1){
			json.put("message", "Insertado");
			respuesta = new ResponseEntity<>(json.toString(), HttpStatus.CREATED);
			System.out.println("Se ha insertado el evento con resultado: " + resultadoExe);
		}
		else{	
			json.put("message", "No insertado");
			respuesta = new ResponseEntity<>(json.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return respuesta; 
    }




	// Obtiene una lista de eventos
    // Método HTTP: GET
    // URL: http://localhost:8080/evento/lista?dni_usuario=dni
    // Body:
	@ApiOperation(value = "Obtiene una lista de eventos", notes = "Este endpoint permite obtener una lista de todos los eventos de un usuario con dni dado por parámetros")
    @GetMapping("/evento/lista")
    public List<Evento> obtenerEventos(@RequestParam String dni_usuario, @RequestParam(defaultValue = "0") int offset, @RequestParam(defaultValue = "100") int limit) {
		
		Connection conn = null;
		PreparedStatement ps = null;
		List<Evento> eventos = new ArrayList<>();

		try {
			// Acceso a la BBDD
			Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
			// Se buscan los eventos en la BBDD
			ps = conn.prepareStatement("SELECT * FROM eventos WHERE dni_usuario =? ORDER BY dia ASC LIMIT ? OFFSET ?");
			ps.setString(1, dni_usuario);
			ps.setInt(2, limit);    
        	ps.setInt(3, offset);
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {//Añadimos a la lista los que se hayan encontrado
				Evento evento = new Evento(rs.getString("dni_usuario"),rs.getString("nombre"), 
				rs.getString("tipo"), rs.getBoolean("completado"), rs.getDate("dia"));
				eventos.add(evento);
			}
			
		} catch (SQLException | ClassNotFoundException e){ //manejo de excepciones
		System.out.println("eventoController: Excepción:" + e.getMessage());
		} finally {
			try {
				if ( ps != null ) ps.close();
				if ( conn != null ) conn.close(); 
			} catch (SQLException e){
				System.out.println("eventoController: Excepción SQLException cerrando recursos:" + e.getMessage());
			}
		}

		if (!eventos.isEmpty()){
			System.out.println("Encontrados eventos.");
		} else{
			System.out.println("No se ha encontrado ningun evento" );
		}

		return eventos; //Se devuelve la lista de eventos solicitada
}


	// Obtiene una evento con el nombre y dni del usuario
    // Método HTTP: GET
    // URL: http://localhost:8080/evento?nombre=nom1&dni_usuario=dni
    // Body:
	@ApiOperation(value = "Obtiene una evento con el nombre y dni del usuario", notes = "Este endpoint permite obtener un evento a partir del dni y del nombre del evento dado por parámetros")
    @GetMapping("/evento")
    public ResponseEntity<Evento> getEvento(@RequestParam String nombre, @RequestParam String dni_usuario) {
		
		Connection conn = null;
		PreparedStatement ps = null;
		Evento evento = null;
		ResponseEntity<Evento> respuesta = null;

		try {
			// Acceso a la BBDD
			Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
			// Se busca en la BBDD el evento con el nombre y dni_usuario datos
			ps = conn.prepareStatement("SELECT * FROM eventos WHERE nombre=? AND dni_usuario=?");
			ps.setString(1, nombre);
			ps.setString(2, dni_usuario);
			ResultSet rs = ps.executeQuery();

			if (rs.next()) { //Comprobamos si se ha encontrado
				evento = new Evento(rs.getString("dni_usuario"),rs.getString("nombre"), 
				rs.getString("tipo"), rs.getBoolean("completado"), rs.getDate("dia"));
			}

		} catch (SQLException | ClassNotFoundException e){ //Manejo de excepciones
			System.out.println("eventoController: Excepción:" + e.getMessage());
		} finally {
				try {
				if ( ps != null ) ps.close();
				if ( conn != null ) conn.close(); 
				} catch (SQLException e){
					System.out.println("eventoController: Excepción SQLException cerrando recursos:" + e.getMessage());
			}
		}

		if (evento != null) {//Se devuelve el evento solicitado o un error
			System.out.println("Encontrado el evento con nombre: " + nombre + " y dni del usuario: "+ dni_usuario);
			respuesta=  new ResponseEntity<>(evento, HttpStatus.OK);
		} else{
			respuesta=  new ResponseEntity<>( HttpStatus.INTERNAL_SERVER_ERROR);    
		}

		return respuesta;
    }

 
    // Actualiza un evento con nombre y dni_usuario dado por parámetros
    // Método HTTP: PUT
    // URL: http:localhost:8080/evento?nombre=nom&dni_usuario=dni
    // Body: Datos nuevos de la evento
	@ApiOperation(value = "Actualiza un evento con nombre y dni_usuario dado por parámetros", notes = "Este endpoint permite actualizar un evento con dni y nombre dados por parámetros a partir del body de la petición")
    @PutMapping("/evento")
    public ResponseEntity<String> updateEvento(@RequestParam String nombre, @RequestParam String dni_usuario, @RequestBody Evento nuevoEvento) {
		
		Connection conn = null;
		PreparedStatement ps = null;
		int resultado = 0;
		ResponseEntity<String> respuesta = null;

		try {
			// Acceso a la BBDD
			Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
			//Se actualiza el evento correspondiente al nombre y dni en la BBDD
			ps = conn.prepareStatement("UPDATE eventos SET nombre = ?, tipo = ?, dni_usuario = ?, completado = ?, dia = ? WHERE nombre = ? AND dni_usuario = ?");
			ps.setString(1, nuevoEvento.getNombre());
			ps.setString(2, nuevoEvento.getTipo());
			ps.setString(3, nuevoEvento.getDni_usuario());
			ps.setBoolean(4, nuevoEvento.getCompletado());
			ps.setDate(5, nuevoEvento.getDia());
			ps.setString(6, nombre);
			ps.setString(7, dni_usuario);

			resultado = ps.executeUpdate();

			
		}catch (SQLException | ClassNotFoundException e){ //Manejo de excepciones
			System.out.println("eventoController: Excepción:" + e.getMessage());
		}finally {
			try {
				if ( ps != null ) ps.close();
				if ( conn != null ) conn.close(); 
				} catch (SQLException e){
					System.out.println("eventoController: Excepción SQLException cerrando recursos:" + e.getMessage());
			}
		}

		//Por compatibilidad con las aplicaciones móviles desarrolladas se devuelve un objeto JSON
		// convertido a string en lugar de un string directamente, tanto en caso de éxito como en caso de error
		JSONObject json = new JSONObject();
		if (resultado >= 1) {
			json.put("message", "Actualizado");
			System.out.println("Actualizada el evento con nombre " + nombre + " y dni del usuario: " + dni_usuario);
			respuesta = new ResponseEntity<>(json.toString(), HttpStatus.OK);
		} else {
			json.put("message", "No actualizado");
			System.out.println("No se ha podido actualizar el evento con nombre " + nombre + " y dni del usuario: " + dni_usuario);
			respuesta = new ResponseEntity<>(json.toString(), HttpStatus.NOT_MODIFIED);
		}

		return respuesta;
    }




	// Elimina un evento dado su nombre y dni_usuario
	// Metodo HTTP: DELETE 
	// Ejemplo URI: http://localhost:8080/evento?nombre=nom&dni_usuario=dni
	@ApiOperation(value = "Elimina un evento dado su nombre y dni_usuario", notes = "Este endpoint permite eliminar un evento a partir del dni y del nombre")
	@DeleteMapping("/evento")
	public ResponseEntity<String> 
		deleteEvento(@RequestParam String nombre, @RequestParam String dni_usuario) {
		
		ResponseEntity<String> resultado = null;
		Connection conn = null;
		PreparedStatement ps = null;
		int numRegBorrados = 0;
		try{
			// Acceso a la BBDD
			Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
			// Borrado del evento
			ps = conn.prepareStatement("DELETE FROM eventos  WHERE nombre = ? AND dni_usuario = ?");
			ps.setString(1, nombre);
			ps.setString(2, dni_usuario);
			numRegBorrados = ps.executeUpdate();

		} catch (SQLException | ClassNotFoundException e){ //manejo de excepciones
		System.out.println("eventoController: Excepción SQLException:" + e.getMessage());
		} finally {
			try{
				if (ps != null) ps.close();
				if (conn != null) conn.close(); 
			} catch (SQLException e){
				System.out.println("eventoController: Excepción SQLException:" + e.getMessage());
			}
		}

		//Por compatibilidad con las aplicaciones móviles desarrolladas se devuelve un objeto JSON
		// convertido a string en lugar de un string directamente, tanto en caso de éxito como en caso de error
		JSONObject json = new JSONObject();
		if (numRegBorrados >= 1){
			json.put("message", "Eliminado");
			System.out.println("Eliminado el evento con  nombre:  " + nombre + " y dni del usuario: " + dni_usuario);
			resultado = new ResponseEntity<>(json.toString(), HttpStatus.OK);
		}
		else {
			json.put("message", "No eliminado");
			System.out.println("NO eliminado el evento con  nombre:  " + nombre + " y dni del usuario: " + dni_usuario);
			resultado = new ResponseEntity<>(json.toString(),HttpStatus.NOT_FOUND);
		}

		return resultado;
	}


}
