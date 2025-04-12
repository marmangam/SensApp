/**
 * Clase principal de la aplicación Spring Boot para el proyecto.
 * 
 * Esta clase contiene el método main, que es el punto de entrada de la aplicación.
 *  
 * Método:
 * - main: Inicia la aplicación utilizando SpringApplication.run.
 */

package tfg;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class Application {

    public static void main(String[] args) {
	// Iniciamos la aplicación
	SpringApplication.run(Application.class, args);
    }
}

