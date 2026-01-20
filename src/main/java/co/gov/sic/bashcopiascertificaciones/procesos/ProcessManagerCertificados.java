/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.gov.sic.bashcopiascertificaciones.procesos;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.itextpdf.text.BadElementException;

import co.gov.sic.bashcopiascertificaciones.database.DataBaseConnectionCertificados;
import co.gov.sic.bashcopiascertificaciones.entities.Cesl_detalleSolicitud;
import co.gov.sic.bashcopiascertificaciones.entities.Cesl_tramite;
import co.gov.sic.bashcopiascertificaciones.entities.Sancion;
import co.gov.sic.bashcopiascertificaciones.enums.EstadoTramite;
import co.gov.sic.bashcopiascertificaciones.enums.TipoSancion;
import co.gov.sic.bashcopiascertificaciones.utils.Constantes;
import co.gov.sic.bashcopiascertificaciones.utils.GetLogger;
import co.gov.sic.bashcopiascertificaciones.utils.MailService;
import co.gov.sic.bashcopiascertificaciones.utils.PDFGeneratorService;
import co.gov.sic.bashcopiascertificaciones.utils.TemplateContent;
import co.gov.sic.bashcopiascertificaciones.utils.Utility;
import co.gov.sic.copiasycertificaciones.entities.ws.request.RequestSignPDF;
import co.gov.sic.copiasycertificaciones.entities.ws.response.ResponseSignPDF;
import co.gov.sic.copiasycertificaciones.ws.client.soap.WSSignClient;
import sic.ws.interop.api.InteropWSClient;
import sic.ws.interop.entities.Perfil;
import sic.ws.interop.entities.Persona;
import sic.ws.interop.entities.Tramite;
import sic.ws.interop.entities.radicacion.Radicacion;
import sic.ws.interop.entities.request.RequestTramite;
import sic.ws.interop.entities.response.ResponseTramite;
import sic.ws.interop.entities.response.radicacion.ResponseRadicacion;

/**
 *
 * @author elmos
 */
public class ProcessManagerCertificados {

	private static final Logger log = GetLogger.getInstance("ProcessManager", "conf/log4j.properties");
	private static final InteropWSClient wsInteropClient = new InteropWSClient(Constantes.WS_INTEROP_USER,
			Constantes.WS_INTEROP_PASS, Constantes.URL_WS_INTEROP, 500, false);
	private static final String DIRECTORY = "/Users/carlossarmiento/Developer/SIC/copias/documentos/SL/Copias/CERTIFICACIONES";

	public ProcessManagerCertificados() throws BadElementException, IOException {
		log.info("Inicia proceso...");
	}

	public void pending() throws Exception {
		List<Cesl_tramite> tramites = DataBaseConnectionCertificados.getInstance().getRequestPending();
		if (tramites.size() > 0) {
			Perfil perfilRadicacion = DataBaseConnectionCertificados.getInstance().getPerfilCertificado();

			RequestSignPDF requestSign = new RequestSignPDF();
			requestSign.setPasswordCliente(Constantes.WS_SIGN_PASS);
			requestSign.setIdCliente(Constantes.WS_SIGN_USER);
			requestSign.setIdPolitica(Constantes.WS_SIGN_ID_POLITICA_SIN_ESTAMPA);
			requestSign.setStringToFind(Constantes.WS_SIGN_NOMBRE_SECRETARIO_AD_HOC);
			requestSign.setNoPagina("0");

			tramites.forEach(tramite -> {
				log.info("Procesando radicado: " + tramite.getIdtramite());
				Cesl_detalleSolicitud detalle = DataBaseConnectionCertificados.getInstance()
						.getRequestDetail(tramite.getIdtramite());
				if (detalle != null) {
					// log.info("Tipo de documento: " + detalle.getTipo_docu());
					// log.info("Numero de documento: " + detalle.getNume_docu());
					for (int index = 1; index <= detalle.getCantidad(); index++) {
						try {
							TipoSancion tipoSancion = TipoSancion.fromValue(detalle.getTipo_certifica());
							LocalDateTime now = LocalDateTime.now();
							LocalDateTime finalDate = Utility.getStartDateFromTodayAnYears(detalle.getAnos(), now);
							List<Sancion> sanciones = DataBaseConnectionCertificados.getInstance().consultarSanciones(
									detalle.getTipo_docu(), detalle.getNume_docu(), tipoSancion, finalDate, now);
							log.info("sanciones: " + sanciones.size());
							TemplateContent templateContent = new TemplateContent("CertificadoDIS");
							String html = templateContent.buildCertificadoSancionesPDFTemplate(detalle, tipoSancion,
									sanciones, finalDate, now);

							// Obtener los bytes de la firma para agregarla programáticamente al PDF
							byte[] firmaBytes = templateContent.getFirmaImageBytes();

							try (ByteArrayOutputStream fileContent = PDFGeneratorService.createPdf(html,
									"Certificado Demandas, Investigaciones y Sanciones",
									tramite.getIdtiposolicitud().getDescripcion(), Constantes.KEYWORDS_PDF_DEMANDAS,
									true, null, firmaBytes)) {
								String fileName = String.format(
										"%s_Certificado Demandas, Investigaciones y Sanciones_%s_%s%s_%s.%s",
										detalle.getIdtramite(), detalle.getTipo_certifica(), detalle.getTipo_docu(),
										detalle.getNume_docu(), index, Constantes.PDF_EXTENSION);

								Path fullPath = Paths.get(DIRECTORY, fileName);
								Files.write(fullPath, fileContent.toByteArray());

								requestSign.setFilePath(fullPath.toString());
								String base64EncodedPdf = this.signDocument(requestSign, fileName);

								log.info("Archivo guardado en: " + fullPath.toString());

								Radicacion radi = new Radicacion();
								Persona radicador = new Persona();
								radicador.setId(tramite.getIden_pers());

								radicador.setRetornarSoloUltimosDatos(true);
								InteropWSClient wsInteropClient = Utility.GetWSClient();
								radicador = wsInteropClient.personaConsultar(radicador).getPersona();
								radi.setRadicador(radicador);
								radi.setPerfil(perfilRadicacion);
								radi.setTipoRadicacion(Constantes.TIPO_RADICACION_ENTRADA);
								radi.setTotalFolios(1);
								radi.setMedioEntrada(Constantes.WS_RADICACION_MEDIO_ENTRADA);
								radi.setIdFuncionario(Constantes.WS_RADICACION_FUNCIONARIO_RADICADOR_ID);
								radi.setIdTasa(Constantes.WS_RADICACION_CONS_TASA);
								ResponseRadicacion responseRadicacion = new ResponseRadicacion();
								radi.setObservaciones(
										"Se genera este certificado por problemas en las tarifas a través de un proceso masivo.");
								responseRadicacion = wsInteropClient.radicacionRegistrar(radi);
								if (responseRadicacion.getCodigo() == 0) {
									radi = responseRadicacion.getRadicacion();
									radi.setRadicador(radicador);
									tramite.setAno_radi(radi.getAnio());
									tramite.setNume_radi(radi.getNumero());
									tramite.setCont_radi(radi.getControl());
									tramite.setCons_radi(radi.getConsecutivo());
									tramite.setEstado(EstadoTramite.FINALIZADO);

									radi.addAdjunto(base64EncodedPdf, fileName, true);
									ResponseRadicacion responseRadicacionAdjuntos = wsInteropClient
											.radicacionAdjuntosRegistrar(radi);

									if (responseRadicacionAdjuntos.getCodigo() == 0) {
										DataBaseConnectionCertificados.getInstance().actualizarTramite(tramite);
										
										String htmlEmail = templateContent.buildEmailTemplate(radi,
												tramite.getIdtiposolicitud());
										List<String> adjuntos = new LinkedList<>();
										adjuntos.add(fullPath.toString());
										MailService.Send(radi, tramite.getIdtiposolicitud().getDescripcion(),
												htmlEmail, adjuntos);
									}
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}

					}
				}
			});
		}
	}

	private String signDocument(RequestSignPDF requestSign, String fileName) throws Exception {
		int attempts = 0;
		boolean success = false;
		ResponseSignPDF responseSign = null;

		while (attempts < 6 && !success) {
			try {
				WSSignClient wsSign = new WSSignClient();
				responseSign = wsSign.Firmar(requestSign);
				if (responseSign != null && responseSign.getDocumento() != null) {
					success = true;
				} else {
					throw new Exception("Error en la firma: Documento no disponible.");
				}
			} catch (Exception ex) {
				attempts++;
				log.error("Intento " + attempts + " fallido al firmar el PDF: " + ex.getMessage());

				if (attempts >= 6) {
					log.error("Fallo la firma del PDF tras 6 intentos.");
					throw new Exception("Error tras varios intentos al firmar el documento.");
				}

				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					log.error("Error al dormir el hilo entre intentos de firma: " + e.getMessage());
				}
			}
		}

		if (responseSign != null && responseSign.getDocumento() != null) {
			byte[] filesBytes = responseSign.getDocumento();
			if (filesBytes != null) {
				Path fullPath = Paths.get(DIRECTORY, fileName);
				Files.write(fullPath, filesBytes);
				log.info("Archivo firmado guardado en: " + fullPath.toString());
				return Base64.getEncoder().encodeToString(filesBytes);
			} else {
				log.error("PDF Firma Certificado: " + responseSign.getRespuestaObj().getMensajes().getMensaje());
			}
		}

		return null;
	}

	public void pending3() throws Exception {
		// Array de radicados a procesar (formato: año-número)
		String[][] radicados = { { "26", "10399" }, { "26", "10400" }, { "26", "10402" }, { "26", "10403" },
				{ "26", "10404" }, { "26", "10405" }, { "26", "10406" }, { "26", "10407" }, { "26", "10408" },
				{ "26", "10409" }, { "26", "10412" }, { "26", "10413" }, { "26", "10414" }, { "26", "10415" } };

		Cesl_tramite tramite = DataBaseConnectionCertificados.getInstance().getRequestPending2().get(0);
		if (tramite != null) {
			Perfil perfilRadicacion = DataBaseConnectionCertificados.getInstance().getPerfilCertificado();
			RequestSignPDF requestSign = new RequestSignPDF();
			requestSign.setPasswordCliente(Constantes.WS_SIGN_PASS);
			requestSign.setIdCliente(Constantes.WS_SIGN_USER);
			requestSign.setIdPolitica(Constantes.WS_SIGN_ID_POLITICA_SIN_ESTAMPA);
			requestSign.setStringToFind(Constantes.WS_SIGN_NOMBRE_SECRETARIO_AD_HOC);
			requestSign.setNoPagina("0");

			log.info("Procesando tramite: " + tramite.getIdtramite());
			Cesl_detalleSolicitud detalle = DataBaseConnectionCertificados.getInstance()
					.getRequestDetail(tramite.getIdtramite());

			if (detalle != null) {
				TipoSancion tipoSancion = TipoSancion.fromValue(detalle.getTipo_certifica());
				LocalDateTime now = LocalDateTime.now();
				LocalDateTime finalDate = Utility.getStartDateFromTodayAnYears(detalle.getAnos(), now);
				List<Sancion> sanciones = DataBaseConnectionCertificados.getInstance().consultarSanciones(
						detalle.getTipo_docu(), detalle.getNume_docu(), tipoSancion, finalDate, now);
				log.info("sanciones: " + sanciones.size());

				// Iterar sobre cada radicado del array
				for (String[] radicado : radicados) {
					short anoRadi = Short.parseShort(radicado[0]);
					int numeRadi = Integer.parseInt(radicado[1]);

					try {
						// Consultar la radicación existente
						InteropWSClient wsInteropClient = Utility.GetWSClient();
						RequestTramite request = new RequestTramite();
						request.setAnio(anoRadi);
						request.setNumero(numeRadi);
						request.setExcluirDependencias(false);
						ResponseTramite responseConsulta = wsInteropClient.radicacionConsultar(request);

						if (responseConsulta.getCodigo() == 0) {
							log.info("Procesando radicado: " + anoRadi + "-" + numeRadi);
							List<Tramite> expedientes = responseConsulta.getExpedientes();
							if (!expedientes.isEmpty()) {
								Tramite tram = expedientes.get(0);

								Persona radicador = new Persona();
								radicador.setId(tramite.getIden_pers());
								radicador.setRetornarSoloUltimosDatos(true);
								radicador = wsInteropClient.personaConsultar(radicador).getPersona();

								Radicacion radiSalida = new Radicacion();
								radiSalida.setAnio(tram.getAnio());
								radiSalida.setNumero(tram.getNumero());
								radiSalida.setControl(tram.getControl());
								perfilRadicacion.setActuacion((short) 440);
								radiSalida.setPerfil(perfilRadicacion);
								radiSalida.setMedioEntrada(Constantes.WS_RADICACION_MEDIO_ENTRADA);
								radiSalida.setIdFuncionario(Constantes.WS_RADICACION_FUNCIONARIO_RADICADOR_ID);
								radiSalida.setRadicador(radicador);
								radiSalida.setTotalFolios(1);
								radiSalida.setIdTasa(Constantes.WS_RADICACION_CONS_TASA);
								radiSalida.setTipoRadicacion(Constantes.TIPO_RADICACION_SALIDA);
								ResponseRadicacion responseRadicacion = wsInteropClient.radicacionRegistrar(radiSalida);
								if (responseRadicacion.getCodigo() == 0) {
									radiSalida = responseRadicacion.getRadicacion();
									TemplateContent templateContent = new TemplateContent("CertificadoDIS");
									String html = templateContent.buildCertificadoSancionesPDFTemplate(detalle,
											tipoSancion, sanciones, finalDate, now);

									byte[] firmaBytes = templateContent.getFirmaImageBytes();

									try (ByteArrayOutputStream fileContent = PDFGeneratorService.createPdf(html,
											"Certificado Demandas, Investigaciones y Sanciones",
											tramite.getIdtiposolicitud().getDescripcion(),
											Constantes.KEYWORDS_PDF_DEMANDAS, true, null, firmaBytes)) {
										String fileName = String.format(
												"%s_%s-%s_Certificado Demandas, Investigaciones y Sanciones_%s_%s%s.%s",
												detalle.getIdtramite(), anoRadi, numeRadi, detalle.getTipo_certifica(),
												detalle.getTipo_docu(), detalle.getNume_docu(),
												Constantes.PDF_EXTENSION);

										Path fullPath = Paths.get(DIRECTORY, fileName);
										Files.write(fullPath, fileContent.toByteArray());

										requestSign.setFilePath(fullPath.toString());
										String base64EncodedPdf = this.signDocument(requestSign, fileName);

										log.info("Archivo guardado en: " + fullPath.toString());

										// Agregar adjunto a la radicación existente
										radiSalida.addAdjunto(base64EncodedPdf, fileName, true);
										ResponseRadicacion responseRadicacionAdjuntos = wsInteropClient
												.radicacionAdjuntosRegistrar(radiSalida);

										if (responseRadicacionAdjuntos.getCodigo() == 0) {
											log.info("Adjunto registrado exitosamente para radicado: " + anoRadi + "-"
													+ numeRadi);

											String htmlEmail = templateContent.buildEmailTemplate(radiSalida,
													tramite.getIdtiposolicitud());
											List<String> adjuntos = new LinkedList<>();
											adjuntos.add(fullPath.toString());
											MailService.Send(radiSalida, tramite.getIdtiposolicitud().getDescripcion(),
													htmlEmail, adjuntos);
										} else {
											log.error("Error al registrar adjunto para radicado: " + anoRadi + "-"
													+ numeRadi + " - " + responseRadicacionAdjuntos.getMensaje());
										}
									}
								}
							}
						} else {
							log.error("Error al consultar radicado: " + anoRadi + "-" + numeRadi + " - "
									+ responseConsulta.getMensaje());
						}
					} catch (Exception e) {
						log.error("Error procesando radicado: " + anoRadi + "-" + numeRadi);
						e.printStackTrace();
					}
				}
			}
		}
	}

}
