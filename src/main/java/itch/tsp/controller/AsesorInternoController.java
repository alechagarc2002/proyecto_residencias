package itch.tsp.controller;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import itch.tsp.config.ArchivoStorageService;
import itch.tsp.modelo.AsesorInterno;
import itch.tsp.modelo.Usuario;
import itch.tsp.repository.AsesorInternoRepository;
import itch.tsp.service.IAsesorInternoService;
import itch.tsp.service.IDepartamentoAcademicoService;
import itch.tsp.service.IUsuarioService;

@RequestMapping("/asesorInterno")
@Controller
public class AsesorInternoController {

	@Autowired
	private IAsesorInternoService asesorInternoService;

	@Autowired
	private IDepartamentoAcademicoService departamentoService;

	@Autowired
	private IUsuarioService usuarioService;

	@Autowired
	private AsesorInternoRepository asesorInternoRepository;

	@Autowired
	private ArchivoStorageService archivoStorageService;

	@GetMapping("/mostrarAsesoresInternos")
	public String mostrarDatos(
			@RequestParam(name = "nombre", required = false) String nombre,
			Model model) {

		List<AsesorInterno> lista = asesorInternoService.buscarAsesoresInternosActivosPorNombre(nombre);
		model.addAttribute("asesorInterno", lista);
		model.addAttribute("nombre", nombre);

		return "asesorInterno/datosAsesorInterno";
	}

	@GetMapping("/estatusAsesoresInternos")
	public String mostrarEstatusAsesoresInternos(
			@RequestParam(name = "nombre", required = false) String nombre,
			Model model) {

		List<AsesorInterno> lista = asesorInternoService.buscarTodosAsesoresInternosPorNombre(nombre);
		model.addAttribute("asesorInterno", lista);
		model.addAttribute("nombre", nombre);

		return "asesorInterno/estatusAsesorInterno";
	}

	@GetMapping("/formulario")
	public String mostrarFormulario(Model model) {
		model.addAttribute("asesorInterno", new AsesorInterno());
		model.addAttribute("departamentos", departamentoService.buscarTodosDep());
		model.addAttribute("usuariosDisponibles", obtenerUsuariosAsesorInternoDisponibles(null));
		return "asesorInterno/formAsesorInterno";
	}

	@PostMapping("/guardar")
	public String guardarAsesorInterno(AsesorInterno asesorInterno,
	        @RequestParam("archivo") MultipartFile archivo,
	        RedirectAttributes redirectAttributes) {

	    try {
	    	if (asesorInterno.getUsuario() != null && asesorInterno.getUsuario().getId() != null) {
	    		boolean yaExiste = asesorInternoRepository.existsByUsuario_IdAndEstatus(asesorInterno.getUsuario().getId(), 1);

	    		if (asesorInterno.getIdAsesorInterno() == null && yaExiste) {
	    			redirectAttributes.addFlashAttribute("msg", "Ese usuario ya está vinculado a otro asesor interno");
	    			return "redirect:/asesorInterno/formulario";
	    		}

	    		if (asesorInterno.getIdAsesorInterno() != null) {
	    			AsesorInterno asesorBD = asesorInternoService.buscarPorIdAsInt(asesorInterno.getIdAsesorInterno());
	    			if (asesorBD != null && asesorBD.getUsuario() != null) {
	    				Integer usuarioActual = asesorBD.getUsuario().getId();
	    				if (!usuarioActual.equals(asesorInterno.getUsuario().getId()) && yaExiste) {
	    					redirectAttributes.addFlashAttribute("msg", "Ese usuario ya está vinculado a otro asesor interno");
	    					return "redirect:/asesorInterno/editar/" + asesorInterno.getIdAsesorInterno();
	    				}
	    			}
	    		}
	    	}

	        File directorio = archivoStorageService.getRutaDirectorio("asesoresInternos").toFile();

	        if (archivo != null && !archivo.isEmpty()) {
	            String nombreArchivo = System.currentTimeMillis() + "_" + archivo.getOriginalFilename().replace(" ", "_");
	            File destino = archivoStorageService.getRutaArchivo("asesoresInternos", nombreArchivo).toFile();

	            archivo.transferTo(destino);
	            asesorInterno.setFoto(nombreArchivo);
	        } else {
	            if (asesorInterno.getFoto() == null || asesorInterno.getFoto().isBlank()) {
	                asesorInterno.setFoto("noFoto.jpeg");
	            }
	        }

	        if (asesorInterno.getEstatus() == null) {
	            asesorInterno.setEstatus(1);
	        }

	        asesorInternoService.guardarAsesorInterno(asesorInterno);
	        redirectAttributes.addFlashAttribute("msg", "Asesor interno guardado correctamente");
	        return "redirect:/asesorInterno/mostrarAsesoresInternos";

	    } catch (Exception e) {
	        e.printStackTrace();
	        redirectAttributes.addFlashAttribute("msg", "Ocurrió un error al guardar la imagen o el asesor interno");
	        return "redirect:/asesorInterno/formulario";
	    }
	}

	@GetMapping("/editar/{id}")
	public String editarAsesorInterno(@PathVariable("id") Integer id, Model model) {
		AsesorInterno asesorInterno = asesorInternoService.buscarPorIdAsInt(id);
		model.addAttribute("asesorInterno", asesorInterno);
		model.addAttribute("departamentos", departamentoService.buscarTodosDep());
		model.addAttribute("usuariosDisponibles", obtenerUsuariosAsesorInternoDisponibles(asesorInterno));
		return "asesorInterno/formAsesorInterno";
	}

	@GetMapping("/eliminar/{id}")
	public String eliminarAsesorInterno(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {

		if (asesorInternoService.asesorInternoTieneVinculoActivo(id)) {
			redirectAttributes.addFlashAttribute("msg",
					"No se puede desactivar el asesor interno porque está vinculado a un proyecto de residencia activo");
			return "redirect:/asesorInterno/mostrarAsesoresInternos";
		}

		asesorInternoService.eliminarAsesorInterno(id);
		redirectAttributes.addFlashAttribute("msg", "Asesor interno desactivado correctamente");
		return "redirect:/asesorInterno/mostrarAsesoresInternos";
	}

	@GetMapping("/activar/{id}")
	public String activarAsesorInterno(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
		asesorInternoService.activarAsesorInterno(id);
		redirectAttributes.addFlashAttribute("msg", "Asesor interno activado correctamente");
		return "redirect:/asesorInterno/estatusAsesoresInternos";
	}

	@GetMapping("/ver/{id}")
	public String verDetalleAsesorInterno(@PathVariable("id") Integer id, Model model) {
	    AsesorInterno asesorInterno = asesorInternoService.buscarPorIdAsInt(id);
	    model.addAttribute("asesorInterno", asesorInterno);
	    return "asesorInterno/detalleAsesorInterno";
	}

	private List<Usuario> obtenerUsuariosAsesorInternoDisponibles(AsesorInterno asesorActual) {
		return usuarioService.buscarTodosActivos().stream()
				.filter(u -> u.getRol() != null && "ASESOR_INTERNO".equalsIgnoreCase(u.getRol().getNombre()))
				.filter(u -> !asesorInternoRepository.existsByUsuario_IdAndEstatus(u.getId(), 1)
						|| (asesorActual != null
								&& asesorActual.getUsuario() != null
								&& asesorActual.getUsuario().getId().equals(u.getId())))
				.collect(Collectors.toList());
	}
}
