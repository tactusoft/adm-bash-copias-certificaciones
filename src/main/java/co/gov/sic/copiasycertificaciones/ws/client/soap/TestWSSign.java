package co.gov.sic.copiasycertificaciones.ws.client.soap;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

public class TestWSSign {

    private static final String SERVICE_URL = "http://10.20.101.223:8080/WSSIGN-war/WSDigitalPDF";
    
    // Directorio donde se guardarán los PDFs firmados
    private static final String OUTPUT_DIR = "/Users/carlossarmiento/Developer/SIC/copias/documentos/firmados/";

    public static void main(String[] args) {
        try {
            System.out.println("════════════════════════════════════════════════════════");
            System.out.println("  PRUEBA DE FIRMA DIGITAL DE PDF CON CERTICÁMARA");
            System.out.println("════════════════════════════════════════════════════════\n");

            // Leer archivo
            String filePath = "/Users/carlossarmiento/Developer/SIC/copias/documentos/SL/Copias/PRUE25/25-101753/25-101753-_-00000-000.PDF";
            byte[] fileBytes = Files.readAllBytes(Paths.get(filePath));
            String base64File = Base64.getEncoder().encodeToString(fileBytes);

            System.out.println("→ Archivo leído: " + filePath);
            System.out.println("  Tamaño: " + fileBytes.length + " bytes (" + (fileBytes.length / 1024) + " KB)");
            System.out.println("  Base64: " + base64File.length() + " caracteres\n");

            // Crear mensaje SOAP
            String soapRequest =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
                               "xmlns:reg=\"http://ws.certicamara.com.co/\">" +
                "  <soap:Body>" +
                "    <reg:procesarPDF>" +
                "      <idCliente>Notificaciones</idCliente>" +
                "      <passwordCliente>knZt/32EFhkbCmVZRMI1Tg==</passwordCliente>" +
                "      <idPolitica>205</idPolitica>" +
                "      <stringToFind>ERIKA ANDREA PARRA SANABRIA</stringToFind>" +
                "      <noPagina>0</noPagina>" +
                "      <pdf>" + base64File + "</pdf>" +
                "    </reg:procesarPDF>" +
                "  </soap:Body>" +
                "</soap:Envelope>";

            System.out.println("→ Enviando petición SOAP a: " + SERVICE_URL);
            System.out.println("  Tamaño del mensaje: " + (soapRequest.length() / 1024) + " KB\n");

            // Enviar petición
            URL url = new URL(SERVICE_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(60000); // 60 segundos
            conn.setReadTimeout(60000);    // 60 segundos
            conn.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
            conn.setRequestProperty("SOAPAction", "http://ws.certicamara.com.co/#procesarPDF");

            // Escribir request
            long startTime = System.currentTimeMillis();
            try (OutputStream os = conn.getOutputStream()) {
                os.write(soapRequest.getBytes("UTF-8"));
            }

            // Leer respuesta
            int responseCode = conn.getResponseCode();
            long duration = System.currentTimeMillis() - startTime;
            
            System.out.println("→ Código de respuesta HTTP: " + responseCode);
            System.out.println("  Tiempo de respuesta: " + duration + "ms\n");

            InputStream is = (responseCode < 400) ? conn.getInputStream() : conn.getErrorStream();

            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line).append("\n");
                }
            }

            String responseStr = response.toString();
            
            System.out.println("════════════════════════════════════════════════════════");
            System.out.println("RESPUESTA DEL SERVICIO:");
            System.out.println("════════════════════════════════════════════════════════");
            System.out.println(responseStr);
            System.out.println("════════════════════════════════════════════════════════\n");

            // Procesar respuesta
            procesarRespuesta(responseStr, filePath);

            conn.disconnect();

        } catch (Exception ex) {
            System.err.println("\n✗✗✗ ERROR ✗✗✗");
            System.err.println("Mensaje: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Procesa la respuesta SOAP y descarga el PDF firmado si es exitoso
     */
    private static void procesarRespuesta(String responseStr, String originalFilePath) {
        try {
            // Verificar si hay código de respuesta
            

                // Extraer documento firmado
                if (responseStr.contains("<documento>")) {
                    String docBase64 = extractValue(responseStr, "documento");
                    
                    if (docBase64 != null && !docBase64.trim().isEmpty()) {
                        // Decodificar Base64
                        byte[] pdfBytes = Base64.getDecoder().decode(docBase64.trim());
                        
                        System.out.println("→ PDF firmado recibido: " + pdfBytes.length + " bytes (" + 
                                         (pdfBytes.length / 1024) + " KB)");

                        // Crear directorio de salida si no existe
                        File outputDir = new File(OUTPUT_DIR);
                        if (!outputDir.exists()) {
                            outputDir.mkdirs();
                            System.out.println("  Directorio creado: " + OUTPUT_DIR);
                        }

                        // Generar nombre de archivo
                        String originalFileName = new File(originalFilePath).getName();
                        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                        String outputFileName = originalFileName.replace(".PDF", "_FIRMADO_" + timestamp + ".PDF");
                        String outputPath = OUTPUT_DIR + outputFileName;

                        // Guardar archivo
                        Files.write(Paths.get(outputPath), pdfBytes);
                        
                        System.out.println("\n✓ ARCHIVO GUARDADO EXITOSAMENTE:");
                        System.out.println("  Ruta: " + outputPath);
                        System.out.println("  Tamaño: " + (pdfBytes.length / 1024) + " KB");
                        
                        // Verificar que el archivo se guardó correctamente
                        File savedFile = new File(outputPath);
                        if (savedFile.exists() && savedFile.length() == pdfBytes.length) {
                            System.out.println("  ✓ Verificación: Archivo guardado correctamente");
                        } else {
                            System.err.println("  ✗ Advertencia: El archivo puede no haberse guardado correctamente");
                        }
                        
                    } else {
                        System.err.println("✗ El elemento <documento> está vacío");
                    }
                } else {
                    System.err.println("✗ No se encontró el elemento <documento> en la respuesta");
                }
                
         

        } catch (Exception e) {
            System.err.println("\n✗ Error procesando la respuesta: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Extrae el valor de un elemento XML simple
     */
    private static String extractValue(String xml, String tagName) {
        try {
            String openTag = "<" + tagName + ">";
            String closeTag = "</" + tagName + ">";
            
            int startIdx = xml.indexOf(openTag);
            if (startIdx == -1) return null;
            
            startIdx += openTag.length();
            int endIdx = xml.indexOf(closeTag, startIdx);
            
            if (endIdx == -1) return null;
            
            return xml.substring(startIdx, endIdx);
            
        } catch (Exception e) {
            return null;
        }
    }
}