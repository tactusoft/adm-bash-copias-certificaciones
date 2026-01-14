package co.gov.sic.copiasycertificaciones.ws.client.soap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import co.gov.sic.bashcopiascertificaciones.utils.Constantes;
import co.gov.sic.bashcopiascertificaciones.utils.Utility;
import co.gov.sic.copiasycertificaciones.entities.ws.request.RequestSignPDF;
import co.gov.sic.copiasycertificaciones.entities.ws.response.ResponseSignMensajePDF;
import co.gov.sic.copiasycertificaciones.entities.ws.response.ResponseSignPDF;
import co.gov.sic.copiasycertificaciones.entities.ws.response.RespuestaObjSignPDF;

/**
 * Cliente SOAP para firma digital usando HttpURLConnection 100% compatible con
 * WSSignClient
 */
public class WSSignClient extends WSSoapClientBase {

	private static final Logger logger = LoggerFactory.getLogger(WSSignClient.class);
	private static final String NAMESPACE = "http://ws.certicamara.com.co/";
	private static final String SOAP_ACTION = "procesarPDF";
	private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

	private RequestSignPDF currentRequest;

	public WSSignClient() {
		super(NAMESPACE, SOAP_ACTION, Constantes.URL_WS_SIGN);
		logger.info("Cliente de firma SOAP inicializado");
	}

	public ResponseSignPDF Firmar(RequestSignPDF request) throws Exception {
		currentRequest = request;

		logger.info("╔════════════════════════════════════════════════════════╗");
		logger.info("║           INICIANDO PROCESO DE FIRMA PDF              ║");
		logger.info("╚════════════════════════════════════════════════════════╝");

		// Validaciones previas
		logger.info("Paso 1/4: Validando parámetros de entrada...");
		validateRequest(request);
		logger.info("✓ Parámetros validados correctamente");

		ResponseSignPDF response = new ResponseSignPDF();

		try {
			// Llamada al servicio SOAP
			logger.info("Paso 2/4: Llamando al servicio SOAP de firma...");
			Document document = call();
			logger.info("✓ Servicio SOAP respondió correctamente");

			// Parse de la respuesta
			logger.info("Paso 3/4: Procesando respuesta...");
			response = parseResponse(document);
			logger.info("✓ Respuesta procesada correctamente");

			logger.info("Paso 4/4: Validando documento firmado...");
			if (response.getDocumento() == null || response.getDocumento().length == 0) {
				throw new Exception("El documento firmado está vacío o es nulo");
			}
			logger.info("✓ Documento firmado recibido correctamente ({} bytes)", response.getDocumento().length);

			logger.info("╔════════════════════════════════════════════════════════╗");
			logger.info("║              FIRMA COMPLETADA EXITOSAMENTE             ║");
			logger.info("╚════════════════════════════════════════════════════════╝");

		} catch (Exception ex) {
			logger.error("╔════════════════════════════════════════════════════════╗");
			logger.error("║                  ERROR EN LA FIRMA                     ║");
			logger.error("╚════════════════════════════════════════════════════════╝");
			logger.error("Error: {}", ex.getMessage());

			if (ex.getCause() != null) {
				logger.error("Causa: {}", ex.getCause().getMessage());
			}

			throw ex;
		}

		return response;
	}

	/**
	 * Valida la petición antes de enviarla
	 */
	private void validateRequest(RequestSignPDF request) throws Exception {
		if (request == null) {
			throw new IllegalArgumentException("El request no puede ser null");
		}

		if (Utility.isNullOrEmptyTrim(request.getFilePath())) {
			throw new IllegalArgumentException("El FilePath no puede estar vacío");
		}

		Path filePath = Paths.get(request.getFilePath());

		// Verificar que el archivo existe
		if (!Files.exists(filePath)) {
			logger.error("El archivo no existe: {}", request.getFilePath());
			throw new IOException("El archivo no existe: " + request.getFilePath());
		}

		// Verificar que es un archivo
		if (!Files.isRegularFile(filePath)) {
			logger.error("La ruta no corresponde a un archivo válido: {}", request.getFilePath());
			throw new IOException("La ruta no corresponde a un archivo válido: " + request.getFilePath());
		}

		// Verificar que no está vacío
		long fileSize = Files.size(filePath);
		if (fileSize == 0) {
			logger.error("El archivo está vacío: {}", request.getFilePath());
			throw new IOException("El archivo está vacío: " + request.getFilePath());
		}

		// Verificar tamaño máximo
		if (fileSize > MAX_FILE_SIZE) {
			logger.error("El archivo excede el tamaño máximo ({} MB): {}", MAX_FILE_SIZE / (1024 * 1024),
					request.getFilePath());
			throw new IOException(String.format("El archivo excede el tamaño máximo permitido (%d MB): %s",
					MAX_FILE_SIZE / (1024 * 1024), request.getFilePath()));
		}

		logger.info("  • Archivo: {}", request.getFilePath());
		logger.info("  • Tamaño: {} bytes ({} KB)", fileSize, fileSize / 1024);
		logger.info("  • IdCliente: {}", request.getIdCliente());
		logger.info("  • IdPolitica: {}", request.getIdPolitica());

		// Validar otros parámetros
		if (Utility.isNullOrEmptyTrim(request.getIdCliente())) {
			throw new IllegalArgumentException("El IdCliente no puede estar vacío");
		}

		if (Utility.isNullOrEmptyTrim(request.getPasswordCliente())) {
			throw new IllegalArgumentException("El PasswordCliente no puede estar vacío");
		}

		if (request.getIdPolitica() == null) {
			throw new IllegalArgumentException("El IdPolitica no puede estar vacío");
		}
	}

	/**
	 * Parsea la respuesta del servicio SOAP
	 */
	private ResponseSignPDF parseResponse(Document document) throws Exception {
		ResponseSignPDF response = new ResponseSignPDF();
		XPathFactory xpathFactory = XPathFactory.newInstance();
		XPath xpath = xpathFactory.newXPath();

		try {
			// Extraer documento
			String val = xpath.evaluate("//documento", document);
			if (!Utility.isNullOrEmptyTrim(val)) {
				response.setDocumento(Base64.getDecoder().decode(val));
				logger.debug("  • Documento firmado: {} bytes", response.getDocumento().length);
			} else {
				logger.warn("  ⚠ La respuesta no contiene el documento firmado");
			}

			// Extraer ID de transacción
			val = xpath.evaluate("//idTransaccion", document);
			if (!Utility.isNullOrEmptyTrim(val)) {
				response.setIdTransaccion(Integer.parseInt(val));
				logger.debug("  • IdTransacción: {}", response.getIdTransaccion());
			}

			// Extraer objeto de respuesta
			RespuestaObjSignPDF responseObj = response.getRespuestaObj();

			val = xpath.evaluate("//numFirmantes", document);
			if (!Utility.isNullOrEmptyTrim(val)) {
				responseObj.setNumFirmantes(Integer.valueOf(val));
				logger.debug("  • Número de firmantes: {}", responseObj.getNumFirmantes());
			}

			val = xpath.evaluate("//verificado", document);
			if (!Utility.isNullOrEmptyTrim(val)) {
				responseObj.setVerificado(Boolean.parseBoolean(val));
				logger.debug("  • Verificado: {}", responseObj.getVerificado());
			}

			// Extraer mensajes
			ResponseSignMensajePDF responseMensaje = responseObj.getMensajes();

			val = xpath.evaluate("//codigo", document);
			if (!Utility.isNullOrEmptyTrim(val)) {
				responseMensaje.setCodigo(val);
				logger.info("  • Código de respuesta: {}", val);
			}

			val = xpath.evaluate("//mensaje", document);
			if (!Utility.isNullOrEmptyTrim(val)) {
				responseMensaje.setMensaje(val);
				logger.info("  • Mensaje: {}", val);

				// Si hay un mensaje de error, lanzar excepción
				if (responseMensaje.getCodigo() != null && !responseMensaje.getCodigo().equals("0")
						&& !responseMensaje.getCodigo().equals("OK")
						&& !responseMensaje.getCodigo().equalsIgnoreCase("SUCCESS")) {

					logger.error("El servicio de firma retornó un error");
					logger.error("  Código: {}", responseMensaje.getCodigo());
					logger.error("  Mensaje: {}", responseMensaje.getMensaje());

					throw new Exception(String.format("Error del servicio de firma - Código: %s, Mensaje: %s",
							responseMensaje.getCodigo(), responseMensaje.getMensaje()));
				}
			}

		} catch (Exception ex) {
			logger.error("Error al parsear la respuesta del servicio: {}", ex.getMessage());
			throw ex;
		}

		return response;
	}

	/**
	 * Implementación para llenar el body del mensaje SOAP Compatible con el método
	 * fillSOAPMessageBody de la clase padre
	 */
	@Override
	protected String fillSOAPMessageBody() throws Exception {
		try {
			logger.debug("  Construyendo parámetros del mensaje...");

			StringBuilder body = new StringBuilder();

			// Agregar elementos
			body.append(createXmlElement("idCliente", currentRequest.getIdCliente()));
			body.append(createXmlElement("passwordCliente", currentRequest.getPasswordCliente()));
			body.append(createXmlElement("idPolitica", currentRequest.getIdPolitica()));
			body.append(createXmlElement("stringToFind", currentRequest.getStringToFind()));
			body.append(createXmlElement("noPagina", currentRequest.getNoPagina()));

			// Leer y codificar el archivo
			logger.debug("  Leyendo y codificando archivo PDF...");
			byte[] fileBytes = Files.readAllBytes(Paths.get(currentRequest.getFilePath()));
			String base64File = Base64.getEncoder().encodeToString(fileBytes);

			logger.debug("  • Archivo original: {} bytes", fileBytes.length);
			logger.debug("  • Base64: {} caracteres", base64File.length());

			body.append(createXmlElement("pdf", base64File));

			logger.debug("  ✓ Parámetros del mensaje construidos");

			return body.toString();

		} catch (IOException ex) {
			logger.error("Error al leer el archivo PDF: {}", ex.getMessage());
			throw new Exception("No se pudo leer el archivo para firma: " + ex.getMessage(), ex);
		} catch (Exception ex) {
			logger.error("Error al construir el mensaje SOAP: {}", ex.getMessage());
			throw ex;
		}
	}
}