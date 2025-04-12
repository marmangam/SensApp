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
 * Clase controladora para la gestión de OPERADORES.
 * 
 * Proporciona endpoints REST para crear, obtener, actualizar y eliminar operadores.
 * 
 * Métodos:
 * - creaOperador: Crea un nuevo operador a partir del body de la petición.
 * - getOperador: Obtiene un operador a partir del usuario y contraseña dados por parámetros.
 * - obtenerOperadores: Obtiene una lista de todos los operadores.
 * - deleteOperador: Elimina un operador a partir del usuario.
 * - getOperador: Obtiene un operador con usuario dado en el PATH.
 *- updateOperador: Actualiza un operador con usuario dado en el PATH a partir del body de la petición.
 * 
 */

@RestController
public class OperadorController {
    //Se definen las variables para las conexiones a la base de datos
    private static final String dbUser ="sensapp";
    private static final String dbPassword = "sensapptfg66&";
    private static final String dbUrl = "jdbc:postgresql://database-sensapp.cz4i4mewyc6r.us-east-1.rds.amazonaws.com:5432/databasesensapp";
 
 



    // Crea un nuevo operador
    // Método HTTP: POST 
    // URL: http://localhost:8080/operador
    // Body: Operador a crear
	@ApiOperation(value = "Crea un nuevo operador", notes = "Este endpoint permite crear un nuevo operador a partir del body de la petición")
    @PostMapping(path = "/operador")
    public ResponseEntity<String> creaOperador(@RequestBody Operador nuevoOperador){
	
		int resultadoExe=0;
		ResponseEntity<String> respuesta = null;
		Connection conn = null;
		PreparedStatement ps = null;

		try{
			Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection(dbUrl, dbUser,dbPassword);
			//Se crea el nuevo operador en la BBDD
			ps = conn.prepareStatement("INSERT INTO operadores (usuario, nombre, apellidos, contrasena, activo, ocupado) VALUES (?, ?, ?, ?, ?, ?)");
			ps.setString(1, nuevoOperador.getUsuario());
			ps.setString(2, nuevoOperador.getNombre());
			ps.setString(3, nuevoOperador.getApellidos());
			ps.setString(4, nuevoOperador.getContrasena());	
			ps.setBoolean(5, nuevoOperador.getActivo());
			ps.setBoolean(6, nuevoOperador.getOcupado());
			resultadoExe = ps.executeUpdate();
			
		} catch (SQLException | ClassNotFoundException e){ //Manejo de excepciones
			System.out.println("operadorController: Excepción:" + e.getMessage());
		} finally {
			try {
				if ( ps != null ) ps.close();
				if ( conn != null ) conn.close(); 
				} catch (SQLException e){
					System.out.println("operadorController: Excepción SQLException cerrando recursos:" + e.getMessage());
			}
		}
		

		//Por compatibilidad con las aplicaciones móviles desarrolladas se devuelve un objeto JSON
		// convertido a string en lugar de un string directamente, tanto en caso de éxito como en caso de error
		JSONObject json = new JSONObject();
		if (resultadoExe >= 1){
			json.put("message", "Insertado");
			respuesta = new ResponseEntity<>(json.toString(), HttpStatus.CREATED);
			System.out.println("Se ha insertado el operador con resultado: " + resultadoExe);
		}
		else{	
			json.put("message", "No insertado");
			respuesta = new ResponseEntity<>(json.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return respuesta; 
    }



    // Obtiene un operador con el usuario y la contraseña dados por parámetro
    // Método HTTP: GET
    // URL: http://localhost:8080/operador?usuario=usuario1&contrasena=contrasena1
    // Body:
	@ApiOperation(value = "Obtiene un operador con el usuario y la contraseña dados por parámetro", notes = "Este endpoint permite obtener un operador a partir del usuario y contraseña dados por parámetros")
    @GetMapping("/operador")
    public ResponseEntity<Operador> getOperador(@RequestParam String usuario, @RequestParam String contrasena) {
	
		Connection conn = null;
		PreparedStatement ps = null;
		Operador operador = null;
		ResponseEntity<Operador> respuesta = null;

		try {
			// Acceso a la BBDD
			Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
			// Se busca en la BBDD el operador con usuario y contraseña
			ps = conn.prepareStatement("SELECT * FROM operadores WHERE usuario =? AND contrasena=?");
			ps.setString(1, usuario);
			ps.setString(2, contrasena);
			ResultSet rs = ps.executeQuery();
			
			if (rs.next()) { //Comprobamos si se ha encontrado
				operador = new Operador(rs.getString("usuario"),rs.getString("nombre"),rs.getString("apellidos"), rs.getString("contrasena"), rs.getBoolean("activo"), rs.getBoolean("ocupado") );
			}

		} catch (SQLException | ClassNotFoundException e){ //Manejo de excepciones
			System.out.println("operadorController: Excepción:" + e.getMessage());
		} finally {
			try {
				if ( ps != null ) ps.close();
				if ( conn != null ) conn.close(); 
				} catch (SQLException e){
					System.out.println("operadorController: Excepción SQLException cerrando recursos:" + e.getMessage());
			}
		}


		if (operador != null) {//Se devuelve el operador solicitado o un error
			System.out.println("Encontrado el operador con usuario: " + usuario + "y contrasena: " + contrasena);
			respuesta=  new ResponseEntity<>(operador, HttpStatus.OK);
		} else{
			respuesta=  new ResponseEntity<>( HttpStatus.INTERNAL_SERVER_ERROR);    
		}

		return respuesta;
    }


	// Obtiene una lista de operadores
    // Método HTTP: GET
    // URL: http://localhost:8080/operador/lista
    // Body:
	@ApiOperation(value = "Obtiene una lista de operadores", notes = "Este endpoint permite obtener una lista de todos los operadores")
    @GetMapping("/operador/lista")
    public List<Operador> obtenerOperadores( @RequestParam(defaultValue = "0") int offset, @RequestParam(defaultValue = "10") int limit) {
	
		Connection conn = null;
		PreparedStatement ps = null;
		List<Operador> operadores = new ArrayList<>();

		try {
			// Acceso a la BBDD
			Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
			// Se buscan los operadores en la BBBDD
			ps = conn.prepareStatement("SELECT * FROM operadores ORDER BY activo DESC, ocupado ASC, usuario DESC LIMIT ? OFFSET ?");
			ps.setInt(1, limit);    
        	ps.setInt(2, offset);
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {//Añadimos a la lista los que se hayan encontrado
				Operador operador = new Operador(rs.getString("usuario"),rs.getString("nombre"), 
				rs.getString("apellidos"), rs.getString("contrasena"), rs.getBoolean("activo"), rs.getBoolean("ocupado"));
				operadores.add(operador);
			}
			
			
		} catch (SQLException | ClassNotFoundException e){ //Manejo de excepciones
		System.out.println("operadorController: Excepción:" + e.getMessage());
		} finally {
			try {
				if ( ps != null ) ps.close();
				if ( conn != null ) conn.close(); 
			} catch (SQLException e){
				System.out.println("operadorController: Excepción SQLException cerrando recursos:" + e.getMessage());
			}
		}
		if (!operadores.isEmpty()){
		System.out.println("Encontrados operadores.");
		} else{
		System.out.println("No se ha encontrado ningún operador");
		}
		return operadores; //Se devuelve la lista de operadores solicitada
}


	// Elimina un operador dado su usuario
	// Metodo HTTP: DELETE 
	// Ejemplo URI: http://localhost:8080/operador/usuario1
	@ApiOperation(value = "Elimina un operador dado su usuario", notes = "Este endpoint permite eliminar un operador a partir del usuario")
	@DeleteMapping("/operador/{usuario}")
	public ResponseEntity<String> 
		deleteOperador(@PathVariable(value = "usuario") String usuario) {
		
		ResponseEntity<String> resultado = null;
		Connection conn = null;
		PreparedStatement ps = null;
		int numRegBorrados = 0;
		
		try{
			// Acceso a la BBDD
			Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
			// Borrado del operador
			ps = conn.prepareStatement("DELETE FROM operadores  WHERE usuario = ?");
			ps.setString(1, usuario);
			numRegBorrados = ps.executeUpdate();

		} catch (SQLException | ClassNotFoundException e){ //Manejo de excepciones
		System.out.println("operadorController: Excepción:" + e.getMessage());
		} finally {
			try{
				if (ps != null) ps.close();
				if (conn != null) conn.close(); 
			} catch (SQLException e){
				System.out.println("operadorController: Excepción SQLException cerrando recursos:" + e.getMessage());
			}
		}

		//Por compatibilidad con las aplicaciones móviles desarrolladas se devuelve un objeto JSON
		// convertido a string en lugar de un string directamente, tanto en caso de éxito como en caso de error
		JSONObject json = new JSONObject();
		if (numRegBorrados >= 1){
			json.put("message", "Eliminado");
			System.out.println("Eliminado el operador con  usuario:  " + usuario);
			resultado = new ResponseEntity<>(json.toString(), HttpStatus.OK);
		}
		else {
			json.put("message", "No eliminado");
			System.out.println("NO eliminado el operador con usuario: " + usuario);
			resultado = new ResponseEntity<>(json.toString(),HttpStatus.NOT_FOUND);
		}

		return resultado;
	}


	// Obtiene el operador cuyo  usuario se da
	// Método HTTP: GET
	// URL: http://localhost:8080/operador/usuario1
	// Body:
	@ApiOperation(value = "Obtiene el operador con usuario dado", notes = "Este endpoint permite obtener un operador con usuario dado en el path")
	@GetMapping("/operador/{usuario}")
	public ResponseEntity<Operador> getOperador(@PathVariable(value = "usuario") String usuario ){
		
		Connection conn = null;
		PreparedStatement ps = null;
		Operador mioperador = null;
		ResponseEntity<Operador> respuesta = null;

		try {
			// Acceso a la BBDD
			Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
			// Se busca en la BBDD el operador con usuario
			ps = conn.prepareStatement("SELECT * FROM operadores WHERE usuario =?");
			ps.setString(1, usuario);
			ResultSet rs = ps.executeQuery();

			if (rs.next()) { //Comprobamos si se ha encontrado
				mioperador = new Operador(rs.getString("usuario"),rs.getString("nombre"), 
				rs.getString("apellidos"), rs.getString("contrasena"), rs.getBoolean("activo"), rs.getBoolean("ocupado"));
			}

		} catch (SQLException | ClassNotFoundException e){ //Manejo de excepciones
		System.out.println("operadorController: Excepción:" + e.getMessage());
		} finally {
			try {
				if ( ps != null ) ps.close();
				if ( conn != null ) conn.close(); 
			} catch (SQLException e){
				System.out.println("operadorController: Excepción SQLException cerrando recursos:" + e.getMessage());
			}
		}

		if (mioperador != null) { //Se devuelve el operador solicitado o error
			System.out.println("Encontrada el operador con usuario: " + usuario);
			respuesta=  new ResponseEntity<>(mioperador, HttpStatus.OK);
		} else{
			respuesta=  new ResponseEntity<>( HttpStatus.INTERNAL_SERVER_ERROR);    
		}

		return respuesta;
	}


 
    // Actualiza un operador con un usuario dado en el path
    // Método HTTP: PUT
    // URL: http:localhost:8080/operador/usuario1
    // Body: Datos nuevos del operador
	@ApiOperation(value = "Actualiza un operador con un usuario dado en el path", notes = "Este endpoint permite actualizar un operador con usuario dado en el path a partir del body de la petición")
    @PutMapping("/operador/{usuario}")
    public ResponseEntity<String> updateOperador(@PathVariable(value = "usuario") String usuario, @RequestBody Operador nuevoOperador) {
	
		Connection conn = null;
		PreparedStatement ps = null;
		int resultado = 0;
		ResponseEntity<String> respuesta = null;

		try {
			// Acceso a la BBDD
			Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
			// Se actualiza el operador correspondiente al usuario
			ps = conn.prepareStatement("UPDATE operadores SET nombre = ?, apellidos = ?, contrasena = ?, activo = ?, ocupado = ? WHERE usuario = ?");
			ps.setString(1, nuevoOperador.getNombre());
			ps.setString(2, nuevoOperador.getApellidos());
			ps.setString(3, nuevoOperador.getContrasena());
			ps.setBoolean(4, nuevoOperador.getActivo());
			ps.setBoolean(5, nuevoOperador.getOcupado());
			ps.setString(6, usuario);
			resultado = ps.executeUpdate();
			
		}catch (SQLException | ClassNotFoundException e){ //manejo de excepciones
			System.out.println("operadorController: Excepción:" + e.getMessage());
		} finally {
			try {
				if ( ps != null ) ps.close();
				if ( conn != null ) conn.close(); 
				} catch (SQLException e){
					System.out.println("operadorController: Excepción SQLException cerrando recursos:" + e.getMessage());
			}
		}

		//Por compatibilidad con las aplicaciones móviles desarrolladas se devuelve un objeto JSON
		// convertido a string en lugar de un string directamente, tanto en caso de éxito como en caso de error
		JSONObject json = new JSONObject();
		if (resultado >= 1) {
			json.put("message", "Actualizado");
			System.out.println("Actualizado el operador con usuario: " + usuario);
			respuesta = new ResponseEntity<>(json.toString(), HttpStatus.OK);
		} else {
			json.put("message", "No actualizado");
			System.out.println("No se ha podido actualizar el operador con usuario: " + usuario);
			respuesta =  new ResponseEntity<>(json.toString(), HttpStatus.NOT_MODIFIED);
		}

		return respuesta;
	}



}
