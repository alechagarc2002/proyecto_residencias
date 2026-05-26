package itch.tsp.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CrearDirectoriosConfig {

	@Bean
	CommandLineRunner crearDirectorios(ArchivoStorageService archivoStorageService) {
		return args -> {
			archivoStorageService.getRutaDirectorio("residentes");
			archivoStorageService.getRutaDirectorio("asesoresInternos");
			archivoStorageService.getRutaDirectorio("asesoresExternos");
			archivoStorageService.getRutaDirectorio("documentos");
			archivoStorageService.getRutaDirectorio("pdf");

			System.out.println("Directorios de residencias verificados correctamente");
		};
	}
}
