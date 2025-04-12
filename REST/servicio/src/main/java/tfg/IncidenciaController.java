package tfg;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.*;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.sql.*;
import org.json.JSONObject;

/**
 * Clase controladora para la gestión de INCIDENCIAS.
 * 
 * Proporciona endpoints REST para crear, obtener, actualizar y eliminar incidencias.
 * 
 * Métodos:
 * - creaIncidencia: Crea una nueva incidencia a partir del body de la petición.
 * - obtenerIncidencias: Obtiene una lista de todas las incidencias.
 * - getIncidencia: Obtiene una incidencia a partir del id dado por parámetros.
 * - updateIncidencia: Actualiza una incidencia con id dado en el PATH a partir del body de la petición.
 * - updateIndicencia: Actualiza una indicencia con id y operador dados por parámetros a partir del body de la petición.
 * - obtenerIncidenciasNoResueltas: Obtiene una lista de todas las incidencias NO resueltas.
 * - obtenerIncidenciasDni: Obtiene una lista de todas las incidencias con dni dado en el PATH.
 * - obtenerIncidenciasOperador: Obtiene una lista de todas las incidencias con operador dado en el PATH.
 * - deleteIncidencia: Elimina una incidencia a partir del id.
 * 
 */

@RestController
public class IncidenciaController {
    //Se definen las variables para las conexiones a la base de datos
    private static final String dbUser ="sensapp";
    private static final String dbPassword = "sensapptfg66&";
    private static final String dbUrl = "jdbc:postgresql://database-sensapp.cz4i4mewyc6r.us-east-1.rds.amazonaws.com:5432/databasesensapp";
 


    // Crea una nueva incidencia
    // Método HTTP: POST 
    // URL: http://localhost:8080/incidencia
    // Body: Incidencia a crear
	@ApiOperation(value = "Crea una nueva incidencia", notes = "Este endpoint permite crear una nueva incidencia a partir del body de la petición")
    @PostMapping(path = "/incidencia")
    public ResponseEntity<String> creaIncidencia(@RequestBody Incidencia nuevaIncidencia){
	
		int resultadoExe=0;
		ResponseEntity<String> respuesta = null;
		Connection conn = null;
		PreparedStatement ps = null;

		try{
			//Acceso a la BBDD
			Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection(dbUrl, dbUser,dbPassword);
			//Se crea la nueva incidencia en la BBDD
			ps = conn.prepareStatement("INSERT INTO incidencias (dni_usuario, resuelta, fecha, hora) VALUES (?, ?, ?, ?)");
			ps.setString(1, nuevaIncidencia.getDni_usuario());
			ps.setBoolean(2, nuevaIncidencia.getResuelta());
			ps.setDate(3, nuevaIncidencia.getFecha());
			ps.setTime(4, nuevaIncidencia.getHora());

			resultadoExe = ps.executeUpdate();
			
		} catch (SQLException | ClassNotFoundException e){ //manejo de excepciones
			System.out.println("incidenciaController: Excepción SQLException:" + e.getMessage());
		} finally {
			try {
				if ( ps != null ) ps.close();
				if ( conn != null ) conn.close(); 
				} catch (SQLException e){
					System.out.println("incidenciaController: Excepción SQLException:" + e.getMessage());
			}
		}
		
		
		//Por compatibilidad con las aplicaciones móviles desarrolladas se devuelve un objeto JSON
		// convertido a string en lugar de un string directamente, tanto en caso de éxito como en caso de error
		JSONObject json = new JSONObject();
		if (resultadoExe >= 1){
			json.put("message", "Insertado");
			respuesta = new ResponseEntity<>(json.toString(), HttpStatus.CREATED);
			System.out.println("Se ha insertado la incidencia con resultado: " + resultadoExe);
	
		}
		else{	
			json.put("message", "No insertado");
			respuesta = new ResponseEntity<>(json.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return respuesta; 
    }




	// Obtiene una lista de incidencias
    // Método HTTP: GET
    // URL: http://localhost:8080/indicencia/lista
    // Body:
	@ApiOperation(value = "Obtiene una lista de incidencias", notes = "Este endpoint permite obtener una lista de todas las incidencias")
    @GetMapping("/incidencia/lista")
    public List<Incidencia> obtenerIncidencias(@RequestParam(defaultValue = "0") int offset, @RequestParam(defaultValue = "10") int limit) {
	
		Connection conn = null;
		PreparedStatement ps = null;
		List<Incidencia> incidencias = new ArrayList<>();

		try {
			// Acceso a la BBDD
			Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
			// Se buscan las incidencias en la BBDD
			ps = conn.prepareStatement("SELECT * FROM incidencias");
			ps = conn.prepareStatement("SELECT * FROM incidencias ORDER BY fecha DESC, hora DESC LIMIT ? OFFSET ?");
			ps.setInt(1, limit);    
        	ps.setInt(2, offset);
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {//Añadimos a la lista las que se hayan encontrado
				Incidencia incidencia = new Incidencia(rs.getLong("id"),rs.getString("dni_usuario"), 
				rs.getString("operador"), rs.getBoolean("resuelta"), rs.getDate("fecha"), 
				rs.getTime("hora"), rs.getString("descripcion"), rs.getString("procedimiento"));
				incidencias.add(incidencia);
			}
			
		} catch (SQLException | ClassNotFoundException e){ //Manejo de excepciones
		System.out.println("incidenciaController: Excepción:" + e.getMessage());
		} finally {
			try {
				if ( ps != null ) ps.close();
				if ( conn != null ) conn.close(); 
			} catch (SQLException e){
				System.out.println("incidenciaController: Excepción SQLException cerrando recursos:" + e.getMessage());
			}
		}
		
		if (!incidencias.isEmpty()){	
			System.out.println("Encontradas incidencias.");
		} else{
			System.out.println("No se ha encontrado ninguna incidencia" );
		}
		
		return incidencias; //Se devuelve la lista de incidencias solicitadas

	}


	// Obtiene una incidencia con el id pasado por parámetros
    // Método HTTP: GET
    // URL: http://localhost:8080/incidencia?id=id1
    // Body:
	@ApiOperation(value = "Obtiene una incidencia con el id pasado por parámetros", notes = "Este endpoint permite obtener una incidencia a partir del id dado por parámetros")
    @GetMapping("/incidencia")
    public ResponseEntity<Incidencia> getIncidencia(@RequestParam Long id) {
		
		Connection conn = null;
		PreparedStatement ps = null;
		Incidencia incidencia = null;
		ResponseEntity<Incidencia> respuesta = null;

		try {
			// Acceso a la BBDD
			Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
			// Se busca la incidencia con el id en la BBDD
			ps = conn.prepareStatement("SELECT * FROM incidencias WHERE id=?");
			ps.setLong(1, id);
			ResultSet rs = ps.executeQuery();

			if (rs.next()) { //Comprobamos si se ha encontrado
				incidencia = new Incidencia(rs.getLong("id"),rs.getString("dni_usuario"), 
				rs.getString("operador"), rs.getBoolean("resuelta"), rs.getDate("fecha"),
				rs.getTime("hora"), rs.getString("descripcion"), rs.getString("procedimiento") );
			}

		} catch (SQLException | ClassNotFoundException e){ //Manejo de excepciones
			System.out.println("incidenciaController: Excepción:" + e.getMessage());
		}finally {
			try {
				if ( ps != null ) ps.close();
				if ( conn != null ) conn.close(); 
				} catch (SQLException e){
					System.out.println("incidenciaController: Excepción SQLException cerrando recursos:" + e.getMessage());
			}
		}

		if (incidencia != null) {//Se devuelve la incidencia solicitada o un error
			System.out.println("Encontrado la incidencia con id: " + id);
			respuesta=  new ResponseEntity<>(incidencia, HttpStatus.OK);
		} else{
			respuesta=  new ResponseEntity<>( HttpStatus.INTERNAL_SERVER_ERROR);    
		}
		return respuesta;
    }

  
    // Actualiza una incidencia dado su id en el path
    // Método HTTP: PUT
    // URL: http:localhost:8080/incidencia/id1
    // Body: Datos nuevos de la incidencia
	@ApiOperation(value = "Actualiza una incidencia dado su id en el path", notes = "Este endpoint permite actualizar  una incidencia con id dado en el path a partir del body de la petición")
    @PutMapping("/incidencia/{id}")
    public ResponseEntity<String> updateIncidencia(@PathVariable(value = "id") Long id, @RequestBody Incidencia nuevaIncidencia) {
		
		Connection conn = null;
		PreparedStatement ps = null;
		int resultado = 0;
		ResponseEntity<String> respuesta = null;

		try {
			// Acceso a la BBDD
			Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
			// Se actualiza la incidencia correspondiente al id
			ps = conn.prepareStatement("UPDATE incidencias SET dni_usuario = ?, operador = ?, resuelta = ?, descripcion = ?, procedimiento = ? WHERE id = ?");
			ps.setString(1, nuevaIncidencia.getDni_usuario());
			ps.setString(2, nuevaIncidencia.getOperador());
			ps.setBoolean(3, nuevaIncidencia.getResuelta());
			ps.setString(4, nuevaIncidencia.getDescripcion());
			ps.setString(5, nuevaIncidencia.getProcedimiento());
			ps.setLong(6, id);
			resultado = ps.executeUpdate();
			
		}catch (SQLException | ClassNotFoundException e){ //Manejo de excepciones
			System.out.println("incidenciaController: Excepción:" + e.getMessage());
		} finally {
			try {
				if ( ps != null ) ps.close();
				if ( conn != null ) conn.close(); 
				} catch (SQLException e){
					System.out.println("incidenciaController: Excepción SQLException cerrando recursos:" + e.getMessage());
			}
		}

		//Por compatibilidad con las aplicaciones móviles desarrolladas se devuelve un objeto JSON
		// convertido a string en lugar de un string directamente, tanto en caso de éxito como en caso de error
		JSONObject json = new JSONObject();
		if (resultado >= 1) {
			json.put("message", "Actualizado");
			System.out.println("Actualizada la incidencia con id " + id);
			respuesta = new ResponseEntity<>(json.toString(), HttpStatus.OK);
		} else {
			json.put("message", "No actualizado");
			System.out.println("No se ha podido actualizar la incidencia con id: " + id);
			respuesta = new ResponseEntity<>(json.toString(), HttpStatus.NOT_MODIFIED);
		}

		return respuesta;
    }



    // Actualiza una incidencia segun id y operador pasados por parámetros
    // Método HTTP: PUT
    // URL: http:localhost:8080/incidencia?id=id1&operador=ope1
    // Body: Datos nuevos de la incidencia
	@ApiOperation(value = "Actualiza una incidencia segun id y operador pasados por parámetros", notes = "Este endpoint permite actualizar una indicencia con id y operador dados por parámetros a partir del body de la petición")
    @PutMapping("/incidencia")
    public ResponseEntity<String> updateIncidencia(@RequestParam Long id, @RequestParam String operador, @RequestBody Incidencia nuevaIncidencia) {
		
		Connection conn = null;
		PreparedStatement ps = null;
		int resultado = 0;
		ResponseEntity<String> respuesta = null;

		try {
			// Acceso a la BBDD
			Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
			// Se actualiza la incidencia correspondiente al id y operador
			ps = conn.prepareStatement("UPDATE incidencias SET dni_usuario = ?,resuelta = ?, descripcion = ?, procedimiento = ? WHERE id = ? AND operador = ?");
			ps.setString(1, nuevaIncidencia.getDni_usuario());
			ps.setBoolean(2, nuevaIncidencia.getResuelta());
			ps.setString(3, nuevaIncidencia.getDescripcion());
			ps.setString(4, nuevaIncidencia.getProcedimiento());
			ps.setLong(5, id);
			ps.setString(6, operador);
			resultado = ps.executeUpdate();

			
		}catch (SQLException | ClassNotFoundException e){ //Manejo de excepciones
			System.out.println("incidenciaController: Excepción:" + e.getMessage());
		} finally {
			try {
				if ( ps != null ) ps.close();
				if ( conn != null ) conn.close(); 
				} catch (SQLException e){
					System.out.println("incidenciaController: Excepción SQLException:" + e.getMessage());
			}
		}

		//Por compatibilidad con las aplicaciones móviles desarrolladas se devuelve un objeto JSON
		// convertido a string en lugar de un string directamente, tanto en caso de éxito como en caso de error
		JSONObject json = new JSONObject();
		if (resultado >= 1) {
			json.put("message", "Actualizado");
			System.out.println("Actualizada la incidencia con id " + id);
			respuesta =  new ResponseEntity<>(json.toString(), HttpStatus.OK);
		} else {
			json.put("message", "No actualizado");
			System.out.println("No se ha podido actualizar la incidencia con id: " + id);
			respuesta =  new ResponseEntity<>(json.toString(), HttpStatus.NOT_MODIFIED);
		}

		return respuesta;
    }

	// Obtiene una lista de incidencias NO resueltas 
    // Método HTTP: GET
    // URL: http://localhost:8080/incidencia/noresuelta
    // Body:
	@ApiOperation(value = "Obtiene una lista de incidencias no resueltas ", notes = "Este endpoint permite obtener una lista de todas las incidencias no resueltas")
    @GetMapping("/incidencia/noresuelta")
    public List<Incidencia> obtenerIncidenciasNoResueltas(@RequestParam(defaultValue = "0") int offset, @RequestParam(defaultValue = "10") int limit) {
	
		Connection conn = null;
		PreparedStatement ps = null;
		List<Incidencia> incidencias = new ArrayList<>();

		try {
			// Acceso a la BBDD
			Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
			// Se buscan las incidencias en la BBDD
			ps = conn.prepareStatement("SELECT * FROM incidencias WHERE resuelta = ? AND (operador IS NULL OR operador = '') ORDER BY fecha ASC, hora ASC LIMIT ? OFFSET ?");
			ps.setBoolean(1, false);
			ps.setInt(2, limit);    
        	ps.setInt(3, offset);
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {//Añadimos a la lista las que se hayan encontrado
				Incidencia incidencia = new Incidencia(rs.getLong("id"),rs.getString("dni_usuario"), 
				rs.getString("operador"), rs.getBoolean("resuelta"), rs.getDate("fecha"), 
				rs.getTime("hora"), rs.getString("descripcion"), rs.getString("procedimiento"));
				incidencias.add(incidencia);
			}
			
		} catch (SQLException | ClassNotFoundException e){ //Manejo de excepciones
		System.out.println("incidenciaController: Excepción:" + e.getMessage());
		} finally {
			try {
				if ( ps != null ) ps.close();
				if ( conn != null ) conn.close(); 
			} catch (SQLException e){
				System.out.println("incidenciaController: Excepción SQLException cerrando recursos:" + e.getMessage());
			}
		}
		
		if (!incidencias.isEmpty()){	
			System.out.println("Encontradas incidencias.");
		} else{
			System.out.println("No se ha encontrado ninguna incidencia" );
		}
		
		return incidencias; //Se devuelve la lista de incidencias solicitadas

	}

	// Obtiene una lista de incidencias de un dni
    // Método HTTP: GET
    // URL: http://localhost:8080/indicencia/dni
    // Body:
	@ApiOperation(value = "Obtiene una lista de incidencias de un dni", notes = "Este endpoint permite obtener una lista de todas las incidencias con dni dado en el path")
    @GetMapping("/incidencia/{dni_usuario}")
    public List<Incidencia> obtenerIncidenciasDni(@PathVariable(value="dni_usuario") String dni_usuario, @RequestParam(defaultValue = "0") int offset, @RequestParam(defaultValue = "10") int limit){
	
		Connection conn = null;
		PreparedStatement ps = null;
		List<Incidencia> incidencias = new ArrayList<>();

		try {
			// Acceso a la BBDD
			Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
			// Se buscan las incidencias en la BBDD
			ps = conn.prepareStatement("SELECT * FROM incidencias WHERE dni_usuario = ? ORDER BY fecha DESC, hora DESC, id ASC LIMIT ? OFFSET ?");
			ps.setString(1, dni_usuario);
			ps.setInt(2, limit);    
        	ps.setInt(3, offset);
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {//Añadimos a la lista las que se hayan encontrado
				Incidencia incidencia = new Incidencia(rs.getLong("id"),rs.getString("dni_usuario"), 
				rs.getString("operador"), rs.getBoolean("resuelta"), rs.getDate("fecha"), 
				rs.getTime("hora"), rs.getString("descripcion"), rs.getString("procedimiento"));
				incidencias.add(incidencia);
			}
			
		} catch (SQLException | ClassNotFoundException e){ //Manejo de excepciones
		System.out.println("incidenciaController: Excepción:" + e.getMessage());
		} finally {
			try {
				if ( ps != null ) ps.close();
				if ( conn != null ) conn.close(); 
			} catch (SQLException e){
				System.out.println("incidenciaController: Excepción SQLException cerrando recursos:" + e.getMessage());
			}
		}
		
		if (!incidencias.isEmpty()){	
			System.out.println("Encontradas incidencias.");
		} else{
			System.out.println("No se ha encontrado ninguna incidencia" );
		}
		
		return incidencias; //Se devuelve la lista de incidencias solicitadas

	}

	// Obtiene una lista de incidencias de un operador
    // Método HTTP: GET
    // URL: http://localhost:8080/indicencia/lista/operador
    // Body:
	@ApiOperation(value = "Obtiene una lista de incidencias de un operador", notes = "Este endpoint permite obtener una lista de todas las incidencias con operador dado en el path")
    @GetMapping("/incidencia/lista/{operador}")
    public List<Incidencia> obtenerIncidenciasOperador(@PathVariable(value="operador") String operador, String dni_usuario, @RequestParam(defaultValue = "0") int offset, @RequestParam(defaultValue = "10") int limit) {
	
		Connection conn = null;
		PreparedStatement ps = null;
		List<Incidencia> incidencias = new ArrayList<>();

		try {
			// Acceso a la BBDD
			Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
			// Se buscan las incidencias en la BBDD
			ps = conn.prepareStatement("SELECT * FROM incidencias WHERE operador =? ORDER BY fecha DESC, hora DESC, id ASC LIMIT ? OFFSET ?");
			ps.setString(1, operador);
			ps.setInt(2, limit);    
        	ps.setInt(3, offset);
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {//Añadimos a la lista las que se hayan encontrado
				Incidencia incidencia = new Incidencia(rs.getLong("id"),rs.getString("dni_usuario"), 
				rs.getString("operador"), rs.getBoolean("resuelta"), rs.getDate("fecha"), 
				rs.getTime("hora"), rs.getString("descripcion"), rs.getString("procedimiento"));
				incidencias.add(incidencia);
			}
			
		} catch (SQLException | ClassNotFoundException e){ //Manejo de excepciones
		System.out.println("incidenciaController: Excepción:" + e.getMessage());
		} finally {
			try {
				if ( ps != null ) ps.close();
				if ( conn != null ) conn.close(); 
			} catch (SQLException e){
				System.out.println("incidenciaController: Excepción SQLException cerrando recursos:" + e.getMessage());
			}
		}
		
		if (!incidencias.isEmpty()){	
			System.out.println("Encontradas incidencias.");
		} else{
			System.out.println("No se ha encontrado ninguna incidencia" );
		}
		
		return incidencias; //Se devuelve la lista de incidencias solicitadas

	}


	// Elimina una incidencia dado su id
	// Metodo HTTP: DELETE 
	// Ejemplo URI: http://localhost:8080/incidencia/id
	@ApiOperation(value = "Elimina una incidencia dado su id", notes = "Este endpoint permite eliminar una incidencia a partir del id")
	@DeleteMapping("/incidencia/{id}")
	public ResponseEntity<String> 
		deleteIncidencia(@PathVariable(value = "id") Long id) {
		
		ResponseEntity<String> resultado = null;
		Connection conn = null;
		PreparedStatement ps = null;
		int numRegBorrados = 0;
		
		try{
			// Acceso a la BBDD
			Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
			// Borrado del operador
			ps = conn.prepareStatement("DELETE FROM incidencias  WHERE id = ?");
			ps.setLong(1, id);
			numRegBorrados = ps.executeUpdate();

		} catch (SQLException | ClassNotFoundException e){ //Manejo de excepciones
		System.out.println("incidenciaController: Excepción:" + e.getMessage());
		} finally {
			try{
				if (ps != null) ps.close();
				if (conn != null) conn.close(); 
			} catch (SQLException e){
				System.out.println("incidenciaController: Excepción SQLException cerrando recursos:" + e.getMessage());
			}
		}

		//Por compatibilidad con las aplicaciones móviles desarrolladas se devuelve un objeto JSON
		// convertido a string en lugar de un string directamente, tanto en caso de éxito como en caso de error
		JSONObject json = new JSONObject();
		if (numRegBorrados >= 1){
			json.put("message", "Eliminado");
			System.out.println("Eliminada la incidencia con id:  " + id);
			resultado = new ResponseEntity<>(json.toString(), HttpStatus.OK);
		}
		else {
			json.put("message", "No eliminado");
			System.out.println("NO eliminada la incidencia con id: " + id);
			resultado = new ResponseEntity<>(json.toString(),HttpStatus.NOT_FOUND);
		}

		return resultado;
	}

}
