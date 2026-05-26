package itch.tsp.util;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import itch.tsp.config.ArchivoStorageService;
import itch.tsp.modelo.ProyectoResidencia;

@Component
public class ProyectoResidenciaPdf {

	private final ArchivoStorageService archivoStorageService;

	public ProyectoResidenciaPdf(ArchivoStorageService archivoStorageService) {
		this.archivoStorageService = archivoStorageService;
	}

	public String generarPdf(ProyectoResidencia proyectoResidencia) throws Exception {

		File carpeta = archivoStorageService.getRutaDirectorio("pdf").toFile();

		String nombreArchivo = "proyecto_residencia_" + proyectoResidencia.getIdProyectoResidencia() + ".pdf";
		String rutaCompleta = new File(carpeta, nombreArchivo).getAbsolutePath();

		Document document = new Document(PageSize.A4, 36, 36, 36, 36);
		PdfWriter.getInstance(document, new FileOutputStream(rutaCompleta));
		document.open();

		Font fontTituloBlanco = new Font(Font.FontFamily.HELVETICA, 13, Font.BOLD, BaseColor.WHITE);
		Font fontSubtitulo = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, new BaseColor(36, 84, 112));
		Font fontEtiqueta = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, new BaseColor(36, 84, 112));
		Font fontTexto = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, BaseColor.BLACK);
		Font fontFooter = new Font(Font.FontFamily.HELVETICA, 7, Font.NORMAL, BaseColor.GRAY);

		// ===== LOGOS =====
		PdfPTable tablaLogos = new PdfPTable(1);
		tablaLogos.setWidthPercentage(100);

		PdfPCell celdaLogo = new PdfPCell();
		celdaLogo.setBorder(Rectangle.NO_BORDER);
		celdaLogo.setHorizontalAlignment(Element.ALIGN_CENTER);
		celdaLogo.setPaddingBottom(8f);

		try {
			Image logos = Image.getInstance(new ClassPathResource("static/img/logoteccompl.png").getURL());
			logos.scaleToFit(500, 90);
			logos.setAlignment(Element.ALIGN_CENTER);
			celdaLogo.addElement(logos);
		} catch (Exception e) {
			Paragraph sinLogo = new Paragraph("Encabezado institucional", fontTexto);
			sinLogo.setAlignment(Element.ALIGN_CENTER);
			celdaLogo.addElement(sinLogo);
		}

		tablaLogos.addCell(celdaLogo);
		document.add(tablaLogos);

		// ===== ENCABEZADO AZUL =====
		PdfPTable tablaTitulo = new PdfPTable(1);
		tablaTitulo.setWidthPercentage(100);

		PdfPCell celdaTitulo = new PdfPCell();
		celdaTitulo.setBackgroundColor(new BaseColor(34, 93, 132));
		celdaTitulo.setHorizontalAlignment(Element.ALIGN_CENTER);
		celdaTitulo.setPadding(10f);
		celdaTitulo.setBorder(Rectangle.NO_BORDER);

		Paragraph p1 = new Paragraph("INSTITUTO TECNOLÓGICO DE CHILPANCINGO", fontTituloBlanco);
		p1.setAlignment(Element.ALIGN_CENTER);
		celdaTitulo.addElement(p1);

		Paragraph p2 = new Paragraph("SISTEMA DE RESIDENCIA PROFESIONAL", fontTituloBlanco);
		p2.setAlignment(Element.ALIGN_CENTER);
		celdaTitulo.addElement(p2);

		tablaTitulo.addCell(celdaTitulo);
		document.add(tablaTitulo);

		document.add(Chunk.NEWLINE);

		// ===== SUBTITULO =====
		Paragraph subtitulo1 = new Paragraph("DATOS DEL PROYECTO DE RESIDENCIA", fontSubtitulo);
		subtitulo1.setAlignment(Element.ALIGN_CENTER);
		document.add(subtitulo1);
		document.add(Chunk.NEWLINE);

		// ===== TABLA DATOS =====
		PdfPTable tablaDatos = new PdfPTable(2);
		tablaDatos.setWidthPercentage(100);
		tablaDatos.setWidths(new float[] { 2.0f, 4.5f });

		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

		agregarFila(tablaDatos, "Título del proyecto", valor(proyectoResidencia.getNombreProyecto()), fontEtiqueta, fontTexto);
		agregarFila(tablaDatos, "Estudiante", proyectoResidencia.getResidente() != null
				? valor(proyectoResidencia.getResidente().getNombre()) + " "
						+ valor(proyectoResidencia.getResidente().getApellidoPaterno()) + " "
						+ valor(proyectoResidencia.getResidente().getApellidoMaterno())
				: "", fontEtiqueta, fontTexto);
		agregarFila(tablaDatos, "No. Control", proyectoResidencia.getResidente() != null
				? valor(proyectoResidencia.getResidente().getNumeroControl())
				: "", fontEtiqueta, fontTexto);
		agregarFila(tablaDatos, "Carrera", proyectoResidencia.getResidente() != null
				&& proyectoResidencia.getResidente().getCarrera() != null
						? valor(proyectoResidencia.getResidente().getCarrera().getNombre())
						: "", fontEtiqueta, fontTexto);
		agregarFila(tablaDatos, "Correo", proyectoResidencia.getResidente() != null
				? valor(proyectoResidencia.getResidente().getCorreo())
				: "", fontEtiqueta, fontTexto);
		agregarFila(tablaDatos, "Empresa", proyectoResidencia.getEmpresa() != null
				? valor(proyectoResidencia.getEmpresa().getNombre())
				: "", fontEtiqueta, fontTexto);
		agregarFila(tablaDatos, "Asesor interno", proyectoResidencia.getAsesorInterno() != null
				? valor(proyectoResidencia.getAsesorInterno().getNombre()) + " "
						+ valor(proyectoResidencia.getAsesorInterno().getApellidoPaterno()) + " "
						+ valor(proyectoResidencia.getAsesorInterno().getApellidoMaterno())
				: "", fontEtiqueta, fontTexto);
		agregarFila(tablaDatos, "Asesor externo", proyectoResidencia.getAsesorExterno() != null
				? valor(proyectoResidencia.getAsesorExterno().getNombre()) + " "
						+ valor(proyectoResidencia.getAsesorExterno().getApellidoPaterno()) + " "
						+ valor(proyectoResidencia.getAsesorExterno().getApellidoMaterno())
				: "", fontEtiqueta, fontTexto);
		agregarFila(tablaDatos, "Periodo", valor(proyectoResidencia.getPeriodo()), fontEtiqueta, fontTexto);
		agregarFila(tablaDatos, "Fecha inicio", proyectoResidencia.getFechaInicio() != null
				? sdf.format(proyectoResidencia.getFechaInicio())
				: "", fontEtiqueta, fontTexto);
		agregarFila(tablaDatos, "Fecha fin", proyectoResidencia.getFechaFin() != null
				? sdf.format(proyectoResidencia.getFechaFin())
				: "", fontEtiqueta, fontTexto);
		agregarFila(tablaDatos, "Horas", proyectoResidencia.getHoras() != null
				? proyectoResidencia.getHoras().toString()
				: "", fontEtiqueta, fontTexto);
		agregarFila(tablaDatos, "Estatus", valor(proyectoResidencia.getEtapaActual()), fontEtiqueta, fontTexto);

		document.add(tablaDatos);
		document.add(Chunk.NEWLINE);

		// ===== OBJETIVO =====
		Paragraph subtitulo2 = new Paragraph("OBJETIVO DEL PROYECTO", fontSubtitulo);
		subtitulo2.setAlignment(Element.ALIGN_LEFT);
		document.add(subtitulo2);

		PdfPTable tablaObjetivo = new PdfPTable(1);
		tablaObjetivo.setWidthPercentage(100);
		PdfPCell objetivoCell = new PdfPCell(new Phrase(valor(proyectoResidencia.getObjetivo()), fontTexto));
		objetivoCell.setBackgroundColor(new BaseColor(227, 236, 243));
		objetivoCell.setPadding(8f);
		tablaObjetivo.addCell(objetivoCell);
		document.add(tablaObjetivo);

		document.add(Chunk.NEWLINE);

		// ===== DESCRIPCIÓN =====
		Paragraph subtitulo3 = new Paragraph("DESCRIPCIÓN DEL PROYECTO", fontSubtitulo);
		subtitulo3.setAlignment(Element.ALIGN_LEFT);
		document.add(subtitulo3);

		PdfPTable tablaDescripcion = new PdfPTable(1);
		tablaDescripcion.setWidthPercentage(100);
		PdfPCell descripcionCell = new PdfPCell(new Phrase(valor(proyectoResidencia.getDescripcion()), fontTexto));
		descripcionCell.setBackgroundColor(new BaseColor(227, 236, 243));
		descripcionCell.setPadding(8f);
		tablaDescripcion.addCell(descripcionCell);
		document.add(tablaDescripcion);

		document.add(Chunk.NEWLINE);
		document.add(Chunk.NEWLINE);

		// ===== FIRMAS =====
		Paragraph subtituloFirmas = new Paragraph("FIRMAS", fontSubtitulo);
		document.add(subtituloFirmas);
		document.add(Chunk.NEWLINE);

		PdfPTable tablaFirmas = new PdfPTable(2);
		tablaFirmas.setWidthPercentage(100);
		tablaFirmas.setWidths(new float[] { 1f, 1f });

		PdfPCell firma1 = new PdfPCell();
		firma1.setBorder(Rectangle.NO_BORDER);
		firma1.setHorizontalAlignment(Element.ALIGN_CENTER);
		firma1.setPaddingTop(25f);
		firma1.addElement(new Paragraph("________________________________", fontTexto));
		firma1.addElement(new Paragraph("Nombre y firma del estudiante", fontTexto));

		PdfPCell firma2 = new PdfPCell();
		firma2.setBorder(Rectangle.NO_BORDER);
		firma2.setHorizontalAlignment(Element.ALIGN_CENTER);
		firma2.setPaddingTop(25f);
		firma2.addElement(new Paragraph("________________________________", fontTexto));
		firma2.addElement(new Paragraph("Nombre y firma del Jefe de División", fontTexto));

		tablaFirmas.addCell(firma1);
		tablaFirmas.addCell(firma2);

		document.add(tablaFirmas);

		document.add(Chunk.NEWLINE);
		document.add(Chunk.NEWLINE);

		Paragraph footer = new Paragraph(
				"Documento generado automáticamente · TecNM Campus Chilpancingo · dep@chilpancingo.tecnm.mx",
				fontFooter);
		footer.setAlignment(Element.ALIGN_CENTER);
		document.add(footer);

		document.close();

		return nombreArchivo;
	}

	private static void agregarFila(PdfPTable tabla, String etiqueta, String valor, Font fontEtiqueta, Font fontTexto) {
		PdfPCell c1 = new PdfPCell(new Phrase(etiqueta, fontEtiqueta));
		c1.setBackgroundColor(new BaseColor(227, 236, 243));
		c1.setPadding(6f);

		PdfPCell c2 = new PdfPCell(new Phrase(valor != null ? valor : "", fontTexto));
		c2.setBackgroundColor(new BaseColor(245, 248, 250));
		c2.setPadding(6f);

		tabla.addCell(c1);
		tabla.addCell(c2);
	}

	private static String valor(String texto) {
		return texto != null ? texto : "";
	}
}
