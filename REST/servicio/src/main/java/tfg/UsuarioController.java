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
 * Clase controladora para la gestión de USUARIOS.
 * 
 * Proporciona endpoints REST para crear, obtener, actualizar y eliminar usuarios.
 * 
 * Métodos:
 * - creaUsuario: Crea un nuevo usuario a partir del body de la petición.
 * - getUsuario: Obtiene un usuario a partir del dni y contraseña dados por parámetros.
 * - obtenerUsuarios: Obtiene una lista de todos los usuarios.
 * - deleteUsuario: Elimina un usuario con dni dado en el PATH.
 * - getUsuario: Obtiene un usuario con dni dado en el PATH.
 * - updateUsuario: Actualiza un usuario con dni dado en el PATH a partir del body de la petición.
 * 
 */

@RestController
public class UsuarioController {
    //Se definen las variables para las conexiones a la base de datos
    private static final String dbUser ="sensapp";
    private static final String dbPassword = "sensapptfg66&";
    private static final String dbUrl = "jdbc:postgresql://database-sensapp.cz4i4mewyc6r.us-east-1.rds.amazonaws.com:5432/databasesensapp";
 
 



    // Crea un nuevo usuario
    // Método HTTP: POST 
    // URL: http://localhost:8080/usuario
    // Body: Usuario a crear
	@ApiOperation(value = "Crea un nuevo usuario", notes = "Este endpoint permite crear  un nuevo usuario a partir del body de la petición")
    @PostMapping(path = "/usuario")
    public ResponseEntity<String> creaUsuario(@RequestBody Usuario nuevoUsuario){
	
		int resultadoExe=0;
		ResponseEntity<String> respuesta = null;
		Connection conn = null;
		PreparedStatement ps = null;

		try{
			//Acceso a la BBDD
			Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection(dbUrl, dbUser,dbPassword);
			//Se crea el nuevo usuario en la BBDD
			ps = conn.prepareStatement("INSERT INTO usuarios (nombre, apellidos, fecha_nacimiento, domicilio, enfermedades_previas, alergias, dni, telefono, contrasena) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
			ps.setString(1, nuevoUsuario.getNombre());
			ps.setString(2, nuevoUsuario.getApellidos());
			ps.setDate(3, nuevoUsuario.getFecha_nacimiento());
			ps.setString(4, nuevoUsuario.getDomicilio());
			ps.setString(5, nuevoUsuario.getEnfermedades_previas());
			ps.setString(6, nuevoUsuario.getAlergias());
			ps.setString(7, nuevoUsuario.getDni());
			ps.setInt(8, nuevoUsuario.getTelefono());
			ps.setString(9, nuevoUsuario.getContrasena());
			
			resultadoExe = ps.executeUpdate();
	
		} catch (SQLException | ClassNotFoundException e){ //Manejo de excepciones
			System.out.println("usuarioController: Excepción:" + e.getMessage());
		} finally {
			try {
				if ( ps != null ) ps.close();
				if ( conn != null ) conn.close(); 
				} catch (SQLException e){
					System.out.println("usuarioController: Excepción SQLException cerrando recursos:" + e.getMessage());
			}
		}


		//Por compatibilidad con las aplicaciones móviles desarrolladas se devuelve un objeto JSON
		// convertido a string en lugar de un string directamente, tanto en caso de éxito como en caso de error
		JSONObject json = new JSONObject();
		if (resultadoExe >= 1){
			json.put("message", "Insertado");
			respuesta = new ResponseEntity<>(json.toString(), HttpStatus.CREATED);
			System.out.println("Se ha insertado el usuario con resultado: " + resultadoExe);
		}
		else{	
			json.put("message", "No insertado");
			respuesta = new ResponseEntity<>(json.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return respuesta; 
    }



    // Obtiene un usuario con el dni y la contraseña pasados por parámetros
    // Método HTTP: GET
    // URL: http://localhost:8080/usuario?dni=dni&contrasena=contrasena1
    // Body:
	@ApiOperation(value = "Obtiene un usuario con el dni y la contraseña pasados por parámetros", notes = "Este endpoint permite obtener un usuario a partir del dni y contraseña dados por parámetros")
    @GetMapping("/usuario")
    public ResponseEntity<Usuario> getUsuario(@RequestParam String dni, @RequestParam String contrasena) {
		
		Connection conn = null;
		PreparedStatement ps = null;
		Usuario usuario = null;
		ResponseEntity<Usuario> respuesta = null;

		try {
			// Acceso a la BBDD
			Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
			// Se busca en la BBDD usuario con usuario y contraseña
			ps = conn.prepareStatement("SELECT * FROM usuarios WHERE dni =? AND contrasena=?");
			ps.setString(1, dni);
			ps.setString(2, contrasena);
			ResultSet rs = ps.executeQuery();

			if (rs.next()) { //Comprobamos si se ha encontrado
				usuario = new Usuario(rs.getString("nombre"),rs.getString("apellidos"), 
				rs.getDate("fecha_nacimiento"), rs.getString("domicilio"), rs.getString("enfermedades_previas"), 
				rs.getString("alergias"), rs.getString("dni"), rs.getInt("telefono"), rs.getString("contrasena") );
			}

		} catch (SQLException | ClassNotFoundException e){ //Manejo de excepciones
			System.out.println("usuarioController: Excepción:" + e.getMessage());
		}finally {
				try {
					if ( ps != null ) ps.close();
					if ( conn != null ) conn.close(); 
				} catch (SQLException e){
					System.out.println("usuarioController: Excepción SQLException cerrando recursos:" + e.getMessage());
				}
		}
		
		if (usuario != null) {//Se devuelve el usuario solicitado o un error
			System.out.println("Encontrado el usuario con dni:  " + dni + "y contrasena: " + contrasena);
			respuesta=  new ResponseEntity<>(usuario, HttpStatus.OK);
		} else{
			respuesta=  new ResponseEntity<>( HttpStatus.INTERNAL_SERVER_ERROR);    
		}

		return respuesta;
    }


	// Obtiene una lista de usuarios
    // Método HTTP: GET
    // URL: http://localhost:8080/usuario/lista
    // Body:
	@ApiOperation(value = "Obtiene una lista de usuarios", notes = "Este endpoint permite obtener una lista de todos los usuarios")
    @GetMapping("/usuario/lista")
    public List<Usuario> obtenerUsuarios(@RequestParam(defaultValue = "0") int offset, @RequestParam(defaultValue = "10") int limit) {
		
		Connection conn = null;
		PreparedStatement ps = null;
		List<Usuario> usuarios = new ArrayList<>();

		try {
			// Acceso a la BBDD
			Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
			// Se buscan en la BBDD los usuarios
			ps = conn.prepareStatement("SELECT * FROM usuarios ORDER BY dni ASC LIMIT ? OFFSET ?");
			ps.setInt(1, limit);    
        	ps.setInt(2, offset);
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {//Añadimos a la lista los que se hayan encontrado
				Usuario usuario = new Usuario(rs.getString("nombre"),rs.getString("apellidos"), 
				rs.getDate("fecha_nacimiento"), rs.getString("domicilio"), rs.getString("enfermedades_previas"), 
				rs.getString("alergias"), rs.getString("dni"), rs.getInt("telefono"), rs.getString("contrasena") );
				usuarios.add(usuario);
			}
			
		} catch (SQLException | ClassNotFoundException e){ //Manejo de excepciones
		System.out.println("usuarioController: Excepción:" + e.getMessage());
		}finally {
			try {
				if ( ps != null ) ps.close();
				if ( conn != null ) conn.close(); 
			} catch (SQLException e){
				System.out.println("usuarioController: Excepción SQLException cerrando recursos:" + e.getMessage());
			}
		}

		if (!usuarios.isEmpty()){
			System.out.println("Encontrados usuarios.");
		} else{
			System.out.println("No se ha encontrado ningún usuario" );
		}

		return usuarios; //Se duvuelve la lista de usuarios solicitada
	}


	// Elimina un usuario dado su dni
	// Metodo HTTP: DELETE 
	// Ejemplo URI: http://localhost:8080/usuario?dni=dni
	@ApiOperation(value = "Elimina un usuario dado su dni", notes = "Este endpoint permite eliminar un usuario con dni dado en el path")
	@DeleteMapping("/usuario")
	public ResponseEntity<String> 
		deleteUsuario(@RequestParam String dni) {
		
		ResponseEntity<String> resultado = null;
		Connection conn = null;
		PreparedStatement ps = null;
		int numRegBorrados = 0;
		
		try{
			// Acceso a la BBDD
			Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
			// Borrado del usuario
			ps = conn.prepareStatement("DELETE FROM usuarios  WHERE dni = ?");
			ps.setString(1, dni);
			numRegBorrados = ps.executeUpdate();

		} catch (SQLException | ClassNotFoundException e){ //Manejo de excepciones
		System.out.println("usuarioController: Excepción:" + e.getMessage());
		}finally {
			try {
				if ( ps != null ) ps.close();
				if ( conn != null ) conn.close(); 
			} catch (SQLException e){
				System.out.println("usuarioController: Excepción SQLException cerrando recursos:" + e.getMessage());
			}
		}

		//Por compatibilidad con las aplicaciones móviles desarrolladas se devuelve un objeto JSON
		// convertido a string en lugar de un string directamente, tanto en caso de éxito como en caso de error
		JSONObject json = new JSONObject();
		if (numRegBorrados >= 1){
			json.put("message", "Eliminado");
			System.out.println("Eliminado el usuario con  dni:  " + dni);
			resultado = new ResponseEntity<>(json.toString(), HttpStatus.OK);
		}
		else {
			json.put("message", "No eliminado");
			System.out.println("NO eliminado el usuario con dni: " + dni);
			resultado = new ResponseEntity<>(json.toString(),HttpStatus.NOT_FOUND);
		}

		return resultado;
	}


	// Obtiene el usuario cuyo dni se da 
	// Método HTTP: GET
	// URL: http://localhost:8080/usuario/dni1
	// Body:
	@ApiOperation(value = "Obtiene un usuario con dni dado", notes = "Este endpoint permite obtener un usuario con dni dado en el path")
	@GetMapping("/usuario/{dni}")
	public ResponseEntity<Usuario> getUsuario(@PathVariable(value = "dni") String dni ){
		
		Connection conn = null;
		PreparedStatement ps = null;
		Usuario miusuario = null;
		ResponseEntity<Usuario> respuesta = null;

		try {
			// Acceso a la BBDD
			Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
			// Se busca el usuario con usuario dado en la BBDD
			ps = conn.prepareStatement("SELECT * FROM usuarios WHERE dni =?");
			ps.setString(1, dni);
			ResultSet rs = ps.executeQuery();

			if (rs.next()) { //Comprobamos si se ha encontrado
				miusuario = new Usuario(rs.getString("nombre"),rs.getString("apellidos"), 
				rs.getDate("fecha_nacimiento"), rs.getString("domicilio"), rs.getString("enfermedades_previas"), 
				rs.getString("alergias"), rs.getString("dni"), rs.getInt("telefono"), rs.getString("contrasena") );
			}

		} catch (SQLException | ClassNotFoundException e){ //Manejo de excepciones
		System.out.println("usuarioController: Excepción:" + e.getMessage());
		}finally {
			try {
				if ( ps != null ) ps.close();
				if ( conn != null ) conn.close(); 
			} catch (SQLException e){
				System.out.println("usuarioController: Excepción SQLException cerrando recursos:" + e.getMessage());
			}
		}

		if (miusuario != null) { //Se devuelve el usuario solicitado o error
			System.out.println("Encontrada el usuario con dni: " + dni);
			respuesta=  new ResponseEntity<>(miusuario, HttpStatus.OK);
		} else{
			respuesta=  new ResponseEntity<>( HttpStatus.INTERNAL_SERVER_ERROR);    
		}

		return respuesta;
	}


 
    // Actualiza un usuario
    // Método HTTP: PUT
    // URL: http:localhost:8080/usuario/dni1
    // Body: Datos nuevos del usuario
	@ApiOperation(value = "Actualiza un usuario", notes = "Este endpoint permite actualizar un usuario con dni dado en el path a partir del body de la petición")
    @PutMapping("/usuario/{dni}")
    public ResponseEntity<String> updateUsuario(@PathVariable(value = "dni") String dni, @RequestBody Usuario nuevoUsuario) {
		
		Connection conn = null;
		PreparedStatement ps = null;
		int resultado = 0;
		ResponseEntity<String> respuesta = null;

		try {
			// Acceso a la BBDD
			Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
			// Se actualiza el usuario correspondiente al usuario
			ps = conn.prepareStatement("UPDATE usuarios SET nombre = ?, apellidos = ?, fecha_nacimiento = ?, domicilio = ?, enfermedades_previas = ?, alergias = ?, dni = ?, telefono = ?, contrasena = ? WHERE dni = ?");
			ps.setString(1, nuevoUsuario.getNombre());	
			ps.setString(2, nuevoUsuario.getApellidos());	
			ps.setDate(3, nuevoUsuario.getFecha_nacimiento());	
			ps.setString(4, nuevoUsuario.getDomicilio());	
			ps.setString(5, nuevoUsuario.getEnfermedades_previas());	
			ps.setString(6, nuevoUsuario.getAlergias());	
			ps.setString(7, nuevoUsuario.getDni());	
			ps.setInt(8, nuevoUsuario.getTelefono());	
			ps.setString(9, nuevoUsuario.getContrasena());	
			ps.setString(10, dni);	
			resultado = ps.executeUpdate();
			
		}catch (SQLException | ClassNotFoundException e){ //Manejo de excepciones
			System.out.println("usuarioController: Excepción:" + e.getMessage());
		} finally {
			try {
				if ( ps != null ) ps.close();
				if ( conn != null ) conn.close(); 
				} catch (SQLException e){
					System.out.println("usuarioController: Excepción SQLException cerrando recursos:" + e.getMessage());
			}
		}
		

		//Por compatibilidad con las aplicaciones móviles desarrolladas se devuelve un objeto JSON
		// convertido a string en lugar de un string directamente, tanto en caso de éxito como en caso de error
		JSONObject json = new JSONObject();
		if (resultado >= 1) {
			json.put("message", "Actualizado");
			System.out.println("Actualizado el usuario con dni: " + dni);
			respuesta =  new ResponseEntity<>(json.toString(), HttpStatus.OK);
		} else {
			json.put("message", "No actualizado");
			System.out.println("No se ha podido actualizar el usuario con dni: " + dni);
			respuesta =  new ResponseEntity<>(json.toString(), HttpStatus.NOT_MODIFIED);
		}
		
		return respuesta;
	}



}
