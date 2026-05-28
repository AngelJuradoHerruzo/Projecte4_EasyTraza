package cat.copernic.easytraza;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * APLICACIÓ EASYTRAZA.
 *
 * Inicialitzada l'aplicació backend d'EasyTraza i el context de Spring Boot
 * necessari per executar els serveis i la interfície web del sistema.
 *
 * @author Ángel Jurado Herruzo
 */
@SpringBootApplication
public class EasytrazaApplication {


	/**
	 * INICI DE L'APLICACIÓ.
	 *
	 * Executat el punt d'entrada principal que arrenca l'aplicació
	 * i carrega la configuració gestionada per Spring Boot.
	 *
	 * @param args arguments rebuts durant l'execució de l'aplicació
	 */
	public static void main(String[] args) {
		SpringApplication.run(EasytrazaApplication.class, args);
	}

}
