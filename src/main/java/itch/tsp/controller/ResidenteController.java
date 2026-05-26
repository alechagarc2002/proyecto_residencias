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
import itch.tsp.modelo.Residente;
import itch.tsp.modelo.Usuario;
import itch.tsp.repository.ResidenteRepository;
import itch.tsp.service.ICarreraService;
import itch.tsp.service.IResidenteService;
import itch.tsp.service.IUsuarioService;

@RequestMapping("/residente")
@Controller
public class ResidenteController {

	@Autowired
	private IResidenteService residenteService;

	@Autowired
	private ICarreraService carreraService;

	@Autowired
	private IUsuarioService usuarioService;

	@Autowired
	private ResidenteRepository residenteRepository;

	@Autowired
	private ArchivoStorageService archivoStorageService;

	@GetMapping("/mostrarResidentes")
	public String mostrarDatos(
	        @RequestParam(name = "nombre", required = false) String nombre,
	        @RequestParam(name = "filtroProyecto", required = false) String filtroProyecto,
	        Model model) {

	    List<Residente> lista;

	    if ("CON_PROYECTO".equals(filtroProyecto)) {
	        lista = residenteService.buscarResidentesConProyectoPorNombre(nombre);
	    } else if ("SIN_PROYECTO".equals(filtroProyecto)) {
	        lista = residenteService.buscarResidentesSinProyectoPorNombre(nombre);
	    } else {
	        lista = residenteService.buscarResidentesActivosPorNombre(nombre);
	    }

	    model.addAttribute("residente", lista);
	    model.addAttribute("nombre", nombre);
	    model.addAttribute("filtroProyecto", filtroProyecto);

	    return "residente/datosResidente";
	}

	@GetMapping("/estatusResidentes")
	public String mostrarEstatusResidentes(
			@RequestParam(name = "nombre", required = false) String nombre,
			Model model) {

		List<Residente> lista = residenteService.buscarTodosResidentesPorNombre(nombre);
		model.addAttribute("residente", lista);
		model.addAttribute("nombre", nombre);

		return "residente/estatusResidente";
	}

	@GetMapping("/formulario")
	public String mostrarFormulario(Model model) {
		model.addAttribute("residente", new Residente());
		model.addAttribute("carreras", carreraService.buscarTodasCar());
		model.addAttribute("usuariosDisponibles", obtenerUsuariosResidenteDisponibles(null));
		return "residente/formResidente";
	}

	@PostMapping("/guardar")
	public String guardarResidente(Residente residente,
	        @RequestParam("archivo") MultipartFile archivo,
	        RedirectAttributes redirectAttributes,
	        Model model) {

	    try {
	    	if (residente.getUsuario() != null && residente.getUsuario().getId() != null) {
	    		boolean yaExiste = residenteRepository.existsByUsuario_IdAndEstatus(residente.getUsuario().getId(), 1);

	    		if (residente.getIdResidente() == null && yaExiste) {
	    			redirectAttributes.addFlashAttribute("msg", "Ese usuario ya está vinculado a otro residente");
	    			return "redirect:/residente/formulario";
	    		}

	    		if (residente.getIdResidente() != null) {
	    			Residente residenteBD = residenteService.buscarPorIdRes(residente.getIdResidente());
	    			if (residenteBD != null && residenteBD.getUsuario() != null) {
	    				Integer usuarioActual = residenteBD.getUsuario().getId();
	    				if (!usuarioActual.equals(residente.getUsuario().getId()) && yaExiste) {
	    					redirectAttributes.addFlashAttribute("msg", "Ese usuario ya está vinculado a otro residente");
	    					return "redirect:/residente/editar/" + residente.getIdResidente();
	    				}
	    			}
	    		}
	    	}

	        File directorio = archivoStorageService.getRutaDirectorio("residentes").toFile();

	        if (archivo != null && !archivo.isEmpty()) {
	            String nombreArchivo = System.currentTimeMillis() + "_" + archivo.getOriginalFilename().replace(" ", "_");
	            File destino = archivoStorageService.getRutaArchivo("residentes", nombreArchivo).toFile();

	            archivo.transferTo(destino);
	            residente.setFoto(nombreArchivo);
	        } else {
	            if (residente.getFoto() == null || residente.getFoto().isBlank()) {
	                residente.setFoto("noFoto.jpeg");
	            }
	        }

	        if (residente.getEstatus() == null) {
	            residente.setEstatus(1);
	        }

	        residenteService.guardarResidente(residente);
	        redirectAttributes.addFlashAttribute("msg", "Residente guardado correctamente");
	        return "redirect:/residente/mostrarResidentes";

	    } catch (Exception e) {
	        e.printStackTrace();
	        redirectAttributes.addFlashAttribute("msg", "Ocurrió un error al guardar la imagen o el residente");
	        return "redirect:/residente/formulario";
	    }
	}

	@GetMapping("/ver/{id}")
	public String verDetalleResidente(@PathVariable("id") Integer id, Model model) {
		Residente residente = residenteService.buscarPorIdRes(id);
		model.addAttribute("residente", residente);
		return "residente/detalleResidente";
	}

	@GetMapping("/editar/{id}")
	public String editarResidente(@PathVariable("id") Integer id, Model model) {
		Residente residente = residenteService.buscarPorIdRes(id);
		model.addAttribute("residente", residente);
		model.addAttribute("carreras", carreraService.buscarTodasCar());
		model.addAttribute("usuariosDisponibles", obtenerUsuariosResidenteDisponibles(residente));
		return "residente/formResidente";
	}

	@GetMapping("/eliminar/{id}")
	public String eliminarResidente(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {

		if (residenteService.residenteTieneVinculoActivo(id)) {
			redirectAttributes.addFlashAttribute("msg",
					"No se puede desactivar el residente porque está vinculado a una propuesta o proyecto activo");
			return "redirect:/residente/mostrarResidentes";
		}

		residenteService.eliminarResidente(id);
		redirectAttributes.addFlashAttribute("msg", "Residente desactivado correctamente");
		return "redirect:/residente/mostrarResidentes";
	}

	@GetMapping("/activar/{id}")
	public String activarResidente(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
		residenteService.activarResidente(id);
		redirectAttributes.addFlashAttribute("msg", "Residente activado correctamente");
		return "redirect:/residente/estatusResidentes";
	}

	private List<Usuario> obtenerUsuariosResidenteDisponibles(Residente residenteActual) {
		return usuarioService.buscarTodosActivos().stream()
				.filter(u -> u.getRol() != null && "RESIDENTE".equalsIgnoreCase(u.getRol().getNombre()))
				.filter(u -> !residenteRepository.existsByUsuario_IdAndEstatus(u.getId(), 1)
						|| (residenteActual != null
								&& residenteActual.getUsuario() != null
								&& residenteActual.getUsuario().getId().equals(u.getId())))
				.collect(Collectors.toList());
	}
}
