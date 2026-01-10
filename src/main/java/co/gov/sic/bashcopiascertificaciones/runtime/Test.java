package co.gov.sic.bashcopiascertificaciones.runtime;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.List;

import co.gov.sic.bashcopiascertificaciones.entities.Cesl_tramite;
import co.gov.sic.bashcopiascertificaciones.utils.Constantes;
import co.gov.sic.bashcopiascertificaciones.utils.Functions;
import co.gov.sic.bashcopiascertificaciones.utils.PDFGeneratorService;
import co.gov.sic.bashcopiascertificaciones.utils.TemplateContent;
import co.gov.sic.bashcopiascertificaciones.utils.Utility;
import sic.ws.interop.api.InteropWSClient;
import sic.ws.interop.entities.Persona;
import sic.ws.interop.entities.ResponsableDepe;
import sic.ws.interop.entities.Tramite;
import sic.ws.interop.entities.radicacion.Radicacion;
import sic.ws.interop.entities.request.RequestTramite;
import sic.ws.interop.entities.response.ResponseResponsable;
import sic.ws.interop.entities.response.ResponseTramite;
import sic.ws.interop.entities.response.radicacion.ResponseRadicacion;

public class Test {

	public static void main(String[] args) {
		
	}
	
	
	private void generarRespuesta() {
		// System.out.println(DesEncrypter.encrypt("Sabanalarga2"));
				try {
					new Constantes();
					InteropWSClient wsInteropClient = new InteropWSClient(Constantes.WS_INTEROP_USER,
							Constantes.WS_INTEROP_PASS, Constantes.URL_WS_INTEROP, 500, false);
					RequestTramite request = new RequestTramite();
					request.setAnio((short) 24);
					request.setNumero(446966);
					request.setExcluirDependencias(false);
					ResponseTramite response = wsInteropClient.radicacionConsultar(request);
					List<Tramite> tramites = response.getExpedientes();
					for (Tramite tram : tramites) {
						
						if (tram.getConsecutivo().intValue() == 2) {

							ResponseResponsable responseResponsable = wsInteropClient
									.dependenciaResponsable(tram.getDependenciaDestino().intValue());
							ResponsableDepe responsableDepe = responseResponsable.getResponsable();

							Persona funcionario = new Persona();
							funcionario.setId(tram.getIdFuncionario());
							funcionario.setRetornarSoloUltimosDatos(true);
							funcionario = wsInteropClient.personaConsultar(funcionario).getPersona();

							Radicacion radiSalida = new Radicacion();
							radiSalida.setAnio(tram.getAnio());
							radiSalida.setNumero(tram.getNumero());
							radiSalida.setConsecutivo(tram.getConsecutivo());
							radiSalida.setFechaRadicacion(tram.getFechaRadicacion());
							radiSalida.setRadicador(funcionario);
							radiSalida.setControl(tram.getControl());
							radiSalida.setSecuenciaEvento(tram.getSecuenciaEvento());
							radiSalida.setPerfil(tram.getPerfil());
							radiSalida.setTotalFolios((int) tram.getTotalFolios());

							Persona radicador = new Persona();
							radicador.setId(tram.getIdPersona());
							radicador.setRetornarSoloUltimosDatos(true);
							radicador = wsInteropClient.personaConsultar(radicador).getPersona();
							radiSalida.setRadicador(radicador);

							TemplateContent templateContent = new TemplateContent("RespuestaComplemSol");
							String contentPDF = templateContent.buildRespuestaComplementarPDFTemplate(radiSalida,
									responsableDepe, funcionario,
									"El número de radicado es SD2022/0113408 (SIPI)");
							
							Cesl_tramite tramiteSolicitud = new Cesl_tramite();
							tramiteSolicitud.setIdtramite(19263);
							tramiteSolicitud.setFecha_creacion(tramites.get(0).getFechaRadicacion());
							tramiteSolicitud.setIdtiposolicitud(5);

							String subject = String.format("Radicación SIC %s", radiSalida.getShortNumeroRadicacion());
							ByteArrayOutputStream fileContent = PDFGeneratorService.createPdf(contentPDF,
									tramiteSolicitud.getIdtiposolicitud().getDescripcion(), subject,
									String.format(Constantes.KEYWORDS_PDF_RADICACION,
											radiSalida.getFechaRadicacion().getYear()),
									false, Utility.getBarCode(tramiteSolicitud.getIdtramite(), radiSalida.getConsecutivo(),
											tramiteSolicitud.getIdtiposolicitud()));
							String fullPathRadicadoEntrada = Functions.saveFile(radiSalida, fileContent);
							String fileName = String.format("%s.%s", radiSalida.getFullNumeroRadicacion(), Constantes.PDF_EXTENSION);
							String base64EncodedPdf = Base64.getEncoder().encodeToString(fileContent.toByteArray());
							radiSalida.addAdjunto(base64EncodedPdf, fileName, true);
							
							ResponseRadicacion responseRadicacionAdjuntos = wsInteropClient
									.radicacionAdjuntosRegistrar(radiSalida);

							if (responseRadicacionAdjuntos.getCodigo() == 0) {
								System.out.println(responseRadicacionAdjuntos.getMensaje());
							}

						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
	}

}
