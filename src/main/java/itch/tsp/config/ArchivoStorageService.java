package itch.tsp.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ArchivoStorageService {

	private final Path rutaRaiz;

	public ArchivoStorageService(@Value("${ruta.archivos:${user.home}/residencias}") String rutaArchivos) {
		this.rutaRaiz = Path.of(rutaArchivos).toAbsolutePath().normalize();
	}

	public Path getRutaRaiz() {
		return rutaRaiz;
	}

	public Path getRutaDirectorio(String nombreDirectorio) {
		try {
			Path directorio = rutaRaiz.resolve(nombreDirectorio).normalize();
			Files.createDirectories(directorio);
			return directorio;
		} catch (IOException e) {
			throw new IllegalStateException("No se pudo preparar el directorio: " + nombreDirectorio, e);
		}
	}

	public Path getRutaArchivo(String nombreDirectorio, String nombreArchivo) {
		return getRutaDirectorio(nombreDirectorio).resolve(nombreArchivo).normalize();
	}

	public String getUbicacionResourceHandler() {
		String ubicacion = rutaRaiz.toUri().toString();
		return ubicacion.endsWith("/") ? ubicacion : ubicacion + "/";
	}
}
