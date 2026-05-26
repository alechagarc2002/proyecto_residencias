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

import itch.tsp.config.ArchivoStorageService;
import itch.tsp.modelo.ProyectoResidencia;
import itch.tsp.modelo.ReportePreliminar;
import itch.tsp.service.IProyectoResidenciaService;
import itch.tsp.service.IReportePreliminarService;

import java.io.File;
import java.io.FileInputStream;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import itch.tsp.util.ReportePreliminarPdf;

@RequestMapping("/reportePreliminar")
@Controller
public class ReportePreliminarController {

	@Autowired
	private ArchivoStorageService archivoStorageService;

	@Autowired
	private ReportePreliminarPdf reportePreliminarPdf;

	@Autowired
	private IReportePreliminarService reportePreliminarService;

	@Autowired
	private IProyectoResidenciaService proyectoResidenciaService;

	@InitBinder
	public void initBinder(WebDataBinder binder) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, false));
	}

	@GetMapping("/mostrarReportes")
	public String mostrarDatos(
			@RequestParam(name = "nombreProyecto", required = false) String nombreProyecto,
			@RequestParam(name = "dictamen", required = false) String dictamen,
			Model model) {

		List<ReportePreliminar> lista = reportePreliminarService.buscarReportesActivos(nombreProyecto, dictamen);
		model.addAttribute("reportePreliminar", lista);
		model.addAttribute("nombreProyecto", nombreProyecto);
		model.addAttribute("dictamen", dictamen);

		return "reportePreliminar/datosReportePreliminar";
	}

	@GetMapping("/estatusReportes")
	public String mostrarEstatusReportes(
			@RequestParam(name = "nombreProyecto", required = false) String nombreProyecto,
			@RequestParam(name = "dictamen", required = false) String dictamen,
			Model model) {

		List<ReportePreliminar> lista = reportePreliminarService.buscarTodosReportes(nombreProyecto, dictamen);
		model.addAttribute("reportePreliminar", lista);
		model.addAttribute("nombreProyecto", nombreProyecto);
		model.addAttribute("dictamen", dictamen);

		return "reportePreliminar/estatusReportePreliminar";
	}

	@GetMapping("/formulario")
	public String mostrarFormulario(Model model) {
		model.addAttribute("reportePreliminar", new ReportePreliminar());
		model.addAttribute("proyectosResidencia", proyectoResidenciaService.buscarTodosProyRes());
		return "reportePreliminar/formReportePreliminar";
	}

	@PostMapping("/guardar")
	public String guardarReportePreliminar(ReportePreliminar reportePreliminar,
	        RedirectAttributes redirectAttributes) {
		

	    if (reportePreliminar.getProyectoResidencia() == null ||
	            reportePreliminar.getProyectoResidencia().getIdProyectoResidencia() == null) {

	        redirectAttributes.addFlashAttribute("msg", "Debe seleccionar un proyecto de residencia");
	        return "redirect:/reportePreliminar/formulario";
	    }

	    ProyectoResidencia proyectoResidencia = proyectoResidenciaService.buscarPorIdProyRes(
	            reportePreliminar.getProyectoResidencia().getIdProyectoResidencia());

	    if (proyectoResidencia == null) {
	        redirectAttributes.addFlashAttribute("msg", "El proyecto de residencia seleccionado no existe");
	        return "redirect:/reportePreliminar/formulario";
	    }

	    reportePreliminar.setProyectoResidencia(proyectoResidencia);

	    if (reportePreliminar.getObjetivoProyecto() == null ||
	            reportePreliminar.getObjetivoProyecto().isBlank()) {
	        reportePreliminar.setObjetivoProyecto(proyectoResidencia.getObjetivo());
	    }

	    if (reportePreliminar.getFechaEntrega() == null) {
	        reportePreliminar.setFechaEntrega(new Date());
	    }

	    if (reportePreliminar.getDictamen() == null ||
	            reportePreliminar.getDictamen().isBlank()) {
	        reportePreliminar.setDictamen("PENDIENTE");
	    }

	    if (reportePreliminar.getEstatus() == null) {
	        reportePreliminar.setEstatus(1);
	    }

	    reportePreliminarService.guardarReportePreliminar(reportePreliminar);

	    redirectAttributes.addFlashAttribute("msg", "Reporte preliminar guardado correctamente");
	    return "redirect:/reportePreliminar/mostrarReportes";
	}

	@GetMapping("/ver/{id}")
	public String verDetalleReportePreliminar(@PathVariable("id") Integer id, Model model) {
		ReportePreliminar reportePreliminar = reportePreliminarService.buscarPorIdRepPre(id);
		model.addAttribute("reportePreliminar", reportePreliminar);
		return "reportePreliminar/detalleReportePreliminar";
	}

	@GetMapping("/editar/{id}")
	public String editarReportePreliminar(@PathVariable("id") Integer id, Model model) {
		ReportePreliminar reportePreliminar = reportePreliminarService.buscarPorIdRepPre(id);
		model.addAttribute("reportePreliminar", reportePreliminar);
		model.addAttribute("proyectosResidencia", proyectoResidenciaService.buscarTodosProyRes());
		return "reportePreliminar/formReportePreliminar";
	}

	@GetMapping("/revisar")
	public String mostrarRevisionReportes(
			@RequestParam(name = "dictamen", required = false) String dictamen,
			Model model) {

		List<ReportePreliminar> lista = reportePreliminarService.buscarReportesActivos(null, dictamen);
		model.addAttribute("reportePreliminar", lista);
		model.addAttribute("dictamen", dictamen);

		return "reportePreliminar/revisarReportePreliminar";
	}

	@GetMapping("/dictaminar/{id}")
	public String mostrarFormularioDictamen(@PathVariable("id") Integer id, Model model) {
		ReportePreliminar reportePreliminar = reportePreliminarService.buscarPorIdRepPre(id);
		model.addAttribute("reportePreliminar", reportePreliminar);
		return "reportePreliminar/dictamenReportePreliminar";
	}

	@PostMapping("/guardarDictamen")
	public String guardarDictamen(ReportePreliminar reportePreliminar,
			RedirectAttributes redirectAttributes) {

		ReportePreliminar reporteOriginal = reportePreliminarService.buscarPorIdRepPre(
				reportePreliminar.getIdReportePreliminar());

		if (reporteOriginal != null) {
			reporteOriginal.setDictamen(reportePreliminar.getDictamen());
			reporteOriginal.setObservacionesDictamen(reportePreliminar.getObservacionesDictamen());
			reportePreliminarService.guardarReportePreliminar(reporteOriginal);
		}

		redirectAttributes.addFlashAttribute("msg", "Dictamen del reporte preliminar actualizado correctamente");
		return "redirect:/reportePreliminar/revisar";
	}

	@GetMapping("/eliminar/{id}")
	public String eliminarReportePreliminar(@PathVariable("id") Integer id) {
		reportePreliminarService.eliminarReportePreliminar(id);
		return "redirect:/reportePreliminar/mostrarReportes";
	}

	@GetMapping("/activar/{id}")
	public String activarReportePreliminar(@PathVariable("id") Integer id) {
		reportePreliminarService.activarReportePreliminar(id);
		return "redirect:/reportePreliminar/estatusReportes";
	}
	@GetMapping("/pdf/{id}")
	public ResponseEntity<Resource> generarPdfReportePreliminar(@PathVariable("id") Integer id) {
		try {
			ReportePreliminar reportePreliminar = reportePreliminarService.buscarPorIdRepPre(id);

			if (reportePreliminar == null) {
				return ResponseEntity.notFound().build();
			}

			String nombreArchivo = reportePreliminarPdf.generarPdf(reportePreliminar);
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
}
