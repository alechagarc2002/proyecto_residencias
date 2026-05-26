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
import itch.tsp.modelo.AsesorExterno;
import itch.tsp.modelo.Usuario;
import itch.tsp.repository.AsesorExternoRepository;
import itch.tsp.service.IAsesorExternoService;
import itch.tsp.service.IEmpresaService;
import itch.tsp.service.IUsuarioService;

@RequestMapping("/asesorExterno")
@Controller
public class AsesorExternoController {

	@Autowired
	private IAsesorExternoService asesorExternoService;

	@Autowired
	private IEmpresaService empresaService;

	@Autowired
	private IUsuarioService usuarioService;

	@Autowired
	private AsesorExternoRepository asesorExternoRepository;

	@Autowired
	private ArchivoStorageService archivoStorageService;

	@GetMapping("/mostrarAsesoresExternos")
	public String mostrarDatos(
			@RequestParam(name = "nombre", required = false) String nombre,
			Model model) {

		List<AsesorExterno> lista = asesorExternoService.buscarAsesoresExternosActivosPorNombre(nombre);
		model.addAttribute("asesorExterno", lista);
		model.addAttribute("nombre", nombre);

		return "asesorExterno/datosAsesorExterno";
	}

	@GetMapping("/estatusAsesoresExternos")
	public String mostrarEstatusAsesoresExternos(
			@RequestParam(name = "nombre", required = false) String nombre,
			Model model) {

		List<AsesorExterno> lista = asesorExternoService.buscarTodosAsesoresExternosPorNombre(nombre);
		model.addAttribute("asesorExterno", lista);
		model.addAttribute("nombre", nombre);

		return "asesorExterno/estatusAsesorExterno";
	}

	@GetMapping("/formulario")
	public String mostrarFormulario(Model model) {
		model.addAttribute("asesorExterno", new AsesorExterno());
		model.addAttribute("empresas", empresaService.buscarTodasEmp());
		model.addAttribute("usuariosDisponibles", obtenerUsuariosAsesorExternoDisponibles(null));
		return "fragmento/formAsesorExterno";
	}

	@PostMapping("/guardar")
	public String guardarAsesorExterno(AsesorExterno asesorExterno,
	        @RequestParam("archivo") MultipartFile archivo,
	        RedirectAttributes redirectAttributes) {

	    try {
	    	if (asesorExterno.getUsuario() != null && asesorExterno.getUsuario().getId() != null) {
	    		boolean yaExiste = asesorExternoRepository.existsByUsuario_IdAndEstatus(asesorExterno.getUsuario().getId(), 1);

	    		if (asesorExterno.getIdAsesorExterno() == null && yaExiste) {
	    			redirectAttributes.addFlashAttribute("msg", "Ese usuario ya está vinculado a otro asesor externo");
	    			return "redirect:/asesorExterno/formulario";
	    		}

	    		if (asesorExterno.getIdAsesorExterno() != null) {
	    			AsesorExterno asesorBD = asesorExternoService.buscarPorIdAsExt(asesorExterno.getIdAsesorExterno());
	    			if (asesorBD != null && asesorBD.getUsuario() != null) {
	    				Integer usuarioActual = asesorBD.getUsuario().getId();
	    				if (!usuarioActual.equals(asesorExterno.getUsuario().getId()) && yaExiste) {
	    					redirectAttributes.addFlashAttribute("msg", "Ese usuario ya está vinculado a otro asesor externo");
	    					return "redirect:/asesorExterno/editar/" + asesorExterno.getIdAsesorExterno();
	    				}
	    			}
	    		}
	    	}

	        File directorio = archivoStorageService.getRutaDirectorio("asesoresExternos").toFile();

	        if (archivo != null && !archivo.isEmpty()) {
	            String nombreArchivo = System.currentTimeMillis() + "_" + archivo.getOriginalFilename().replace(" ", "_");
	            File destino = archivoStorageService.getRutaArchivo("asesoresExternos", nombreArchivo).toFile();

	            archivo.transferTo(destino);
	            asesorExterno.setFoto(nombreArchivo);
	        } else {
	            if (asesorExterno.getFoto() == null || asesorExterno.getFoto().isBlank()) {
	                asesorExterno.setFoto("noFoto.jpeg");
	            }
	        }

	        if (asesorExterno.getEstatus() == null) {
	            asesorExterno.setEstatus(1);
	        }

	        asesorExternoService.guardarAsesorExterno(asesorExterno);
	        redirectAttributes.addFlashAttribute("msg", "Asesor externo guardado correctamente");
	        return "redirect:/asesorExterno/mostrarAsesoresExternos";

	    } catch (Exception e) {
	        e.printStackTrace();
	        redirectAttributes.addFlashAttribute("msg", "Ocurrió un error al guardar la imagen o el asesor externo");
	        return "redirect:/asesorExterno/formulario";
	    }
	}

	@GetMapping("/ver/{id}")
	public String verDetalleAsesorExterno(@PathVariable("id") Integer id, Model model) {
		AsesorExterno asesorExterno = asesorExternoService.buscarPorIdAsExt(id);
		model.addAttribute("asesorExterno", asesorExterno);
		return "asesorExterno/detalleAsesorExterno";
	}

	@GetMapping("/editar/{id}")
	public String editarAsesorExterno(@PathVariable("id") Integer id, Model model) {
		AsesorExterno asesorExterno = asesorExternoService.buscarPorIdAsExt(id);
		model.addAttribute("asesorExterno", asesorExterno);
		model.addAttribute("empresas", empresaService.buscarTodasEmp());
		model.addAttribute("usuariosDisponibles", obtenerUsuariosAsesorExternoDisponibles(asesorExterno));
		return "fragmento/formAsesorExterno";
	}

	@GetMapping("/eliminar/{id}")
	public String eliminarAsesorExterno(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {

		if (asesorExternoService.asesorExternoTieneVinculoActivo(id)) {
			redirectAttributes.addFlashAttribute("msg",
					"No se puede desactivar el asesor externo porque está vinculado a un proyecto de residencia activo");
			return "redirect:/asesorExterno/mostrarAsesoresExternos";
		}

		asesorExternoService.eliminarAsesorExterno(id);
		redirectAttributes.addFlashAttribute("msg", "Asesor externo desactivado correctamente");
		return "redirect:/asesorExterno/mostrarAsesoresExternos";
	}

	@GetMapping("/activar/{id}")
	public String activarAsesorExterno(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
		asesorExternoService.activarAsesorExterno(id);
		redirectAttributes.addFlashAttribute("msg", "Asesor externo activado correctamente");
		return "redirect:/asesorExterno/estatusAsesoresExternos";
	}

	private List<Usuario> obtenerUsuariosAsesorExternoDisponibles(AsesorExterno asesorActual) {
		return usuarioService.buscarTodosActivos().stream()
				.filter(u -> u.getRol() != null && "ASESOR_EXTERNO".equalsIgnoreCase(u.getRol().getNombre()))
				.filter(u -> !asesorExternoRepository.existsByUsuario_IdAndEstatus(u.getId(), 1)
						|| (asesorActual != null
								&& asesorActual.getUsuario() != null
								&& asesorActual.getUsuario().getId().equals(u.getId())))
				.collect(Collectors.toList());
	}
}
