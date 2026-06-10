package com.cuidared;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Clase principal que inicializa la aplicación Spring Boot para CuidaRed.
 */
@SpringBootApplication
@EnableScheduling
public class CuidaRedApplication {

	public static void main(String[] args) {
		SpringApplication.run(CuidaRedApplication.class, args);
	}

}
