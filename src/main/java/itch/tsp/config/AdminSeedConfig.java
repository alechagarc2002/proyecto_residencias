package itch.tsp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import itch.tsp.modelo.Rol;
import itch.tsp.modelo.Usuario;
import itch.tsp.repository.RolRepository;
import itch.tsp.repository.UsuarioRepository;

@Configuration
public class AdminSeedConfig {

	@Bean
	CommandLineRunner crearAdminInicial(
			UsuarioRepository usuarioRepository,
			RolRepository rolRepository,
			PasswordEncoder passwordEncoder,
			@Value("${app.admin.enabled:true}") boolean adminEnabled,
			@Value("${app.admin.username:admin}") String adminUsername,
			@Value("${app.admin.password:123456}") String adminPassword) {
		return args -> {
			if (!adminEnabled) {
				return;
			}

			Rol rolJefeDivision = rolRepository.findByNombreIgnoreCase("JEFE_DIVISION");
			if (rolJefeDivision == null) {
				rolJefeDivision = new Rol();
				rolJefeDivision.setNombre("JEFE_DIVISION");
			}
			rolJefeDivision.setEstatus(1);
			rolJefeDivision = rolRepository.save(rolJefeDivision);

			Usuario admin = usuarioRepository.findByUsername(adminUsername);
			if (admin == null) {
				admin = new Usuario();
				admin.setUsername(adminUsername);
			}

			admin.setPassword(passwordEncoder.encode(adminPassword));
			admin.setEstatus(1);
			admin.setRol(rolJefeDivision);
			usuarioRepository.save(admin);
		};
	}
}
