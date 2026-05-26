package itch.tsp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import itch.tsp.modelo.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

	Usuario findByUsername(String username);

	Usuario findByUsernameAndEstatus(String username, Integer estatus);

	List<Usuario> findByEstatus(Integer estatus);

	boolean existsByRol_NombreAndEstatus(String nombreRol, Integer estatus);
}
