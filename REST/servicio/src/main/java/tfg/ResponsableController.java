package tfg;
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
 * Clase controladora para la gestión de RESPONSABLES.
 * 
 * Proporciona endpoints REST para crear, obtener, actualizar y eliminar responsables.
 * 
 * Métodos:
 * - creaResponsable: Crea un nuevo responsable a partir del body de la petición.
 * - getResponsable: Obtiene un responsable a partir del usuario y contraseña dados por parámetros.
 * - obtenerResponsables: Obtiene una lista de todos los responsables con dni_usuario dado en el PATH.
 * - obtenerAllResponsables: Obtiene una lista de todos los repsonsables.
 * - deleteResponsable: Elimina un responsable a partir del usuario.
 * - getResponsable: Obtiene un responsable con usuario dado en el PATH.
 * - updateResponsable: Actualiza un responsable con usuario dado en el PATH a partir del body de la petición.
 * - deleteResponsables: Elimina todos los responsables con dni_usuario dado en el PATH.
 * 
 */

@RestController
public class ResponsableController {
    //Se definen las variables para las conexiones a la base de datos
    private static final String dbUser ="sensapp";
    private static final String dbPassword = "sensapptfg66&";
    private static final String dbUrl = "jdbc:postgresql://database-sensapp.cz4i4mewyc6r.us-east-1.rds.amazonaws.com:5432/databasesensapp";
 
 



    // Crea un nuevo responsable
    // Método HTTP: POST 
    // URL: http://localhost:8080/responsable
    // Body: Responsable a crear
	@ApiOperation(value = "Crea un nuevo responsable", notes = "Este endpoint permite crear un nuevo responsable a partir del body de la petición")
    @PostMapping(path = "/responsable")
    public ResponseEntity<String> creaResponsable(@RequestBody Responsable nuevoResponsable){
	
		int resultadoExe=0;
		ResponseEntity<String> respuesta = null;
		Connection conn = null;
		PreparedStatement ps = null;

		try{
			//Acceso a la BBDD
			Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection(dbUrl, dbUser,dbPassword);
			//Se crea el nuevo responsable en la BBDD
			ps = conn.prepareStatement("INSERT INTO responsables (usuario, nombre, apellidos, telefono, dni, dni_usuario, contrasena) VALUES (?, ?, ?, ?, ?, ?, ?)");
			ps.setString(1, nuevoResponsable.getUsuario());
			ps.setString(2, nuevoResponsable.getNombre());
			ps.setString(3, nuevoResponsable.getApellidos());
			ps.setInt(4, nuevoResponsable.getTelefono());
			ps.setString(5, nuevoResponsable.getDni());
			ps.setString(6, nuevoResponsable.getDni_usuario());
			ps.setString(7, nuevoResponsable.getContrasena());
			
			resultadoExe = ps.executeUpdate();
			
	
		} catch (SQLException | ClassNotFoundException e){ //Manejo de excepciones
			System.out.println("responsableController: Excepción:" + e.getMessage());
		} finally {
			try {
				if ( ps != null ) ps.close();
				if ( conn != null ) conn.close(); 
				} catch (SQLException e){
					System.out.println("responsableController: Excepción SQLException cerrando recursos:" + e.getMessage());
			}
		}


		//Por compatibilidad con las aplicaciones móviles desarrolladas se devuelve un objeto JSON
		// convertido a string en lugar de un string directamente, tanto en caso de éxito como en caso de error
		JSONObject json = new JSONObject();
		if (resultadoExe >= 1){
			json.put("message", "Insertado");
			respuesta = new ResponseEntity<>(json.toString(), HttpStatus.CREATED);
			System.out.println("Se ha insertado el responsable con resultado: " + resultadoExe);
		}
		else{	
			json.put("message", "No insertado");
			respuesta = new ResponseEntity<>(json.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return respuesta; 
    }



    // Obtiene un responsable con el usuario y la contraseña
    // Método HTTP: GET
    // URL: http://localhost:8080/responsable?usuario=usu1&contrasena=contrasena1
    // Body:
	@ApiOperation(value = "Obtiene un responsable con el usuario y la contraseña", notes = "Este endpoint permite obtener un responsable a partir del usuario y contraseña dados por parámetros")
    @GetMapping("/responsable")
    public ResponseEntity<Responsable> getResponsable(@RequestParam String usuario,  @RequestParam String contrasena) {
		
		Connection conn = null;
		PreparedStatement ps = null;
		Responsable responsable = null;
		ResponseEntity<Responsable> respuesta = null;

		try {
			// Acceso a la BBDD
			Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
			// Se busca en la BBDD responsable con usuario y contraseña
			ps = conn.prepareStatement("SELECT * FROM responsables WHERE usuario =? AND contrasena=?");
			ps.setString(1, usuario);
			ps.setString(2, contrasena);
			ResultSet rs = ps.executeQuery();

			if (rs.next()) { //Comprobamos si se ha encontrado
				responsable = new Responsable(rs.getString("usuario"), rs.getString("nombre"),
				rs.getString("apellidos"), rs.getInt("telefono"), rs.getString("dni"), 
				rs.getString("dni_usuario"),rs.getString("contrasena") );
			}

		} catch (SQLException | ClassNotFoundException e){ //Manejo de excepciones
			System.out.println("responsableController: Excepción:" + e.getMessage());
		} finally {
			try {
				if ( ps != null ) ps.close();
				if ( conn != null ) conn.close(); 
				} catch (SQLException e){
					System.out.println("responsableController: Excepción SQLException cerrando recursos:" + e.getMessage());
			}
		}

		if (responsable != null) {//Se devuelve el responsable solicitado o un error
			System.out.println("Encontrado el responsable con usuario:  " + usuario + "y contrasena: " + contrasena);
			respuesta=  new ResponseEntity<>(responsable, HttpStatus.OK);
		} else{
			respuesta=  new ResponseEntity<>( HttpStatus.INTERNAL_SERVER_ERROR);    
		}

		return respuesta;
    }


	// Obtiene una lista de responsables
    // Método HTTP: GET
    // URL: http://localhost:8080/responsable/lista/dniusuario
    // Body:
	@ApiOperation(value = "Obtiene una lista de responsables", notes = "Este endpoint permite obtener una lista de todos los responsables con dni_usuario dado en el path")
    @GetMapping("/responsable/lista/{dni_usuario}")
    public List<Responsable> obtenerResponsables(@PathVariable(value= "dni_usuario") String dni_usuario,  @RequestParam(defaultValue = "0") int offset, @RequestParam(defaultValue = "10") int limit) {
		
		Connection conn = null;
		PreparedStatement ps = null;
		List<Responsable> responsables = new ArrayList<>();

		try {
			// Acceso a la BBDD
			Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
			// Se buscan en la BBDD los responsables
			ps = conn.prepareStatement("SELECT * FROM responsables WHERE dni_usuario =? LIMIT ? OFFSET ?");
			ps.setString(1, dni_usuario);
			ps.setInt(2, limit);    
        	ps.setInt(3, offset);
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {//Añadimos a la lista los que se hayan encontrado
				Responsable responsable = new Responsable(rs.getString("usuario"), 
				rs.getString("nombre"),rs.getString("apellidos"), rs.getInt("telefono"), 
				rs.getString("dni"), rs.getString("dni_usuario"),rs.getString("contrasena") );
				responsables.add(responsable);
			}
			

		} catch (SQLException | ClassNotFoundException e){ //Manejo de excepciones
			System.out.println("responsableController: Excepción:" + e.getMessage());
		} finally {
			try {
				if ( ps != null ) ps.close();
				if ( conn != null ) conn.close(); 
				} catch (SQLException e){
					System.out.println("responsableController: Excepción SQLException cerrando recursos:" + e.getMessage());
			}
		}

		if (!responsables.isEmpty()){
			System.out.println("Encontrados responsables.");
		} else{
			System.out.println("No se ha encontrado ningún responsable" );
		}

		return responsables; //Se devuelve la lista de responables solicitados
	}



	// Obtiene una lista de responsables
    // Método HTTP: GET
    // URL: http://localhost:8080/responsable/lista
    // Body:
	@ApiOperation(value = "Obtiene una lista de responsables", notes = "Este endpoint permite obtener una lista de todos los responsables")
    @GetMapping("/responsable/lista")
    public List<Responsable> obtenerAllResponsables(@RequestParam(defaultValue = "0") int offset, @RequestParam(defaultValue = "10") int limit) {
		
		Connection conn = null;
		PreparedStatement ps = null;
		List<Responsable> responsables = new ArrayList<>();

		try {
			// Acceso a la BBDD
			Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
			// Se buscan en la BBDD los responsables
			ps = conn.prepareStatement("SELECT * FROM responsables LIMIT ? OFFSET ?");
			ps.setInt(1, limit);    
        	ps.setInt(2, offset);
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {//Añadimos a la lista los que se hayan encontrado
				Responsable responsable = new Responsable(rs.getString("usuario"), 
				rs.getString("nombre"),rs.getString("apellidos"), rs.getInt("telefono"), 
				rs.getString("dni"), rs.getString("dni_usuario"),rs.getString("contrasena") );
				responsables.add(responsable);
			}
			

		} catch (SQLException | ClassNotFoundException e){ //Manejo de excepciones
			System.out.println("responsableController: Excepción:" + e.getMessage());
		} finally {
			try {
				if ( ps != null ) ps.close();
				if ( conn != null ) conn.close(); 
				} catch (SQLException e){
					System.out.println("responsableController: Excepción SQLException cerrando recursos:" + e.getMessage());
			}
		}

		if (!responsables.isEmpty()){
			System.out.println("Encontrados responsables.");
		} else{
			System.out.println("No se ha encontrado ningún responsable" );
		}

		return responsables; //Se devuelve la lista de responsables solicitados
	}


	// Elimina un responsable dado su usuario
	// Metodo HTTP: DELETE 
	// Ejemplo URI: http://localhost:8080/responsable/usu1
	@ApiOperation(value = "Elimina un responsable dado su usuario", notes = "Este endpoint permite eliminar un responsable a partir del usuario")
	@DeleteMapping("/responsable/{usuario}")
	public ResponseEntity<String> 
		deleteResponsable(@PathVariable(value= "usuario") String usuario) {
		
		ResponseEntity<String> resultado = null;
		Connection conn = null;
		PreparedStatement ps = null;
		int numRegBorrados = 0;

		try{
		
			// Acceso a la BBDD
			Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
			// Borrado del responsable
			ps = conn.prepareStatement("DELETE FROM responsables  WHERE usuario = ?");
			ps.setString(1, usuario);
			numRegBorrados = ps.executeUpdate();

		} catch (SQLException | ClassNotFoundException e){ //Manejo de excepciones
			System.out.println("responsableController: Excepción:" + e.getMessage());
		} finally {
			try {
				if ( ps != null ) ps.close();
				if ( conn != null ) conn.close(); 
				} catch (SQLException e){
					System.out.println("responsableController: Excepción SQLException cerrando recursos:" + e.getMessage());
			}
		}


		//Por compatibilidad con las aplicaciones móviles desarrolladas se devuelve un objeto JSON
		// convertido a string en lugar de un string directamente, tanto en caso de éxito como en caso de error
		JSONObject json = new JSONObject();
		if (numRegBorrados >= 1){
			json.put("message", "Eliminado");
			System.out.println("Eliminado el responsable con  usuario:  " + usuario);
			resultado = new ResponseEntity<>(json.toString(), HttpStatus.OK);
		}
		else {
			json.put("message", "No eliminado");
			System.out.println("NO eliminado el responsable con usuario: " + usuario);
			resultado = new ResponseEntity<>(json.toString(),HttpStatus.NOT_FOUND);
		}
		return resultado;
	}


	// Obtiene el responsable cuyo usuario se da 
	// Método HTTP: GET
	// URL: http://localhost:8080/responsable/usuario1
	// Body:
	@ApiOperation(value = "Obtiene el responsable con usuario dado", notes = "Este endpoint permite obtener un responsable con usuario dado en el path")
	@GetMapping("/responsable/{usuario}")
	public ResponseEntity<Responsable> getResponsable(@PathVariable(value = "usuario") String usuario ){
		
		Connection conn = null;
		PreparedStatement ps = null;
		Responsable miresponsable = null;
		ResponseEntity<Responsable> respuesta = null;

		try {
			// Acceso a la BBDD
			Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
			// Se busca responsable con usuario dado en la BDD
			ps = conn.prepareStatement("SELECT * FROM responsables WHERE usuario =?");
			ps.setString(1, usuario);
			ResultSet rs = ps.executeQuery();
			
			if (rs.next()) { //Comprobamos si se ha encontrado
				miresponsable = new Responsable(rs.getString("usuario"), rs.getString("nombre"),
				rs.getString("apellidos"), rs.getInt("telefono"), rs.getString("dni"), rs.getString("dni_usuario"),
				rs.getString("contrasena") );
			}

		} catch (SQLException | ClassNotFoundException e){ //Manejo de excepciones
			System.out.println("responsableController: Excepción:" + e.getMessage());
		} finally {
			try {
				if ( ps != null ) ps.close();
				if ( conn != null ) conn.close(); 
				} catch (SQLException e){
					System.out.println("responsableController: Excepción SQLException cerrando recursos:" + e.getMessage());
			}
		}


		if (miresponsable != null) { //devolvemos el responsable solicitado o error
			System.out.println("Encontrada el responsable con usuario: " + usuario);
			respuesta=  new ResponseEntity<>(miresponsable, HttpStatus.OK);
		} else{
			respuesta=  new ResponseEntity<>( HttpStatus.INTERNAL_SERVER_ERROR);    
		}

		return respuesta;
	}


 
    // Actualiza un responsable
    // Método HTTP: PUT
    // URL: http:localhost:8080/responsable/usuario1
    // Body: Datos nuevos del responsable
	@ApiOperation(value = "Actualiza un responsable", notes = "Este endpoint permite actualizar un responsable con usuario dado en el path a partir del body de la petición")
    @PutMapping("/responsable/{usuario}")
    public ResponseEntity<String> updateResponsable(@PathVariable(value = "usuario") String usuario, @RequestBody Responsable nuevoResponsable) {
		Connection conn = null;
		PreparedStatement ps = null;
		int resultado = 0;
		ResponseEntity<String> respuesta = null;

		try {
			// Acceso a la BBDD
			Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
			// Se actualiza el responsable correspondiente al usuario
			ps = conn.prepareStatement("UPDATE responsables SET usuario = ?, nombre = ?, apellidos = ?, telefono = ?, dni = ?, dni_usuario = ?, contrasena = ? WHERE usuario = ?");
			ps.setString(1, nuevoResponsable.getUsuario());
			ps.setString(2, nuevoResponsable.getNombre());
			ps.setString(3, nuevoResponsable.getApellidos());
			ps.setInt(4, nuevoResponsable.getTelefono());
			ps.setString(5, nuevoResponsable.getDni());
			ps.setString(6, nuevoResponsable.getDni_usuario());
			ps.setString(7, nuevoResponsable.getContrasena());
			ps.setString(8, usuario);
			resultado = ps.executeUpdate();

			
		}catch (SQLException | ClassNotFoundException e){ //Manejo de excepciones
			System.out.println("responsableController: Excepción:" + e.getMessage());
		} finally {
			try {
				if ( ps != null ) ps.close();
				if ( conn != null ) conn.close(); 
				} catch (SQLException e){
					System.out.println("responsableController: Excepción SQLException cerrando recursos:" + e.getMessage());
			}
		}

		//Por compatibilidad con las aplicaciones móviles desarrolladas se devuelve un objeto JSON
		// convertido a string en lugar de un string directamente, tanto en caso de éxito como en caso de error
		JSONObject json = new JSONObject();
		if (resultado >= 1) {
			json.put("message", "Actualizado");
			System.out.println("Actualizado el responsable con usaurio " + usuario);
			respuesta = new ResponseEntity<>(json.toString(), HttpStatus.OK);
		} else {
			json.put("message", "No actualizado");
			System.out.println("No se ha podido actualizar el responsable con usuario: " + usuario);
			respuesta =  new ResponseEntity<>(json.toString(), HttpStatus.NOT_MODIFIED);
		}

		return respuesta;
    }




	// Elimina los responsables con un dni_usuario dado
	// Metodo HTTP: DELETE 
	// Ejemplo URI: http://localhost:8080/responsable/lista/dni_usuario1
	@ApiOperation(value = "Elimina los responsables con un dni_usuario dado", notes = "Este endpoint permite eliminar todos los responsables con dni_usuario dado en el path")
	@DeleteMapping("/responsable/lista/{dni_usuario}")
	public ResponseEntity<String> 
		deleteResponsables(@PathVariable(value= "dni_usuario") String dni_usuario) {
		
		ResponseEntity<String> resultado = null;
		Connection conn = null;
		PreparedStatement ps = null;
		int numRegBorrados = 0;

		try{
			// Acceso a la BBDD
			Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
			// Borrado del responsable
			ps = conn.prepareStatement("DELETE FROM responsables  WHERE dni_usuario = ?");
			ps.setString(1, dni_usuario);
			numRegBorrados = ps.executeUpdate();


		} catch (SQLException | ClassNotFoundException e){ //Manejo de excepciones
			System.out.println("responsableController: Excepción:" + e.getMessage());
		} finally {
			try {
				if ( ps != null ) ps.close();
				if ( conn != null ) conn.close(); 
				} catch (SQLException e){
					System.out.println("responsableController: Excepción SQLException cerrando recursos:" + e.getMessage());
			}
		}
		
		//Por compatibilidad con las aplicaciones móviles desarrolladas se devuelve un objeto JSON
		// convertido a string en lugar de un string directamente, tanto en caso de éxito como en caso de error
		JSONObject json = new JSONObject();
		if (numRegBorrados >= 1){
			json.put("message", "Eliminado");
			System.out.println("Eliminados los responsable con  dni usuario:  " + dni_usuario);
			resultado = new ResponseEntity<>(json.toString(), HttpStatus.OK);
		}
		else {
			json.put("message", "No eliminado");
			System.out.println("NO eliminados los responsable con  dni usuario:  " + dni_usuario);
			resultado = new ResponseEntity<>(json.toString(),HttpStatus.NOT_FOUND);
		}

		return resultado;
	}


}
