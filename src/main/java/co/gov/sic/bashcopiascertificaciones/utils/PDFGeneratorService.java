package co.gov.sic.bashcopiascertificaciones.utils;


import co.gov.sic.bashcopiascertificaciones.entities.Cesl_detalleSolicitud;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorkerFontProvider;
import com.itextpdf.tool.xml.XMLWorkerHelper;

import java.io.IOException;
import java.nio.charset.Charset;
import org.apache.log4j.Logger;

import sic.ws.interop.entities.Persona;

public class PDFGeneratorService {

    private static final Logger logger = GetLogger.getInstance("PDFGeneratorService", "conf/log4j.properties");

    public PDFGeneratorService() {

    }

    public static ByteArrayOutputStream createPdf(String content, String title, String subject, String keywords, boolean isCertificacion, String barcode) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {

            PDFHeaderFooter event = new PDFHeaderFooter(isCertificacion, true, true, barcode);
            Document document = new Document(PageSize.LETTER, PDFHeaderFooter.MARGIN_LEFT,
                    PDFHeaderFooter.MARGIN_RIGHT, PDFHeaderFooter.MARGIN_TOP + event.getTableHeaderHeight(),
                    PDFHeaderFooter.MARGIN_BOTTOM + event.getTableFooterHeight());
            PdfWriter writer = PdfWriter.getInstance(document, bos);
            writer.setFullCompression();
            writer.setPageEvent(event);
            document.open();
            try (ByteArrayInputStream input = new ByteArrayInputStream(content.getBytes(Constantes.UTF_8))) {
                String pathFonts = "../resource/webfonts/";

                // Pre-procesar el contenido HTML para convertir imágenes base64 en archivos temporales
                String processedContent = processBase64Images(content);

                ByteArrayInputStream processedInput = new ByteArrayInputStream(processedContent.getBytes(Constantes.UTF_8));
                XMLWorkerHelper worker = XMLWorkerHelper.getInstance();
                worker.parseXHtml(writer, document, processedInput, null, Charset.forName(Constantes.UTF_8), new XMLWorkerFontProvider(pathFonts));
            }//new StringReader("<p>helloworld</p>")); //
            document.addAuthor(Constantes.AUTHOR);
            document.addCreationDate();
            document.addProducer();
            document.addSubject(subject);
            document.addCreator(Constantes.AUTHOR);
            document.addTitle(title);
            document.addKeywords(keywords);
            document.close();

        } catch (IOException | DocumentException ex) {
            logger.error(ex.toString());
        }
        return bos;
    }

    public static void AddSignPage(String source, String target, Cesl_detalleSolicitud detalle, Persona radicador)
            throws Exception {
        byte[] fileBytes = null;
        /* List<X509Certificate> certificatesInfo = Utility.getPDFInfoCertificate(source);
        String nombreSecretarioCamra = CertificateInfo.getSubjectFields(certificatesInfo.get(0)).getField("CN");
        PdfReader reader = new PdfReader(source);
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            PDFHeaderFooter event = new PDFHeaderFooter(true, false, false, null);
            Document document = new Document(reader.getPageSize(1), PDFHeaderFooter.MARGIN_LEFT,
                    PDFHeaderFooter.MARGIN_RIGHT, PDFHeaderFooter.MARGIN_TOP + event.getTableHeaderHeight(),
                    PDFHeaderFooter.MARGIN_BOTTOM + event.getTableFooterHeight());
            PdfWriter writer = PdfWriter.getInstance(document, bos);
            writer.setFullCompression();
            writer.setPageEvent(event);
            document.open();
            Font fontBlack = new Font(Font.FontFamily.HELVETICA, 12f, Font.NORMAL);
            Font fontBlackBold = new Font(Font.FontFamily.HELVETICA, 12f, Font.BOLD);
            Font fontBlackBoldSmall = new Font(Font.FontFamily.HELVETICA, 10f, Font.BOLD);
            Paragraph p1 = new Paragraph(
                    String.format("La Secretaría General AD HOC de la Superintendencia de Industria y Comercio certifica que la firma estampada en este documento corresponde a %s, y se encuentra registrada en ésta Superintendencia.", nombreSecretarioCamra), fontBlack);
            p1.setSpacingAfter(40f);
            p1.setAlignment(Element.ALIGN_JUSTIFIED);
            document.add(p1);

            Paragraph p2 = new Paragraph(String.format("%s\n", Constantes.WS_SIGN_NOMBRE_SECRETARIO_AD_HOC), fontBlackBold);
            p2.setAlignment(Element.ALIGN_CENTER);
            p2.setSpacingBefore(60f);
            p2.add(new Chunk(Constantes.WS_SIGN_CARGO_SECRETARIO_AD_HOC, fontBlackBoldSmall));

            document.add(p2);
            document.close();
            fileBytes = bos.toByteArray();
        }

        //Primero se remueven todas las firmas que puedea tener el certificado original
        String fileNameTemp = target.replace(".PDF", "_1.PDF");
        PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(fileNameTemp));
        stamper.setFormFlattening(true);
        stamper.close();
        reader.close();
        reader = new PdfReader(fileNameTemp);
        Document document = new Document();
        try (FileOutputStream out = new FileOutputStream(target)) {
            PdfCopy copy = new PdfCopy(document, out);
            copy.setFullCompression();

            document.open();
            int numPages = reader.getNumberOfPages();
            for (int index = 1; index <= numPages; index++) {
                copy.addPage(copy.getImportedPage(reader, index));
            }
            PdfReader reader2 = new PdfReader(fileBytes);
            copy.addPage(copy.getImportedPage(reader2, 1));

            Apostilla apostilla = new Apostilla();
            apostilla.setCantidadHojas(numPages);
            apostilla.setCodDocumento("2878");
            if (radicador.getEmails().size() > 0) {
                apostilla.setCorreoElectronico(radicador.getEmails().get(0).getDescripcion());
            }
            apostilla.setFechaDocumento(Utility.YYYY_MM_DD_LD.format(detalle.getFechaAdicional1()));
            apostilla.setIdAutoridad(Constantes.WS_CANCILLERIA_ID_AUTORIDAD_SECRETARIO_AD_HOC);

            apostilla.setNomCampoFirmaDigital("Signature1");
            apostilla.setNomTitularDocumento(detalle.getVariableAdicional1());
            apostilla.setNumDocumento(detalle.getVariableAdicional2());
            apostilla.setRazonSocialOrganizacion(Constantes.AUTHOR);
            apostilla.setTratamiento("1");
            //Es necesario colocar valor vacio y no nulo para que los campos aparezcan siempre en el xml como lo requiere el ministerio
            apostilla.setComplemento(Constantes.STR_EMPTY);
            apostilla.setVigenciaDocumento(Constantes.STR_EMPTY);

            document.addAuthor(Constantes.AUTHOR);
            document.addCreationDate();
            document.addProducer();
            document.addSubject(TipoTramite.CERTIFICADO_FIRMAS_SECRETARIOS_CAMARAS.getDescripcion());
            document.addCreator(Constantes.AUTHOR);
            document.addTitle(TipoTramite.CERTIFICADO_FIRMAS_SECRETARIOS_CAMARAS.getDescripcion());
            document.addKeywords(Constantes.KEYWORDS_PDF_FIRMA_SECRETARIO);
            document.addHeader("Apostilla", Utility.ConverToXML(apostilla));
            document.close();

            copy.close();
            reader2.close();
            reader.close();
        }

        Files.delete(Paths.get(fileNameTemp));*/
    }

    /**
     * Pre-procesa el HTML para convertir imágenes base64 embebidas en elementos Image de iText.
     * Busca todas las etiquetas img con src data:image y las convierte directamente en objetos Image.
     */
    private static String processBase64Images(String htmlContent) {
        try {
            // Patrón para encontrar imágenes base64 en el HTML
            // Busca: <img ... src="data:image/xxx;base64,XXXXX" ...>
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                "(<img[^>]+src=['\"])(data:image/([^;]+);base64,([^'\"]+))(['\"][^>]*>)",
                java.util.regex.Pattern.CASE_INSENSITIVE
            );

            java.util.regex.Matcher matcher = pattern.matcher(htmlContent);
            StringBuffer result = new StringBuffer();
            int imageCounter = 0;

            while (matcher.find()) {
                String fullDataUrl = matcher.group(2);
                String imageFormat = matcher.group(3); // png, jpg, etc.
                String base64Data = matcher.group(4);

                try {
                    // Decodificar base64
                    byte[] imageBytes = java.util.Base64.getDecoder().decode(base64Data);

                    // Crear archivo temporal
                    String tempFileName = System.getProperty("java.io.tmpdir") +
                        java.io.File.separator + "img_" + System.currentTimeMillis() + "_" +
                        imageCounter + "." + imageFormat;

                    java.io.File tempFile = new java.io.File(tempFileName);
                    java.nio.file.Files.write(tempFile.toPath(), imageBytes);
                    tempFile.deleteOnExit();

                    logger.info("Imagen base64 guardada en archivo temporal: " + tempFileName +
                        " (tamaño: " + imageBytes.length + " bytes)");

                    // Reemplazar la referencia base64 con la ruta del archivo temporal
                    String replacement = matcher.group(1) +
                        "file:///" + tempFileName.replace("\\", "/") +
                        matcher.group(5);

                    matcher.appendReplacement(result, java.util.regex.Matcher.quoteReplacement(replacement));
                    imageCounter++;

                } catch (Exception e) {
                    logger.error("Error al procesar imagen base64: " + e.getMessage(), e);
                    // Si falla, mantener el original
                    matcher.appendReplacement(result, java.util.regex.Matcher.quoteReplacement(matcher.group(0)));
                }
            }

            matcher.appendTail(result);

            if (imageCounter > 0) {
                logger.info("Se procesaron " + imageCounter + " imágenes base64");
                return result.toString();
            }

        } catch (Exception e) {
            logger.error("Error en processBase64Images: " + e.getMessage(), e);
        }

        // Si algo falla, devolver el contenido original
        return htmlContent;
    }
}
