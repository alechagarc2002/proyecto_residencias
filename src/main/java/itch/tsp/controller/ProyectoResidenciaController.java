package itch.tsp.controller;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.List;

import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import itch.tsp.modelo.DocumentoResidencia;
import itch.tsp.modelo.EvaluacionReporteFinal;
import itch.tsp.modelo.PropuestaProyecto;
import itch.tsp.modelo.ProyectoBanco;
import itch.tsp.modelo.ProyectoResidencia;
import itch.tsp.modelo.SeguimientoResidencia;
import itch.tsp.service.IAsesorExternoService;
import itch.tsp.service.IAsesorInternoService;
import itch.tsp.service.IDocumentoResidenciaService;
import itch.tsp.service.IEmpresaService;
import itch.tsp.service.IEvaluacionReporteFinalService;
import itch.tsp.service.IPropuestaProyectoService;
import itch.tsp.service.IProyectoBancoService;
import itch.tsp.service.IProyectoResidenciaService;
import itch.tsp.service.IResidenteService;
import itch.tsp.service.ISeguimientoResidenciaService;
import itch.tsp.util.LiberacionAsesorInternoPdf;
import itch.tsp.util.ProyectoResidenciaPdf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import itch.tsp.config.ArchivoStorageService;

@RequestMapping("/proyectoResidencia")
@Controller
public class ProyectoResidenciaController {

	@Autowired
	private ArchivoStorageService archivoStorageService;

	@Autowired
	private ProyectoResidenciaPdf proyectoResidenciaPdf;

	@Autowired
	private IProyectoResidenciaService proyectoResidenciaService;

	@Autowired
	private IProyectoBancoService proyectoBancoService;

	@Autowired
	private IPropuestaProyectoService propuestaProyectoService;

	@Autowired
	private IResidenteService residenteService;

	@Autowired
	private IAsesorInternoService asesorInternoService;

	@Autowired
	private IAsesorExternoService asesorExternoService;

	@Autowired
	private IEmpresaService empresaService;
	
	@Autowired
	private ISeguimientoResidenciaService seguimientoResidenciaService;

	@Autowired
	private IEvaluacionReporteFinalService evaluacionReporteFinalService;

	@Autowired
	private IDocumentoResidenciaService documentoResidenciaService;

	@InitBinder
	public void initBinder(WebDataBinder binder) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, false));
	}

	@GetMapping("/mostrarProyectosResidencia")
	public String mostrarDatos(
			@RequestParam(name = "nombreProyecto", required = false) String nombreProyecto,
			@RequestParam(name = "periodo", required = false) String periodo,
			Model model) {

		List<ProyectoResidencia> lista = proyectoResidenciaService.buscarProyectosResidenciaActivos(nombreProyecto, periodo);
		model.addAttribute("proyectoResidencia", lista);
		model.addAttribute("nombreProyecto", nombreProyecto);
		model.addAttribute("periodo", periodo);

		return "proyectoResidencia/datosProyectoResidencia";
	}

	@GetMapping("/estatusProyectosResidencia")
	public String mostrarEstatusProyectosResidencia(
			@RequestParam(name = "nombreProyecto", required = false) String nombreProyecto,
			@RequestParam(name = "periodo", required = false) String periodo,
			Model model) {

		List<ProyectoResidencia> lista = proyectoResidenciaService.buscarTodosProyectosResidencia(nombreProyecto, periodo);
		model.addAttribute("proyectoResidencia", lista);
		model.addAttribute("nombreProyecto", nombreProyecto);
		model.addAttribute("periodo", periodo);

		return "proyectoResidencia/estatusProyectoResidencia";
	}

	@GetMapping("/formulario")
	public String mostrarFormulario(Model model) {
		model.addAttribute("proyectoResidencia", new ProyectoResidencia());
		model.addAttribute("proyectosBanco", proyectoBancoService.buscarProyectosBancoDisponibles());
		model.addAttribute("propuestasProyecto", propuestaProyectoService.buscarPropuestasDisponibles());
		model.addAttribute("residentes", residenteService.buscarTodosRes());
		model.addAttribute("asesoresInternos", asesorInternoService.buscarTodosAsInt());
		model.addAttribute("asesoresExternos", asesorExternoService.buscarTodosAsExt());
		model.addAttribute("empresas", empresaService.buscarTodasEmp());
		return "proyectoResidencia/formProyectoResidencia";
	}

	@PostMapping("/guardar")
	public String guardarProyectoResidencia(ProyectoResidencia proyectoResidencia,
	        RedirectAttributes redirectAttributes) {

	    ProyectoResidencia proyectoGuardar;
	    boolean esNuevo = proyectoResidencia.getIdProyectoResidencia() == null;

	    // SI ES EDICIÓN, PRIMERO BUSCAMOS EL REGISTRO ORIGINAL EN LA BD
	    if (!esNuevo) {

	        ProyectoResidencia proyectoBD = proyectoResidenciaService.buscarPorIdProyRes(
	                proyectoResidencia.getIdProyectoResidencia()
	        );

	        if (proyectoBD == null) {
	            redirectAttributes.addFlashAttribute("error", "No se encontró el proyecto de residencia");
	            return "redirect:/proyectoResidencia/mostrarProyectosResidencia";
	        }

	        // Actualizamos solo los campos editables
	        proyectoBD.setOrigenProyecto(proyectoResidencia.getOrigenProyecto());
	        proyectoBD.setProyectoBanco(proyectoResidencia.getProyectoBanco());
	        proyectoBD.setPropuestaProyecto(proyectoResidencia.getPropuestaProyecto());

	        proyectoBD.setNombreProyecto(proyectoResidencia.getNombreProyecto());
	        proyectoBD.setObjetivo(proyectoResidencia.getObjetivo());
	        proyectoBD.setDescripcion(proyectoResidencia.getDescripcion());
	        proyectoBD.setPeriodo(proyectoResidencia.getPeriodo());
	        proyectoBD.setFechaInicio(proyectoResidencia.getFechaInicio());
	        proyectoBD.setFechaFin(proyectoResidencia.getFechaFin());
	        proyectoBD.setHoras(proyectoResidencia.getHoras());
	        proyectoBD.setEstatus(proyectoResidencia.getEstatus());
	        proyectoBD.setEtapaActual(proyectoResidencia.getEtapaActual());

	        proyectoBD.setResidente(proyectoResidencia.getResidente());
	        proyectoBD.setEmpresa(proyectoResidencia.getEmpresa());
	        proyectoBD.setAsesorInterno(proyectoResidencia.getAsesorInterno());
	        proyectoBD.setAsesorExterno(proyectoResidencia.getAsesorExterno());

	        proyectoGuardar = proyectoBD;

	    } else {
	        // SI ES NUEVO, USAMOS EL OBJETO QUE VIENE DEL FORMULARIO
	        proyectoGuardar = proyectoResidencia;
	    }

	    /*
	     * VALIDACIÓN PARA EVITAR DUPLICADOS
	     * Solo se valida cuando es un registro nuevo.
	     */
	    if (esNuevo) {

	        if ("BANCO".equals(proyectoGuardar.getOrigenProyecto())
	                && proyectoGuardar.getProyectoBanco() != null
	                && proyectoGuardar.getProyectoBanco().getIdProyectoBanco() != null) {

	            Integer idProyectoBanco = proyectoGuardar.getProyectoBanco().getIdProyectoBanco();

	            if (proyectoResidenciaService.existePorProyectoBanco(idProyectoBanco)) {
	                redirectAttributes.addFlashAttribute("error",
	                        "Este proyecto del banco ya fue asignado a una residencia.");
	                return "redirect:/proyectoResidencia/formulario";
	            }
	        }

	        if ("PROPUESTA".equals(proyectoGuardar.getOrigenProyecto())
	                && proyectoGuardar.getPropuestaProyecto() != null
	                && proyectoGuardar.getPropuestaProyecto().getIdPropuestaProyecto() != null) {

	            Integer idPropuestaProyecto = proyectoGuardar.getPropuestaProyecto().getIdPropuestaProyecto();

	            if (proyectoResidenciaService.existePorPropuestaProyecto(idPropuestaProyecto)) {
	                redirectAttributes.addFlashAttribute("error",
	                        "Esta propuesta ya fue asignada a una residencia.");
	                return "redirect:/proyectoResidencia/formulario";
	            }
	        }
	    }

	    /*
	     * SI EL ORIGEN ES BANCO, BUSCAMOS EL PROYECTO REAL EN LA BD
	     */
	    if ("BANCO".equals(proyectoGuardar.getOrigenProyecto())
	            && proyectoGuardar.getProyectoBanco() != null
	            && proyectoGuardar.getProyectoBanco().getIdProyectoBanco() != null) {

	        ProyectoBanco proyectoBanco = proyectoBancoService.buscarPorIdProyBan(
	                proyectoGuardar.getProyectoBanco().getIdProyectoBanco()
	        );

	        if (proyectoBanco != null) {
	            proyectoGuardar.setProyectoBanco(proyectoBanco);
	            proyectoGuardar.setPropuestaProyecto(null);

	            proyectoGuardar.setNombreProyecto(proyectoBanco.getNombreProyecto());
	            proyectoGuardar.setObjetivo(proyectoBanco.getObjetivo());
	            proyectoGuardar.setDescripcion(proyectoBanco.getDescripcion());

	            if (proyectoGuardar.getPeriodo() == null || proyectoGuardar.getPeriodo().isBlank()) {
	                proyectoGuardar.setPeriodo(proyectoBanco.getPeriodo());
	            }
	        }
	    }

	    /*
	     * SI EL ORIGEN ES PROPUESTA, BUSCAMOS LA PROPUESTA REAL EN LA BD
	     */
	    if ("PROPUESTA".equals(proyectoGuardar.getOrigenProyecto())
	            && proyectoGuardar.getPropuestaProyecto() != null
	            && proyectoGuardar.getPropuestaProyecto().getIdPropuestaProyecto() != null) {

	        PropuestaProyecto propuestaProyecto = propuestaProyectoService.buscarPorIdProp(
	                proyectoGuardar.getPropuestaProyecto().getIdPropuestaProyecto()
	        );

	        if (propuestaProyecto != null) {
	            proyectoGuardar.setPropuestaProyecto(propuestaProyecto);
	            proyectoGuardar.setProyectoBanco(null);

	            proyectoGuardar.setNombreProyecto(propuestaProyecto.getNombreProyecto());
	            proyectoGuardar.setObjetivo(propuestaProyecto.getObjetivo());
	            proyectoGuardar.setDescripcion(propuestaProyecto.getDescripcion());
	            proyectoGuardar.setPeriodo(propuestaProyecto.getPeriodo());

	            if (propuestaProyecto.getResidente() != null) {
	                proyectoGuardar.setResidente(propuestaProyecto.getResidente());
	            }

	            if (propuestaProyecto.getEmpresa() != null) {
	                proyectoGuardar.setEmpresa(propuestaProyecto.getEmpresa());
	            }
	        }
	    }

	    proyectoGuardar.setHoras(500);

	    if (proyectoGuardar.getEstatus() == null) {
	        proyectoGuardar.setEstatus(1);
	    }

	    if (proyectoGuardar.getEtapaActual() == null || proyectoGuardar.getEtapaActual().isBlank()) {
	        proyectoGuardar.setEtapaActual("ACTIVO");
	    }

	    proyectoResidenciaService.guardarProyectoResidencia(proyectoGuardar);

	    redirectAttributes.addFlashAttribute("msg", "Proyecto de residencia guardado correctamente");
	    return "redirect:/proyectoResidencia/mostrarProyectosResidencia";
	}
	@GetMapping("/ver/{id}")
	public String verDetalleProyectoResidencia(@PathVariable("id") Integer id, Model model) {
		ProyectoResidencia proyectoResidencia = proyectoResidenciaService.buscarPorIdProyRes(id);
		model.addAttribute("proyectoResidencia", proyectoResidencia);
		return "proyectoResidencia/detalleProyectoResidencia";
	}

	@GetMapping("/editar/{id}")
	public String editarProyectoResidencia(@PathVariable("id") Integer id, Model model) {
		ProyectoResidencia proyectoResidencia = proyectoResidenciaService.buscarPorIdProyRes(id);
		model.addAttribute("proyectoResidencia", proyectoResidencia);
		model.addAttribute("proyectosBanco", proyectoBancoService.buscarTodosProyBan());
		model.addAttribute("propuestasProyecto", propuestaProyectoService.buscarTodasProp());
		model.addAttribute("residentes", residenteService.buscarTodosRes());
		model.addAttribute("asesoresInternos", asesorInternoService.buscarTodosAsInt());
		model.addAttribute("asesoresExternos", asesorExternoService.buscarTodosAsExt());
		model.addAttribute("empresas", empresaService.buscarTodasEmp());
		return "proyectoResidencia/formProyectoResidencia";
	}

	@GetMapping("/eliminar/{id}")
	public String eliminarProyectoResidencia(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {

		if (proyectoResidenciaService.proyectoResidenciaTieneVinculosActivos(id)) {
			redirectAttributes.addFlashAttribute("msg",
					"No se puede desactivar el proyecto de residencia porque tiene documentos, reporte preliminar o seguimientos activos");
			return "redirect:/proyectoResidencia/mostrarProyectosResidencia";
		}

		proyectoResidenciaService.eliminarProyectoResidencia(id);
		redirectAttributes.addFlashAttribute("msg", "Proyecto de residencia desactivado correctamente");
		return "redirect:/proyectoResidencia/mostrarProyectosResidencia";
	}

	@GetMapping("/activar/{id}")
	public String activarProyectoResidencia(@PathVariable("id") Integer id) {
		proyectoResidenciaService.activarProyectoResidencia(id);
		return "redirect:/proyectoResidencia/estatusProyectosResidencia";
	}
	@GetMapping("/pdf/{id}")
	public ResponseEntity<Resource> generarPdfProyectoResidencia(@PathVariable("id") Integer id) {
		try {
			ProyectoResidencia proyectoResidencia = proyectoResidenciaService.buscarPorIdProyRes(id);

			if (proyectoResidencia == null) {
				return ResponseEntity.notFound().build();
			}

			String nombreArchivo = proyectoResidenciaPdf.generarPdf(proyectoResidencia);
			File archivo = archivoStorageService.getRutaArchivo("pdf", nombreArchivo).toFile();

			InputStreamResource resource = new InputStreamResource(new FileInputStream(archivo));

			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + nombreArchivo)
					.contentType(MediaType.APPLICATION_PDF)
					.contentLength(archivo.length())
					.body(resource);

		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.internalServerError().build();
		}
	}
	
	@GetMapping("/calcularFinal/{id}")
	public String calcularCalificacionFinalResidencia(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {

		ProyectoResidencia proyecto = proyectoResidenciaService.buscarPorIdProyRes(id);

		if (proyecto == null) {
			redirectAttributes.addFlashAttribute("msg", "Proyecto de residencia no encontrado");
			return "redirect:/proyectoResidencia/mostrarProyectosResidencia";
		}

		List<SeguimientoResidencia> seguimientos = seguimientoResidenciaService.buscarPorProyectoResidencia(id);

		Double seguimiento1 = null;
		Double seguimiento2 = null;

		for (SeguimientoResidencia s : seguimientos) {
			if ("PRIMER_SEGUIMIENTO".equalsIgnoreCase(s.getTipoSeguimiento())) {
				seguimiento1 = s.getCalificacionFinal();
			}
			if ("SEGUNDO_SEGUIMIENTO".equalsIgnoreCase(s.getTipoSeguimiento())) {
				seguimiento2 = s.getCalificacionFinal();
			}
		}

		EvaluacionReporteFinal evaluacionReporteFinal = evaluacionReporteFinalService.buscarPorProyecto(id);
		Double reporteFinal = evaluacionReporteFinal != null ? evaluacionReporteFinal.getCalificacionFinalReporte() : null;

		if (seguimiento1 == null || seguimiento2 == null || reporteFinal == null) {
			redirectAttributes.addFlashAttribute("msg",
					"No se puede calcular la calificación final porque faltan seguimientos o la evaluación del reporte final");
			return "redirect:/proyectoResidencia/mostrarProyectosResidencia";
		}

		double finalResidencia = (seguimiento1 * 0.10) + (seguimiento2 * 0.10) + (reporteFinal * 0.80);
		proyecto.setCalificacionFinalResidencia(finalResidencia);
		proyecto.setEtapaActual("CONCLUIDO");
		proyectoResidenciaService.guardarProyectoResidencia(proyecto);

		redirectAttributes.addFlashAttribute("msg", "Calificación final de residencia calculada correctamente");
		return "redirect:/proyectoResidencia/mostrarProyectosResidencia";
	}
	
	//Este es para poder generar el pff de liberacion
	@GetMapping("/liberacion/{id}")
	public ResponseEntity<byte[]> generarLiberacion(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
		try {
			ProyectoResidencia proyecto = proyectoResidenciaService.buscarPorIdProyRes(id);

			if (proyecto == null) {
				return ResponseEntity.notFound().build();
			}

			if (proyecto.getCalificacionFinalResidencia() == null) {
				return ResponseEntity.badRequest().build();
			}

			String nombreProyecto = proyecto.getNombreProyecto();
			String nombreResidente = proyecto.getResidente().getNombre() + " "
					+ proyecto.getResidente().getApellidoPaterno() + " "
					+ proyecto.getResidente().getApellidoMaterno();
			String numeroControl = proyecto.getResidente().getNumeroControl();
			String carrera = proyecto.getResidente().getCarrera().getNombre();
			String nombreAsesorInterno = proyecto.getAsesorInterno().getNombre() + " "
					+ proyecto.getAsesorInterno().getApellidoPaterno() + " "
					+ proyecto.getAsesorInterno().getApellidoMaterno();

			String nombreJefeDepartamento = "JEFE DEL DEPARTAMENTO";
			String nombreDepartamento = proyecto.getAsesorInterno().getDepartamentoAcademico().getNombre();

			String periodoTexto = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy").format(proyecto.getFechaInicio())
					+ " al "
					+ new SimpleDateFormat("dd 'de' MMMM 'de' yyyy").format(proyecto.getFechaFin());

			byte[] pdf = LiberacionAsesorInternoPdf.generarPdf(
					nombreProyecto,
					nombreResidente,
					numeroControl,
					carrera,
					nombreAsesorInterno,
					nombreJefeDepartamento,
					nombreDepartamento,
					periodoTexto
			);

			File directorio = archivoStorageService.getRutaDirectorio("documentos").toFile();

			String nombreArchivo = "liberacion_" + proyecto.getIdProyectoResidencia() + ".pdf";
			File archivo = archivoStorageService.getRutaArchivo("documentos", nombreArchivo).toFile();

			try (FileOutputStream fos = new FileOutputStream(archivo)) {
				fos.write(pdf);
			}

			DocumentoResidencia documentoExistente = documentoResidenciaService.buscarPorProyectoYTipo(
					proyecto.getIdProyectoResidencia(), "LIBERACION");

			if (documentoExistente == null) {
				documentoExistente = new DocumentoResidencia();
				documentoExistente.setProyectoResidencia(proyecto);
				documentoExistente.setTipoDocumento("LIBERACION");
				documentoExistente.setFechaCarga(new java.util.Date());
				documentoExistente.setEstatus(1);
			}

			documentoExistente.setNombreArchivo(nombreArchivo);
			documentoExistente.setRutaArchivo(nombreArchivo);
			documentoExistente.setObservaciones("Liberación generada automáticamente por el sistema");
			documentoResidenciaService.guardarDocumentoResidencia(documentoExistente);

			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + nombreArchivo)
					.contentType(MediaType.APPLICATION_PDF)
					.body(pdf);

		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.internalServerError().build();
		}
	}
}
