package itch.tsp.controller;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import itch.tsp.config.ArchivoStorageService;
import itch.tsp.modelo.DocumentoResidencia;
import itch.tsp.service.IDocumentoResidenciaService;
import itch.tsp.service.IProyectoResidenciaService;

@RequestMapping("/documentoResidencia")
@Controller
public class DocumentoResidenciaController {

	@Autowired
	private IDocumentoResidenciaService documentoResidenciaService;

	@Autowired
	private IProyectoResidenciaService proyectoResidenciaService;

	@Autowired
	private ArchivoStorageService archivoStorageService;

	@InitBinder
	public void initBinder(WebDataBinder binder) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, false));
	}

	@GetMapping("/mostrarDocumentos")
	public String mostrarDatos(
			@RequestParam(name = "tipoDocumento", required = false) String tipoDocumento,
			Model model) {

		List<DocumentoResidencia> lista = documentoResidenciaService.buscarDocumentosActivos(tipoDocumento);
		model.addAttribute("documentoResidencia", lista);
		model.addAttribute("tipoDocumento", tipoDocumento);

		return "documentoResidencia/datosDocumentoResidencia";
	}

	@GetMapping("/estatusDocumentos")
	public String mostrarEstatusDocumentos(
			@RequestParam(name = "tipoDocumento", required = false) String tipoDocumento,
			Model model) {

		List<DocumentoResidencia> lista = documentoResidenciaService.buscarTodosDocumentos(tipoDocumento);
		model.addAttribute("documentoResidencia", lista);
		model.addAttribute("tipoDocumento", tipoDocumento);

		return "documentoResidencia/estatusDocumentoResidencia";
	}

	@GetMapping("/formulario")
	public String mostrarFormulario(Model model) {
		model.addAttribute("documentoResidencia", new DocumentoResidencia());
		model.addAttribute("proyectosResidencia", proyectoResidenciaService.buscarTodosProyRes());
		return "documentoResidencia/formDocumentoResidencia";
	}

	@PostMapping("/guardar")
	public String guardarDocumentoResidencia(DocumentoResidencia documentoResidencia,
			@RequestParam("archivo") MultipartFile archivo,
			RedirectAttributes redirectAttributes) {

		File directorio = archivoStorageService.getRutaDirectorio("documentos").toFile();

		if (!archivo.isEmpty()) {
			String nombreArchivo = System.currentTimeMillis() + "_" + archivo.getOriginalFilename().replace(" ", "_");
			File destino = archivoStorageService.getRutaArchivo("documentos", nombreArchivo).toFile();
			try {
				archivo.transferTo(destino);
				documentoResidencia.setNombreArchivo(nombreArchivo);
				documentoResidencia.setRutaArchivo(nombreArchivo);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (documentoResidencia.getFechaCarga() == null) {
			documentoResidencia.setFechaCarga(new Date());
		}

		if (documentoResidencia.getEstatus() == null) {
			documentoResidencia.setEstatus(1);
		}

		documentoResidenciaService.guardarDocumentoResidencia(documentoResidencia);
		redirectAttributes.addFlashAttribute("msg", "Documento de residencia guardado correctamente");
		return "redirect:/documentoResidencia/mostrarDocumentos";
	}

	@GetMapping("/ver/{id}")
	public String verDetalleDocumentoResidencia(@PathVariable("id") Integer id, Model model) {
		DocumentoResidencia documentoResidencia = documentoResidenciaService.buscarPorIdDocRes(id);
		model.addAttribute("documentoResidencia", documentoResidencia);
		return "documentoResidencia/detalleDocumentoResidencia";
	}

	@GetMapping("/editar/{id}")
	public String editarDocumentoResidencia(@PathVariable("id") Integer id, Model model) {
		DocumentoResidencia documentoResidencia = documentoResidenciaService.buscarPorIdDocRes(id);
		model.addAttribute("documentoResidencia", documentoResidencia);
		model.addAttribute("proyectosResidencia", proyectoResidenciaService.buscarTodosProyRes());
		return "documentoResidencia/formDocumentoResidencia";
	}

	@GetMapping("/eliminar/{id}")
	public String eliminarDocumentoResidencia(@PathVariable("id") Integer id) {
		documentoResidenciaService.eliminarDocumentoResidencia(id);
		return "redirect:/documentoResidencia/mostrarDocumentos";
	}

	@GetMapping("/activar/{id}")
	public String activarDocumentoResidencia(@PathVariable("id") Integer id) {
		documentoResidenciaService.activarDocumentoResidencia(id);
		return "redirect:/documentoResidencia/estatusDocumentos";
	}

	@GetMapping("/proyecto/{id}")
	public String verDocumentosPorProyecto(@PathVariable("id") Integer idProyectoResidencia,
	        @RequestParam(name = "volver", required = false) String volver,
	        @RequestParam(name = "tipoSeguimiento", required = false) String tipoSeguimiento,
	        Model model) {

	    String[] tiposEsperados = {
	            "CARTA_ACEPTACION",
	            "REPORTE_PRELIMINAR",
	            "SEGUIMIENTO_1",
	            "SEGUIMIENTO_2",
	            "REPORTE_FINAL",
	            "LIBERACION"
	    };

	    List<ExpedienteDocumentoView> expediente = new ArrayList<>();

	    for (String tipo : tiposEsperados) {
	        DocumentoResidencia documento = documentoResidenciaService.buscarPorProyectoYTipo(idProyectoResidencia, tipo);

	        ExpedienteDocumentoView item = new ExpedienteDocumentoView();
	        item.setTipoDocumento(tipo);
	        item.setDocumento(documento);
	        item.setEstado(documento != null && documento.getRutaArchivo() != null && !documento.getRutaArchivo().isBlank()
	                ? "ENTREGADO"
	                : "NO ENTREGADO");

	        expediente.add(item);
	    }

	    model.addAttribute("expediente", expediente);
	    model.addAttribute("idProyectoResidencia", idProyectoResidencia);
	    model.addAttribute("volver", volver);
	    model.addAttribute("tipoSeguimiento", tipoSeguimiento);

	    return "documentoResidencia/documentosPorProyecto";
	}

	@GetMapping("/archivo/{id}")
	public ResponseEntity<Resource> verArchivoDocumento(@PathVariable("id") Integer id) {
		try {
			DocumentoResidencia documentoResidencia = documentoResidenciaService.buscarPorIdDocRes(id);

			if (documentoResidencia == null || documentoResidencia.getRutaArchivo() == null || documentoResidencia.getRutaArchivo().isBlank()) {
				return ResponseEntity.notFound().build();
			}

			File archivo = archivoStorageService.getRutaArchivo("documentos", documentoResidencia.getRutaArchivo()).toFile();

			if (!archivo.exists()) {
				return ResponseEntity.notFound().build();
			}

			InputStreamResource resource = new InputStreamResource(new FileInputStream(archivo));

			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + archivo.getName())
					.contentType(MediaType.APPLICATION_PDF)
					.contentLength(archivo.length())
					.body(resource);

		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.internalServerError().build();
		}
	}

	public static class ExpedienteDocumentoView {
		private String tipoDocumento;
		private String estado;
		private DocumentoResidencia documento;

		public String getTipoDocumento() {
			return tipoDocumento;
		}

		public void setTipoDocumento(String tipoDocumento) {
			this.tipoDocumento = tipoDocumento;
		}

		public String getEstado() {
			return estado;
		}

		public void setEstado(String estado) {
			this.estado = estado;
		}

		public DocumentoResidencia getDocumento() {
			return documento;
		}

		public void setDocumento(DocumentoResidencia documento) {
			this.documento = documento;
		}
	}
}
