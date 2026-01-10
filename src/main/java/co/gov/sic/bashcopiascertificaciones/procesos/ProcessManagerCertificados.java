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
import java.util.List;

import org.apache.log4j.Logger;

import com.itextpdf.text.BadElementException;

import co.gov.sic.bashcopiascertificaciones.database.DataBaseConnectionCertificados;
import co.gov.sic.bashcopiascertificaciones.entities.Cesl_detalleSolicitud;
import co.gov.sic.bashcopiascertificaciones.entities.Cesl_tramite;
import co.gov.sic.bashcopiascertificaciones.entities.Sancion;
import co.gov.sic.bashcopiascertificaciones.enums.TipoSancion;
import co.gov.sic.bashcopiascertificaciones.utils.Constantes;
import co.gov.sic.bashcopiascertificaciones.utils.GetLogger;
import co.gov.sic.bashcopiascertificaciones.utils.PDFGeneratorService;
import co.gov.sic.bashcopiascertificaciones.utils.TemplateContent;
import co.gov.sic.bashcopiascertificaciones.utils.Utility;
import sic.ws.interop.api.InteropWSClient;

/**
 *
 * @author elmos
 */
public class ProcessManagerCertificados {

	private static final Logger log = GetLogger.getInstance("ProcessManager", "conf/log4j.properties");
	private static final InteropWSClient wsInteropClient = new InteropWSClient(Constantes.WS_INTEROP_USER,
			Constantes.WS_INTEROP_PASS, Constantes.URL_WS_INTEROP, 500, false);

	public ProcessManagerCertificados() throws BadElementException, IOException {
		log.info("Inicia proceso...");
	}

	public void pending() {
		List<Cesl_tramite> tramites = DataBaseConnectionCertificados.getInstance().getRequestPending();
		if (tramites.size() > 0) {
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
								
								String directorio = "/Users/carlossarmiento/Developer/SIC/copias/documentos/SL/Copias/CERTIFICACIONES";
							    Path rutaCompleta = Paths.get(directorio, fileName);
							    Files.write(rutaCompleta, fileContent.toByteArray());
							    
							    log.info("Archivo guardado en: " + rutaCompleta.toString());
							}
						} catch (Exception e) {
							e.printStackTrace();
						}

					}
				}
			});
		}
	}

}
