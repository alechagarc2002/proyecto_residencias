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
import itch.tsp.modelo.ReportePreliminar;

@Component
public class ReportePreliminarPdf {

	private final ArchivoStorageService archivoStorageService;

	public ReportePreliminarPdf(ArchivoStorageService archivoStorageService) {
		this.archivoStorageService = archivoStorageService;
	}

	public String generarPdf(ReportePreliminar reportePreliminar) throws Exception {

		File carpeta = archivoStorageService.getRutaDirectorio("pdf").toFile();

		String nombreArchivo = "reporte_preliminar_" + reportePreliminar.getIdReportePreliminar() + ".pdf";
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

		Paragraph p2 = new Paragraph("REPORTE PRELIMINAR DE RESIDENCIA PROFESIONAL", fontTituloBlanco);
		p2.setAlignment(Element.ALIGN_CENTER);
		celdaTitulo.addElement(p2);

		tablaTitulo.addCell(celdaTitulo);
		document.add(tablaTitulo);

		document.add(Chunk.NEWLINE);

		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

		// ===== DATOS GENERALES =====
		Paragraph subtitulo1 = new Paragraph("DATOS GENERALES", fontSubtitulo);
		subtitulo1.setAlignment(Element.ALIGN_CENTER);
		document.add(subtitulo1);
		document.add(Chunk.NEWLINE);

		PdfPTable tablaDatos = new PdfPTable(2);
		tablaDatos.setWidthPercentage(100);
		tablaDatos.setWidths(new float[] { 2.0f, 4.5f });

		String nombreProyecto = "";
		String estudiante = "";

		if (reportePreliminar.getProyectoResidencia() != null) {
			nombreProyecto = valor(reportePreliminar.getProyectoResidencia().getNombreProyecto());

			if (reportePreliminar.getProyectoResidencia().getResidente() != null) {
				estudiante = valor(reportePreliminar.getProyectoResidencia().getResidente().getNombre()) + " "
						+ valor(reportePreliminar.getProyectoResidencia().getResidente().getApellidoPaterno()) + " "
						+ valor(reportePreliminar.getProyectoResidencia().getResidente().getApellidoMaterno());
			}
		}

		agregarFila(tablaDatos, "Proyecto", nombreProyecto, fontEtiqueta, fontTexto);
		agregarFila(tablaDatos, "Estudiante", estudiante, fontEtiqueta, fontTexto);
		agregarFila(tablaDatos, "Fecha de entrega",
				reportePreliminar.getFechaEntrega() != null ? sdf.format(reportePreliminar.getFechaEntrega()) : "",
				fontEtiqueta, fontTexto);
		agregarFila(tablaDatos, "Dictamen", valor(reportePreliminar.getDictamen()), fontEtiqueta, fontTexto);

		document.add(tablaDatos);
		document.add(Chunk.NEWLINE);

		// ===== OBJETIVO =====
		Paragraph subtitulo2 = new Paragraph("OBJETIVO DEL PROYECTO", fontSubtitulo);
		subtitulo2.setAlignment(Element.ALIGN_LEFT);
		document.add(subtitulo2);

		document.add(crearBloqueTexto(valor(reportePreliminar.getObjetivoProyecto()), fontTexto));
		document.add(Chunk.NEWLINE);

		// ===== ACTIVIDADES =====
		Paragraph subtitulo3 = new Paragraph("ACTIVIDADES REALIZADAS", fontSubtitulo);
		subtitulo3.setAlignment(Element.ALIGN_LEFT);
		document.add(subtitulo3);

		document.add(crearBloqueTexto(valor(reportePreliminar.getActividadesRealizadas()), fontTexto));
		document.add(Chunk.NEWLINE);

		// ===== AVANCE =====
		Paragraph subtitulo4 = new Paragraph("AVANCE GENERAL", fontSubtitulo);
		subtitulo4.setAlignment(Element.ALIGN_LEFT);
		document.add(subtitulo4);

		document.add(crearBloqueTexto(valor(reportePreliminar.getAvanceGeneral()), fontTexto));
		document.add(Chunk.NEWLINE);

		// ===== OBSERVACIONES =====
		Paragraph subtitulo5 = new Paragraph("OBSERVACIONES", fontSubtitulo);
		subtitulo5.setAlignment(Element.ALIGN_LEFT);
		document.add(subtitulo5);

		document.add(crearBloqueTexto(valor(reportePreliminar.getObservaciones()), fontTexto));
		document.add(Chunk.NEWLINE);

		// ===== OBSERVACIONES DICTAMEN =====
		Paragraph subtitulo6 = new Paragraph("OBSERVACIONES DEL DICTAMEN", fontSubtitulo);
		subtitulo6.setAlignment(Element.ALIGN_LEFT);
		document.add(subtitulo6);

		document.add(crearBloqueTexto(valor(reportePreliminar.getObservacionesDictamen()), fontTexto));
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

	private static PdfPTable crearBloqueTexto(String texto, Font fontTexto) {
		PdfPTable tabla = new PdfPTable(1);
		tabla.setWidthPercentage(100);

		PdfPCell celda = new PdfPCell(new Phrase(texto != null ? texto : "", fontTexto));
		celda.setBackgroundColor(new BaseColor(227, 236, 243));
		celda.setPadding(8f);

		tabla.addCell(celda);
		return tabla;
	}

	private static String valor(String texto) {
		return texto != null ? texto : "";
	}
}
