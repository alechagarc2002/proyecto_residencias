package itch.tsp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class RutaImagenesConfig implements WebMvcConfigurer {

	private final ArchivoStorageService archivoStorageService;

	public RutaImagenesConfig(ArchivoStorageService archivoStorageService) {
		this.archivoStorageService = archivoStorageService;
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/residencias/**")
				.addResourceLocations(archivoStorageService.getUbicacionResourceHandler());
	}
}
