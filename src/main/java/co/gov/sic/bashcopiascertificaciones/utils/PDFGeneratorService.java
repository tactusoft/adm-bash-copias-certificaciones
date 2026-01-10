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
        return createPdf(content, title, subject, keywords, isCertificacion, barcode, null);
    }

    public static ByteArrayOutputStream createPdf(String content, String title, String subject, String keywords, boolean isCertificacion, String barcode, byte[] firmaImageBytes) {
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

                XMLWorkerHelper worker = XMLWorkerHelper.getInstance();
                worker.parseXHtml(writer, document, input, null, Charset.forName(Constantes.UTF_8), new XMLWorkerFontProvider(pathFonts));
            }//new StringReader("<p>helloworld</p>")); //

            // Agregar metadatos antes de cerrar el documento
            document.addAuthor(Constantes.AUTHOR);
            document.addCreationDate();
            document.addProducer();
            document.addSubject(subject);
            document.addCreator(Constantes.AUTHOR);
            document.addTitle(title);
            document.addKeywords(keywords);

            // Cerrar el documento para finalizar el contenido HTML
            document.close();

            // Si se proporcionó una imagen de firma, agregarla en posición absoluta
            if (firmaImageBytes != null && firmaImageBytes.length > 0) {
                try {
                    // Reabrir el PDF para agregar la firma en posición absoluta
                    com.itextpdf.text.pdf.PdfReader reader = new com.itextpdf.text.pdf.PdfReader(bos.toByteArray());
                    ByteArrayOutputStream finalBos = new ByteArrayOutputStream();
                    com.itextpdf.text.pdf.PdfStamper stamper = new com.itextpdf.text.pdf.PdfStamper(reader, finalBos);

                    // Obtener el PdfContentByte de la última página
                    int totalPages = reader.getNumberOfPages();
                    com.itextpdf.text.pdf.PdfContentByte canvas = stamper.getOverContent(totalPages);

                    // Buscar la posición del nombre del secretario en el PDF
                    TextPositionFinder finder = new TextPositionFinder(Constantes.WS_SIGN_NOMBRE_SECRETARIO_AD_HOC);
                    com.itextpdf.text.pdf.parser.PdfReaderContentParser parser =
                        new com.itextpdf.text.pdf.parser.PdfReaderContentParser(reader);
                    parser.processContent(totalPages, finder);

                    // Crear la imagen y configurar su tamaño
                    com.itextpdf.text.Image firmaImage = com.itextpdf.text.Image.getInstance(firmaImageBytes);
                    firmaImage.scaleToFit(230f, 100f);

                    // Calcular posición basándose en la ubicación del nombre del secretario
                    float xPosition = (PageSize.LETTER.getWidth() - 230f) / 2; // Centrado horizontalmente
                    float yPosition;

                    if (finder.isFound()) {
                        // Posicionar la firma justo arriba del nombre del secretario
                        // Agregar un offset para el espacio entre la firma y el nombre
                        yPosition = finder.getY() + 10f; // 10 puntos arriba del texto
                        logger.info("Posición del nombre encontrada en Y=" + finder.getY() + ", firma se posicionará en Y=" + yPosition);
                    } else {
                        // Si no se encuentra el texto, usar posición por defecto
                        yPosition = 200f;
                        logger.warn("No se encontró el nombre del secretario en el PDF, usando posición por defecto");
                    }

                    firmaImage.setAbsolutePosition(xPosition, yPosition);
                    canvas.addImage(firmaImage);

                    stamper.close();
                    reader.close();

                    // Reemplazar el contenido del ByteArrayOutputStream original con el nuevo
                    bos.reset();
                    bos.write(finalBos.toByteArray());

                    logger.info("Firma agregada programáticamente al PDF en posición absoluta (tamaño: " + firmaImageBytes.length + " bytes)");
                } catch (Exception e) {
                    logger.error("Error al agregar firma al PDF: " + e.getMessage(), e);
                }
            }

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
     * Clase auxiliar para encontrar la posición de un texto específico en el PDF.
     * Implementa RenderListener para procesar el contenido del PDF y encontrar coordenadas.
     */
    private static class TextPositionFinder implements com.itextpdf.text.pdf.parser.RenderListener {
        private String textToFind;
        private float foundX = -1;
        private float foundY = -1;
        private boolean found = false;

        public TextPositionFinder(String textToFind) {
            this.textToFind = textToFind;
        }

        @Override
        public void beginTextBlock() {
            // No necesitamos implementar esto
        }

        @Override
        public void endTextBlock() {
            // No necesitamos implementar esto
        }

        @Override
        public void renderImage(com.itextpdf.text.pdf.parser.ImageRenderInfo renderInfo) {
            // No necesitamos procesar imágenes
        }

        @Override
        public void renderText(com.itextpdf.text.pdf.parser.TextRenderInfo renderInfo) {
            String text = renderInfo.getText();
            if (text != null && text.contains(textToFind)) {
                // Obtener la posición baseline del texto
                com.itextpdf.text.pdf.parser.Vector baseline = renderInfo.getBaseline().getStartPoint();
                foundX = baseline.get(com.itextpdf.text.pdf.parser.Vector.I1);
                foundY = baseline.get(com.itextpdf.text.pdf.parser.Vector.I2);
                found = true;
                logger.info("Texto encontrado: '" + text + "' en posición X=" + foundX + ", Y=" + foundY);
            }
        }

        public boolean isFound() {
            return found;
        }

        public float getX() {
            return foundX;
        }

        public float getY() {
            return foundY;
        }
    }
}
