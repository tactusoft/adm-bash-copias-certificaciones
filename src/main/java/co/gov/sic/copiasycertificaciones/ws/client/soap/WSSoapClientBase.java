package co.gov.sic.copiasycertificaciones.ws.client.soap;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * Cliente SOAP base usando HttpURLConnection en lugar de SAAJ 100% compatible
 * con WSSoapClientBase
 */
public abstract class WSSoapClientBase {

	private final Logger LOGGER = LoggerFactory.getLogger(WSSoapClientBase.class);
	private final String NAMESPACE_URI;
	private final String SOAP_ACTION_NAME;
	private final String SOAP_ACTION;
	private final String WS_URL;

	/**
	 * Constructor compatible con WSSoapClientBase
	 * 
	 * @param nameSpaceURI   Namespace del servicio (ej:
	 *                       "http://ws.certicamara.com.co/")
	 * @param soapActionName Nombre de la acción SOAP (ej: "procesarPDF")
	 * @param wsUrl          URL del servicio web
	 */
	public WSSoapClientBase(String nameSpaceURI, String soapActionName, String wsUrl) {
		NAMESPACE_URI = nameSpaceURI;
		SOAP_ACTION_NAME = soapActionName;
		WS_URL = wsUrl;
		SOAP_ACTION = String.format("%s#%s", NAMESPACE_URI, SOAP_ACTION_NAME);
		LOGGER.info("Inicializando cliente SOAP - URL: {}, Action: {}", WS_URL, SOAP_ACTION);
	}

	/**
	 * Extrae el XML válido de la respuesta
	 */
	public String extractValidXmlResponse(String xmlResponse) {
		// Buscar el inicio del XML válido
		int startIndex = xmlResponse.indexOf("<?xml");
		if (startIndex == -1) {
			LOGGER.error("No se encontró el encabezado XML en la respuesta");
			LOGGER.error("Respuesta recibida (primeros 500 caracteres): {}",
					xmlResponse.substring(0, Math.min(500, xmlResponse.length())));
			throw new RuntimeException("No se encontró el encabezado XML en la respuesta.");
		}

		// Extraer el contenido XML desde el inicio hasta el final de la respuesta
		return xmlResponse.substring(startIndex).trim();
	}

	/**
	 * Verifica si el servicio está disponible antes de intentar la llamada SOAP
	 */
	private void verifyServiceAvailability() throws Exception {
		try {
			URL url = new URL(WS_URL);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(5000);
			conn.setReadTimeout(5000);

			int responseCode = conn.getResponseCode();
			LOGGER.debug("Verificación de servicio - Código HTTP: {}", responseCode);

			// Para SOAP, un 405 (Method Not Allowed) en GET es normal
			if (responseCode != 200 && responseCode != 405) {
				LOGGER.warn("El servicio respondió con código HTTP no esperado: {}", responseCode);
			}

			conn.disconnect();
		} catch (Exception ex) {
			LOGGER.error("No se pudo verificar la disponibilidad del servicio: {}", ex.getMessage());
			throw new Exception("El servicio SOAP no está disponible en: " + WS_URL, ex);
		}
	}

	/**
	 * Realiza la llamada SOAP usando HttpURLConnection Compatible 100% con el
	 * método call() de WSSoapClientBase
	 */
	public Document call() throws Exception {
		HttpURLConnection conn = null;
		Document document = null;

		LOGGER.info("════════════════════════════════════════════════════════");
		LOGGER.info("Iniciando llamada SOAP");
		LOGGER.info("URL: {}", WS_URL);
		LOGGER.info("Action: {}", SOAP_ACTION);
		LOGGER.info("════════════════════════════════════════════════════════");

		try {
			// Verificar disponibilidad del servicio
			LOGGER.debug("Verificando disponibilidad del servicio...");
			verifyServiceAvailability();
			LOGGER.debug("✓ Servicio disponible");

			// Crear mensaje SOAP como String XML
			LOGGER.debug("Creando mensaje SOAP...");
			String soapRequest = createSOAPRequest();
			LOGGER.debug("✓ Mensaje SOAP creado");

			// Log del request (opcional)
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Request SOAP (primeros 1000 caracteres): {}",
						soapRequest.substring(0, Math.min(1000, soapRequest.length())));
			}

			// Configurar conexión HTTP
			URL url = new URL(WS_URL);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			conn.setDoInput(true);

			// Timeouts
			conn.setConnectTimeout(60000); // 60 segundos
			conn.setReadTimeout(60000); // 60 segundos

			// Headers
			conn.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
			conn.setRequestProperty("SOAPAction", SOAP_ACTION);

			// Enviar petición
			LOGGER.info("→ Enviando petición SOAP a: {}", WS_URL);

			long startTime = System.currentTimeMillis();
			try (OutputStream os = conn.getOutputStream()) {
				os.write(soapRequest.getBytes("UTF-8"));
				os.flush();
			}

			// Leer respuesta
			int responseCode = conn.getResponseCode();
			long duration = System.currentTimeMillis() - startTime;

			LOGGER.info("✓ Respuesta SOAP recibida");
			LOGGER.debug("Código HTTP: {}", responseCode);
			LOGGER.debug("Tiempo de respuesta: {}ms", duration);

			// Determinar qué stream leer
			InputStream is = (responseCode < 400) ? conn.getInputStream() : conn.getErrorStream();

			if (is == null) {
				throw new Exception("El servicio no devolvió ninguna respuesta. HTTP Code: " + responseCode);
			}

			// Leer respuesta completa
			StringBuilder response = new StringBuilder();
			try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
				String line;
				while ((line = br.readLine()) != null) {
					response.append(line).append("\n");
				}
			}

			String xmlResponse = response.toString();

			LOGGER.debug("Respuesta SOAP recibida - Tamaño: {} bytes", xmlResponse.length());

			// Log de la respuesta (opcional)
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Response SOAP (primeros 1000 caracteres): {}",
						xmlResponse.substring(0, Math.min(1000, xmlResponse.length())));
			}

			// Verificar que no esté vacía
			if (xmlResponse.trim().isEmpty()) {
				throw new Exception("El servicio devolvió una respuesta vacía. HTTP Code: " + responseCode);
			}

			// Extraer XML válido
			LOGGER.debug("Extrayendo XML válido de la respuesta...");
			String validXmlResponse = extractValidXmlResponse(xmlResponse);
			LOGGER.debug("✓ XML válido extraído");

			// Parsear a Document
			LOGGER.debug("Parseando XML a Document...");
			InputSource source = new InputSource(new StringReader(validXmlResponse));
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			document = db.parse(source);
			LOGGER.debug("✓ Document creado exitosamente");

			LOGGER.info("════════════════════════════════════════════════════════");
			LOGGER.info("✓ Llamada SOAP completada exitosamente");
			LOGGER.info("════════════════════════════════════════════════════════");

		} catch (IOException ioEx) {
			LOGGER.error("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
			LOGGER.error("ERROR DE COMUNICACIÓN");
			LOGGER.error("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
			LOGGER.error("URL: {}", WS_URL);
			LOGGER.error("Mensaje: {}", ioEx.getMessage());
			LOGGER.error("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

			throw new Exception("Error al comunicarse con el servicio SOAP: " + ioEx.getMessage(), ioEx);

		} catch (Exception ex) {
			LOGGER.error("Error inesperado durante la llamada SOAP: {}", ex.getMessage(), ex);
			throw ex;

		} finally {
			if (conn != null) {
				conn.disconnect();
				LOGGER.debug("✓ Conexión HTTP cerrada");
			}
		}

		return document;
	}

	/**
	 * Crea el mensaje SOAP como String XML
	 */
	private String createSOAPRequest() throws Exception {
		try {
			StringBuilder soap = new StringBuilder();

			// Encabezado XML y Envelope
			soap.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			soap.append("<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" ");
			soap.append("xmlns:reg=\"").append(NAMESPACE_URI).append("\">");
			soap.append("<soap:Body>");
			soap.append("<reg:").append(SOAP_ACTION_NAME).append(">");

			// Llenar el body con los parámetros específicos
			LOGGER.debug("Llenando cuerpo del mensaje SOAP...");
			String bodyContent = fillSOAPMessageBody();
			soap.append(bodyContent);
			LOGGER.debug("✓ Cuerpo del mensaje llenado");

			// Cerrar tags
			soap.append("</reg:").append(SOAP_ACTION_NAME).append(">");
			soap.append("</soap:Body>");
			soap.append("</soap:Envelope>");

			return soap.toString();

		} catch (Exception ex) {
			LOGGER.error("Error al crear el mensaje SOAP: {}", ex.getMessage(), ex);
			throw new Exception("Error al construir el mensaje SOAP: " + ex.getMessage(), ex);
		}
	}

	/**
	 * Método abstracto para llenar el body del mensaje SOAP Las clases hijas deben
	 * implementar este método para agregar sus parámetros específicos
	 * 
	 * @return String XML con el contenido del body (sin los tags de envelope)
	 */
	protected abstract String fillSOAPMessageBody() throws Exception;

	/**
	 * Método helper para crear elementos XML
	 */
	protected String createXmlElement(String nombre, Object valor) {
		StringBuilder element = new StringBuilder();
		element.append("<").append(nombre).append(">");
		if (valor != null) {
			element.append(escapeXml(valor.toString()));
		}
		element.append("</").append(nombre).append(">");
		return element.toString();
	}

	/**
	 * Escapa caracteres especiales XML
	 */
	protected String escapeXml(String text) {
		if (text == null)
			return "";
		return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'",
				"&apos;");
	}
}