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
 * Clase controladora para la gestión de ADMINISTRATIVOS.
 * 
 * Proporciona endpoints REST para crear, obtener, actualizar y eliminar administrativos.
 * 
 * Métodos:
 * - creaAdministrativo: Crea un nuevo administrativo a partir del body de la petición.
 * - getAdministrativo: Obtiene un administrativo a partir del usuario y la contraseña.
 * - obtenerAdministrativos: Obtiene una lista con todos los administrativos.
 * - deleteAdministrativo: Elimina un administrativo a partir del usuario del PATH.
 * - getAdministrativo: Obtiene un administrativo a partir del usuario del PATH.
 * - updateAdministrativo: Actualiza un administrativo con usuario dado por parámetros a partir del body de la petición.
 *
 */

@RestController
public class AdministrativoController {

    // Se definen las variables para las conexiones a la base de datos
    private static final String dbUser ="sensapp";
    private static final String dbPassword = "sensapptfg66&";
    private static final String dbUrl = "jdbc:postgresql://database-sensapp.cz4i4mewyc6r.us-east-1.rds.amazonaws.com:5432/databasesensapp";
 


    // Crea un nuevo administrativo
    // Método HTTP: POST 
    // URL: http://localhost:8080/administrativo
    // Body: Administrativo a crear
	@ApiOperation(value = "Crea un nuevo administrativo", notes = "Este endpoint permite crear un nuevo administrativo a partir del body de la petición")
    @PostMapping(path = "/administrativo")
    public ResponseEntity<String> creaAdministrativo(@RequestBody Administrativo nuevoAdministrativo){
	
		
		ResponseEntity<String> respuesta = null;
		int resultadoExe = 0;
		Connection conn = null;
		PreparedStatement ps = null;
		
		try{
			//Acceso a la BBDD
			Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection(dbUrl, dbUser,dbPassword);
			//Se crea el nuevo administrativo en la BBDD
			ps = conn.prepareStatement("INSERT INTO administrativos (usuario, nombre, apellidos, contrasena) VALUES (?,?,?,?)");
			ps.setString(1, nuevoAdministrativo.getUsuario());
			ps.setString(2, nuevoAdministrativo.getNombre());
			ps.setString(3, nuevoAdministrativo.getApellidos());
			ps.setString(4, nuevoAdministrativo.getContrasena());

			resultadoExe =ps.executeUpdate();
	
		} catch (SQLException | ClassNotFoundException e){ //Manejo de excepciones
			System.out.println("administrativoController: Excepción:" + e.getMessage());
		} finally{
			try {
				if ( ps != null ) ps.close();
				if ( conn != null ) conn.close(); 
				} catch (SQLException e){
				System.out.println("administrativoController: Excepción SQLException cerrando recursos: " + e.getMessage());
				}
		}

		//Por compatibilidad con las aplicaciones móviles desarrolladas se devuelve un objeto JSON
		// convertido a string en lugar de un string directamente, tanto en caso de éxito como en caso de error
		JSONObject json = new JSONObject();
		if (resultadoExe >= 1){
			json.put("message", "Insertado");
			respuesta = new ResponseEntity<>(json.toString(), HttpStatus.CREATED);
			System.out.println("Se ha insertado el administrativo con resultado: " + resultadoExe);
		}
		else{	
			json.put("message", "No insertado");
			respuesta = new ResponseEntity<>(json.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return respuesta; 
    }



    // Obtiene un administrativo con el usuario y la contraseña dados por parámetros
    // Método HTTP: GET
    // URL: http://localhost:8080/administrativo?usuario=usuario1&contrasena=contrasena1
    // Body:
	@ApiOperation(value = "Obtiene un administrativo con el usuario y la contraseña dados por parámetros", notes = "Este endpoint permite obtener un administrativo a partir del usuario y la contraseña")
    @GetMapping("/administrativo")
    public ResponseEntity<Administrativo> getAdministrativo(@RequestParam String usuario, @RequestParam String contrasena) {
	
		Administrativo administrativo = null;
		ResponseEntity<Administrativo> respuesta = null;
		Connection conn = null;
		PreparedStatement ps = null;

		try {
			// Acceso a la BBDD
			Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
			//Se busca en la BBDD el administrativo con el usuario y contraseña dados
			ps =conn.prepareStatement("SELECT * FROM administrativos WHERE usuario =? AND contrasena=?");
			ps.setString(1, usuario);
			ps.setString(2, contrasena);

			try (ResultSet rs = ps.executeQuery()){
				if (rs.next()) { //Comprobamos si se ha encontrado
				administrativo = new Administrativo(rs.getString("usuario"),rs.getString("nombre"), 
								rs.getString("apellidos"), rs.getString("contrasena") );
				}
			}

		} catch (SQLException | ClassNotFoundException e){ //Manejo de excepciones
			System.out.println("administrativoController: Excepción:" + e.getMessage());
		}finally{
			try {
				if ( ps != null ) ps.close();
				if ( conn != null ) conn.close(); 
				} catch (SQLException e){
				System.out.println("administrativoController: Excepción SQLException cerrando recursos: " + e.getMessage());
				}
		}

		if (administrativo != null) {//Se devuelve el administrativo solicitado o un error
			System.out.println("Encontrado el administrativo con usuario: " + usuario + "y contrasena: " + contrasena);
			respuesta=  new ResponseEntity<>(administrativo, HttpStatus.OK);
		} else{
			respuesta=  new ResponseEntity<>( HttpStatus.INTERNAL_SERVER_ERROR);    
		}

		return respuesta;
    }


	// Obtiene una lista de administrativos
    // Método HTTP: GET
    // URL: http://localhost:8080/administrativo/lista
    // Body:
	@ApiOperation(value = "Obtiene una lista de administrativos", notes = "Este endpoint permite obtener una lista con todos los administrativos")
    @GetMapping("/administrativo/lista")
    public List<Administrativo> obtenerAdministrativos() {

		Connection conn = null;
		PreparedStatement ps = null;
		List<Administrativo> administrativos = new ArrayList<>();

		try {
			// Acceso a la BBDD
			Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
			//Se buscan los administrativos en la BBDD
			ps= conn.prepareStatement("SELECT * FROM administrativos");
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {//Añadimos a la lista los que se hayan encontrado
				Administrativo administrativo = new Administrativo(rs.getString("usuario"),
				rs.getString("nombre"), rs.getString("apellidos"), rs.getString("contrasena"));
				administrativos.add(administrativo);
			}

		} catch (SQLException | ClassNotFoundException e){ //Manejo de excepciones
			System.out.println("administrativoController: Excepción:" + e.getMessage());
		} finally {
			try {
				if ( ps != null ) ps.close();
				if ( conn != null ) conn.close(); 
			} catch (SQLException e){
				System.out.println("administrativoController: Excepción SQLException cerrando recursos: " + e.getMessage());
			}
		}
		
		if (!administrativos.isEmpty()){
			System.out.println("Encontrados administrativos.");
		} else{
			System.out.println("No se ha encontrado ningun administrativo.");
		}

		return administrativos; //Se devuelve la lista de administrativos solicitada
	}


	// Elimina un administrativo dado su usuario
	// Metodo HTTP: DELETE 
	// Ejemplo URI: http://localhost:8080/administrativo/usuario1
	@ApiOperation(value = "Elimina un administrativo dado su usuario", notes = "Este endpoint permite eliminar un administrativo a partir del usuario del path")
	@DeleteMapping("/administrativo/{usuario}")
	public ResponseEntity<String> 
		deleteAdministrativo(@PathVariable(value = "usuario") String usuario) {

		ResponseEntity<String> resultado = null;
		Connection conn = null;
		PreparedStatement ps = null;
		int numRegBorrados = 0;

		try{
			// Acceso a la BBDD
			Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
			//Borrado del administrativo
			ps = conn.prepareStatement("DELETE FROM administrativos  WHERE usuario =?");
			ps.setString(1, usuario);
			numRegBorrados = ps.executeUpdate();

		} catch (SQLException | ClassNotFoundException e){ //Manejo de excepciones
		System.out.println("administrativoController: Excepción:" + e.getMessage());
		} finally {
			try{
				if (ps != null) ps.close();
				if (conn != null) conn.close(); 
			} catch (SQLException e){
				System.out.println("administrativoController: Excepción SQLException cerrando recursos" + e.getMessage());
			}
		}

		//Por compatibilidad con las aplicaciones móviles desarrolladas se devuelve un objeto JSON
		// convertido a string en lugar de un string directamente, tanto en caso de éxito como en caso de error
		JSONObject json = new JSONObject();
		if (numRegBorrados >= 1){
			json.put("message", "Eliminado");
			System.out.println("Eliminado el administrativo con  usuario:  " + usuario);
			resultado = new ResponseEntity<>(json.toString(), HttpStatus.OK);
		}
		else {
			json.put("message", "No eliminado");
			System.out.println("NO eliminado el administrativo con usuario: " + usuario);
			resultado = new ResponseEntity<>(json.toString(),HttpStatus.NOT_FOUND);
		}

		return resultado;
	}


	// Obtiene el administrativo cuyo  usuario se pasa en el path
	// Método HTTP: GET
	// URL: http://localhost:8080/administrativo/usuario1
	// Body:
	@ApiOperation(value = "Obtiene el administrativo cuyo  usuario se pasa en el path", notes = "Este endpoint permite obtener un administrativo a partir del usuario del path")
	@GetMapping("/administrativo/{usuario}")
	public ResponseEntity<Administrativo> getAdministrativo(@PathVariable(value = "usuario") String usuario ){
		
		Connection conn = null;
		PreparedStatement ps = null;
		Administrativo miadministrativo = null;
		ResponseEntity<Administrativo> respuesta = null;

		try {
			// Acceso a la BBDD
			Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
			//Se busca el administrativo con usuario dado en la BBDD
			ps = conn.prepareStatement("SELECT * FROM administrativos WHERE usuario =?");
			ps.setString(1, usuario);
			ResultSet rs = ps.executeQuery();

			if (rs.next()) { //Comprobamos si se ha encontrado
				miadministrativo = new Administrativo(rs.getString("usuario"),rs.getString("nombre"), 
				rs.getString("apellidos"), rs.getString("contrasena"));
			}

		} catch (SQLException | ClassNotFoundException e){ //Manejo de excepciones
		System.out.println("administrativoController: Excepción:" + e.getMessage());
		} finally {
			try {
				if ( ps != null ) ps.close();
				if ( conn != null ) conn.close(); 
			} catch (SQLException e){
				System.out.println("administrativoController: Excepción SQLException cerrando recursos:" + e.getMessage());
			}
		}

		if (miadministrativo != null) { //Se devuelve el administrativo solicitado o error
			System.out.println("Encontrada el administrativo con usuario: " + usuario);
			respuesta=  new ResponseEntity<>(miadministrativo, HttpStatus.OK);
		} else{
			respuesta=  new ResponseEntity<>( HttpStatus.INTERNAL_SERVER_ERROR);    
		}

		return respuesta;
	}


 
    // Actualiza un administrativo dado su usuario en el path
    // Método HTTP: PUT
    // URL: http:localhost:8080/administrativo/usuario1
    // Body: Datos nuevos del administrativo
	@ApiOperation(value = "Actualiza un administrativo dado su usuario en el path", notes = "Este endpoint permite actualizar un administrativo con usuario dado en el path a partir del body de la petición")
    @PutMapping("/administrativo/{usuario}")
    public ResponseEntity<String> updateAdministrativo(@PathVariable(value = "usuario") String usuario, @RequestBody Administrativo nuevoAdministrativo) {
	
		Connection conn = null;
		PreparedStatement ps = null;
		int resultado = 0;
		ResponseEntity<String> respuesta = null;

		try {
			// Acceso a la BBDD
			Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
			//Se actualiza el administrativo correspondiente al usuario en la BBDD
			ps = conn.prepareStatement("UPDATE administrativos SET nombre = ?, apellidos = ?,contrasena = ? WHERE usuario = ?");
			ps.setString(1, nuevoAdministrativo.getNombre());
			ps.setString(2, nuevoAdministrativo.getApellidos());
			ps.setString(3, nuevoAdministrativo.getContrasena());
			ps.setString(4, usuario);
			resultado = ps.executeUpdate();

			
		}catch (SQLException | ClassNotFoundException e){ //Manejo de excepciones
			System.out.println("administrativoController: Excepción:" + e.getMessage());
		} finally {
			try {
				if ( ps != null ) ps.close();
				if ( conn != null ) conn.close(); 
				} catch (SQLException e){
					System.out.println("administrativoController: Excepción SQLException cerrando recursos: " + e.getMessage());
				}
		}

		//Por compatibilidad con las aplicaciones móviles desarrolladas se devuelve un objeto JSON
		// convertido a string en lugar de un string directamente, tanto en caso de éxito como en caso de error
		JSONObject json = new JSONObject();
		if (resultado >= 1) {
			json.put("message", "Actualizado");
			System.out.println("Actualizado el administrativo con usuario: " + usuario);
			respuesta = new ResponseEntity<>(json.toString(), HttpStatus.OK);
		} else {
			json.put("message", "No actualizado");
			System.out.println("No se ha podido actualizar el administrativo con usuario: " + usuario);
			respuesta = new ResponseEntity<>(json.toString(), HttpStatus.NOT_MODIFIED);
		}

		return respuesta;
    }



}
