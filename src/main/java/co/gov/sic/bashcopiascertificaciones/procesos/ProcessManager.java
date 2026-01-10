/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.gov.sic.bashcopiascertificaciones.procesos;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.itextpdf.text.BadElementException;

import co.gov.sic.bashcopiascertificaciones.database.DataBaseConnection;
import co.gov.sic.bashcopiascertificaciones.entities.Cesl_config;
import co.gov.sic.bashcopiascertificaciones.entities.Cesl_tramite;
import co.gov.sic.bashcopiascertificaciones.enums.EstadoTramite;
import co.gov.sic.bashcopiascertificaciones.enums.TipoTramite;
import co.gov.sic.bashcopiascertificaciones.utils.Constantes;
import co.gov.sic.bashcopiascertificaciones.utils.Functions;
import co.gov.sic.bashcopiascertificaciones.utils.GetLogger;
import co.gov.sic.bashcopiascertificaciones.utils.MailService;
import co.gov.sic.bashcopiascertificaciones.utils.PDFGeneratorService;
import co.gov.sic.bashcopiascertificaciones.utils.TemplateContent;
import co.gov.sic.bashcopiascertificaciones.utils.Utility;
import sic.ws.interop.api.InteropWSClient;
import sic.ws.interop.entities.Email;
import sic.ws.interop.entities.Perfil;
import sic.ws.interop.entities.Persona;
import sic.ws.interop.entities.ResponsableDepe;
import sic.ws.interop.entities.radicacion.Radicacion;
import sic.ws.interop.entities.response.ResponsePersona;
import sic.ws.interop.entities.response.ResponseResponsable;
import sic.ws.interop.entities.response.radicacion.ResponseRadicacion;

/**
 *
 * @author elmos
 */
public class ProcessManager {

	private static final Logger log = GetLogger.getInstance("ProcessManager", "conf/log4j.properties");
	private static final InteropWSClient wsInteropClient = new InteropWSClient(Constantes.WS_INTEROP_USER,
			Constantes.WS_INTEROP_PASS, Constantes.URL_WS_INTEROP, 500, false);

	public ProcessManager() throws BadElementException, IOException {
		log.info("Inicia proceso...");
	}

	public void respuestaRequerimientoInterno(int estado) {
		log.info("Proceso respuesta requerimiento interno" + " - " + estado);
		List<Cesl_tramite> tramites = DataBaseConnection.getInstance().getRequestPendingComplement(estado);
		tramites.forEach(tramite -> {
			this.respuestaRequerimiento(tramite);
		});
	}

	public void requerimientoInterno(int estado) {
		String proceso = "internal_days";
		String procesoTransfer = "transfer_days"; 
		log.info("Proceso requerimiento interno" + " - " + proceso);
		try {
			List<String> businessDays = wsInteropClient.utilFestivos(25);
			LocalDateTime currentDate = LocalDateTime.now();
			//LocalDateTime currentDate = LocalDateTime.of(2024, 12, 10, 21, 0); 
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			String currentDateString = currentDate.format(formatter);

			Cesl_config cfgInternal = DataBaseConnection.getInstance().getDayConfigParameters(proceso);
			Cesl_config cfgTransfer = DataBaseConnection.getInstance().getDayConfigParameters(procesoTransfer); 

			if (currentDate.getHour() >= 7 && currentDate.getDayOfWeek() != DayOfWeek.SATURDAY
					&& currentDate.getDayOfWeek() != DayOfWeek.SUNDAY && !businessDays.contains(currentDateString)) {

				List<Cesl_tramite> tramites = DataBaseConnection.getInstance().getRequestPendingComplement(estado);

				tramites.forEach(tramite -> {
					log.info("Procesando solicitud :" + tramite.getNume_radi());
					LocalDateTime lastModificationDate = tramite.getFecha_modificacion().plusDays(1);

					int daysCount = 0;
					while (lastModificationDate.isBefore(currentDate)) {
						if (cfgInternal.isBusinessDays()) {
							boolean isBusinessDay = lastModificationDate.getDayOfWeek() != DayOfWeek.SATURDAY
									&& lastModificationDate.getDayOfWeek() != DayOfWeek.SUNDAY
									&& !businessDays.contains(lastModificationDate.format(formatter));
							if (isBusinessDay) {
								daysCount++;
							}
						} else {
							daysCount++;
						}
						if (daysCount > Math.max(cfgInternal.getValor(), cfgTransfer.getValor())) {
							break; 
						}
						lastModificationDate = lastModificationDate.plusDays(1);
					}

					List<Cesl_tramite> cantidad = DataBaseConnection.getInstance().getInterRequeriment(
							tramite.getAno_radi(), tramite.getNume_radi(), tramite.getCodigoDependenciaDestino());
					if (daysCount >= cfgInternal.getValor() && cantidad.size() < 2) {
						log.info("Requerimiento Interno - Procesando solicitud: " + tramite.getNume_radi());
						generarRepuestaInterna(tramite, cfgTransfer, businessDays);
					} else if (daysCount >= cfgTransfer.getValor() && cantidad.size() >= 2) {
						log.info("Traslado - Procesando solicitud: " + tramite.getNume_radi());
						transladarPorCompetencias(tramite, cantidad);
					}
				});
			}
		} catch (IOException ex) {
			log.error(ex.toString());
		}
	}

	public void desistirSolicitudes() {
		try {
			log.info("Proceso desitir solicitudes - Complementar");
			List<String> businessDays = wsInteropClient.utilFestivos(25);
			LocalDateTime currentDate = LocalDateTime.now();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			String currentDateString = currentDate.format(formatter);
			if (currentDate.getHour() >= 7 && currentDate.getDayOfWeek() != DayOfWeek.SATURDAY
					&& currentDate.getDayOfWeek() != DayOfWeek.SUNDAY && !businessDays.contains(currentDateString)) {
				List<Cesl_tramite> tramites = DataBaseConnection.getInstance().getRequestDesistimientos();
				if (tramites.size() > 0) {
					tramites.forEach(tramite -> {
						log.info("Procesando radicado: " + tramite.getAno_radi() + "-" + tramite.getNume_radi());
						LocalDate lastModificationDate = tramite.getFecha_modificacion().toLocalDate().plusDays(1)
								.plusMonths(1);
						if (currentDate.toLocalDate().equals(lastModificationDate)
								|| currentDate.toLocalDate().isAfter(lastModificationDate)) {
							log.info("Ejecutado radicado: " + tramite.getAno_radi() + "-" + tramite.getNume_radi());
							cambiarActuacionDesitimiento(tramite, 757);
						}
					});
				}
			} else {
				log.info("Día no habil: " + currentDate);
			}
		} catch (IOException ex) {
			log.error(ex.toString());
		}
	}

	public void respuestaRequerimiento(Cesl_tramite tramite) {
		List<Cesl_tramite> tramites = DataBaseConnection.getInstance().getInterRequeriment(tramite.getAno_radi(),
				tramite.getNume_radi(), tramite.getCodigoDependenciaDestino());
		if (tramites.size() == 0) {
			DataBaseConnection.getInstance().setCeslTramiteEstado(tramite.getIdtramite(),
					EstadoTramite.RTA_INFO_AREA_INTERNA.getValue());
		}
	}

	public void probarTemplate() {
		Radicacion radicacion = new Radicacion();
		radicacion.setAnio((short) 2024);
		radicacion.setNumero(123456);
		radicacion.setControl("A1B2C3");
		radicacion.setConsecutivo(98765);
		radicacion.setSecuenciaEvento((short) 1);

		Perfil perfil = new Perfil();
		perfil.setTramite((short) 123);
		perfil.setNombreTramite("Nombre del Trámite");
		perfil.setEvento((short) 123);
		perfil.setNombreEvento("Nombre del Evento");
		perfil.setActuacion((short) 123);
		perfil.setNombreActuacion("Nombre de la Actuación");
		perfil.setDependencia((short) 123);
		perfil.setNombreDependencia("Nombre de la Dependencia");
		radicacion.setPerfil(perfil);

		radicacion.setFechaRadicacion(LocalDateTime.now());
		radicacion.setTotalFolios(50);

		Persona radicador = new Persona();
		radicador.setAnonimo(false);
		radicador.setNumeroDocumento(86776L);
		List<Email> emails = new ArrayList<>();
		emails.add(new Email());
		radicador.setEmails(emails);
		radicacion.setRadicador(radicador);

		Cesl_tramite tramite = new Cesl_tramite();
		tramite.setIdtramite(202020);
		tramite.setFecha_creacion(LocalDateTime.of(2023, 1, 1, 12, 0));
		tramite.setIdtiposolicitud(3);

		String subject = String.format("Radicación SIC %s", radicacion.getShortNumeroRadicacion());

		TemplateContent templateContent = new TemplateContent("Desitimiento");
		LocalDateTime fechaModificacion = LocalDateTime.now();
		String contentPDF = templateContent.buildRadicacionPDFTemplate(radicacion, tramite, fechaModificacion);

		String tiposol = tramite.getIdtiposolicitud().getDescripcion();
		int anio = radicacion.getFechaRadicacion().getYear();
		int idtramite = tramite.getIdtramite();
		int consRadi = radicacion.getConsecutivo();
		TipoTramite idtiposol = tramite.getIdtiposolicitud();

		ByteArrayOutputStream fileContent = PDFGeneratorService.createPdf(contentPDF, tiposol, subject,
				String.format(Constantes.KEYWORDS_PDF_RADICACION, anio), true,
				Utility.getBarCode(idtramite, consRadi, idtiposol));
		String fullPathRadicadoEntrada;
		try {
			fullPathRadicadoEntrada = Functions.saveFile(radicacion, fileContent);
			System.out.println(fullPathRadicadoEntrada);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void cambiarActuacionDesitimiento(Cesl_tramite tramite, int actuacion) {
		try {
			final LocalDateTime fechaModificacion = tramite.getFecha_modificacion();
			if (DataBaseConnection.getInstance().setCeslTramiteEstado(tramite.getIdtramite(),
					EstadoTramite.FINALIZADO.getValue())) {

				Radicacion radiSalida = new Radicacion();
				radiSalida.setAnio(tramite.getAno_radi());
				radiSalida.setNumero(tramite.getNume_radi());
				radiSalida.setControl(null);
				Perfil perfilRadicacion = new Perfil();
				perfilRadicacion.setActuacion((short) actuacion);
				perfilRadicacion.setDependencia((short) 104);
				perfilRadicacion.setEvento((short) 0);
				perfilRadicacion.setTramite((short) 362);
				radiSalida.setPerfil(perfilRadicacion);
				radiSalida.setMedioEntrada("SL");
				radiSalida.setIdTasa(1537);
				radiSalida.setTipoRadicacion("SA");
				radiSalida.setDependenciaDestino((short) 104);
				radiSalida.setIdFuncionario(tramite.getFunc_asignado());

				Persona radicador = new Persona();
				radicador.setId(tramite.getIden_pers());
				radicador.setRetornarSoloUltimosDatos(true);
				radicador = wsInteropClient.personaConsultar(radicador).getPersona();
				radiSalida.setRadicador(radicador);
				radiSalida.setTotalFolios(1);
				ResponseRadicacion responseRadicacion = wsInteropClient.radicacionRegistrar(radiSalida);
				if (responseRadicacion.getCodigo() == 0) {
					radiSalida = responseRadicacion.getRadicacion();
					TemplateContent templateContent = new TemplateContent("Desitimiento");
					String contentPDF = templateContent.buildRadicacionPDFTemplate(radiSalida, tramite,
							fechaModificacion);
					String subject = String.format("Radicación SIC %s", radiSalida.getShortNumeroRadicacion());

					ByteArrayOutputStream fileContent = PDFGeneratorService.createPdf(contentPDF,
							tramite.getIdtiposolicitud().getDescripcion(), subject,
							String.format(Constantes.KEYWORDS_PDF_RADICACION,
									radiSalida.getFechaRadicacion().getYear()),
							true, Utility.getBarCode(tramite.getIdtramite(), radiSalida.getConsecutivo(),
									tramite.getIdtiposolicitud()));

					String fullPathRadicadoEntrada = Functions.saveFile(radiSalida, fileContent);
					log.info("PDF Radicacion Entrada Copias: " + fullPathRadicadoEntrada);
					String fileName = String.format("%s.%s", radiSalida.getFullNumeroRadicacion(),
							Constantes.PDF_EXTENSION);
					radiSalida.addAdjunto(Utility.encodeBase64(fullPathRadicadoEntrada), fileName, true);
					ResponseRadicacion responseRadicacionAdjuntosRegistrar = wsInteropClient
							.radicacionAdjuntosRegistrar(radiSalida);
					if (responseRadicacionAdjuntosRegistrar.getCodigo() == 0) {
						MailService.Send(radicador.getEmails().get(0).getDescripcion(),
								tramite.getIdtiposolicitud().getDescripcion(), contentPDF, fullPathRadicadoEntrada);
					} else {
						log.info("PDF Radicacion Entrada Copias: " + responseRadicacionAdjuntosRegistrar.getMensaje());
					}
				} else {
					log.info("PDF Radicacion Entrada Copias: " + responseRadicacion.getMensaje());
				}
			}
		} catch (IOException ex) {
			log.error(ex.toString());
		} catch (Exception ex) {
			log.error(ex.toString());
		}
	}

	private void generarRepuestaInterna(Cesl_tramite tramite, Cesl_config cfg, List<String> businessDays) {
		try {
			if (DataBaseConnection.getInstance().setCeslTramiteEstado(tramite.getIdtramite(),
					EstadoTramite.SOL_INFO_AREA_INTERNA.getValue())) {
				Radicacion radiSalida = new Radicacion();
				radiSalida.setAnio(tramite.getAno_radi());
				radiSalida.setNumero(tramite.getNume_radi());
				radiSalida.setControl(null);
				Perfil perfilRadicacion = new Perfil();
				perfilRadicacion.setActuacion((short) 431);
				perfilRadicacion.setDependencia((short) 104);
				perfilRadicacion.setEvento((short) 0);
				perfilRadicacion.setTramite((short) 362);
				radiSalida.setPerfil(perfilRadicacion);
				radiSalida.setMedioEntrada(Constantes.WS_RADICACION_MEDIO_ENTRADA);
				radiSalida.setIdTasa(Constantes.WS_RADICACION_CONS_TASA);
				radiSalida.setTipoRadicacion("TR");
				radiSalida.setDependenciaDestino(tramite.getCodigoDependenciaDestino().shortValue());
				radiSalida.setObservaciones("Requerimiento Interno");

				radiSalida.setIdFuncionario(tramite.getFunc_asignado());

				Persona radicador = new Persona();
				radicador.setId(tramite.getIden_pers());
				radicador.setRetornarSoloUltimosDatos(true);
				radicador = wsInteropClient.personaConsultar(radicador).getPersona();

				Persona funcionario = new Persona();
				funcionario.setId(tramite.getFunc_asignado());
				funcionario.setRetornarSoloUltimosDatos(true);
				funcionario = wsInteropClient.personaConsultar(funcionario).getPersona();

				radiSalida.setRadicador(radicador);
				radiSalida.setTotalFolios(1);

				ResponseRadicacion responseRadicacion = wsInteropClient.radicacionRegistrar(radiSalida);
				if (responseRadicacion.getCodigo() == 0) {
					radiSalida = responseRadicacion.getRadicacion();

					Persona personaResponsable = new Persona();
					ResponseResponsable responseResponsable = wsInteropClient
							.dependenciaResponsable(radiSalida.getDependenciaDestino().intValue());

					ResponsableDepe responsableDepe = responseResponsable.getResponsable();

					personaResponsable.setId(Long.valueOf(responsableDepe.getIdenPers()));
					ResponsePersona responsePersona = wsInteropClient.personaConsultar(personaResponsable);
					personaResponsable = responsePersona.getPersona();

					TemplateContent templateContent = new TemplateContent("InfoAreaInterna");
					String contentPDF = templateContent.buildInfoAreaInternaPDFTemplate(radiSalida,
							tramite.getIdtiposolicitud(), personaResponsable, responsableDepe, radicador, cfg,
							businessDays, funcionario, tramite.getFecha_modificacion());
					String subject = String.format("Radicación SIC %s", radiSalida.getShortNumeroRadicacion());

					ByteArrayOutputStream fileContent = PDFGeneratorService.createPdf(contentPDF,
							tramite.getIdtiposolicitud().getDescripcion(), subject,
							String.format(Constantes.KEYWORDS_PDF_RADICACION,
									radiSalida.getFechaRadicacion().getYear()),
							false, Utility.getBarCode(tramite.getIdtramite(), radiSalida.getConsecutivo(),
									tramite.getIdtiposolicitud()));

					String fullPathRadicadoEntrada = Functions.saveFile(radiSalida, fileContent);
					log.info("PDF Radicacion Entrada Copias: " + fullPathRadicadoEntrada);
					String fileName = String.format("%s.%s", radiSalida.getFullNumeroRadicacion(),
							Constantes.PDF_EXTENSION);
					radiSalida.addAdjunto(Utility.encodeBase64(fullPathRadicadoEntrada), fileName, true);
					ResponseRadicacion responseRadicacionAdjuntosRegistrar = wsInteropClient
							.radicacionAdjuntosRegistrar(radiSalida);
					if (responseRadicacionAdjuntosRegistrar.getCodigo() == 0) {
						MailService.Send(radicador.getEmails().get(0).getDescripcion(),
								tramite.getIdtiposolicitud().getDescripcion(), contentPDF, fullPathRadicadoEntrada);
					} else {
						log.info("PDF Radicacion Entrada Copias: " + responseRadicacionAdjuntosRegistrar.getMensaje());
					}
				} else {
					log.info("PDF Radicacion Entrada Copias: " + responseRadicacion.getMensaje());
				}
			}
		} catch (IOException ex) {
			log.error(ex.toString());
		} catch (Exception ex) {
			log.error(ex.toString());
		}
	}

	private String getConsecutivos(List<Cesl_tramite> radicados) {
		int size = radicados.size();
		List<Cesl_tramite> ultimosRadicados = radicados.subList(size - 2, size);
		StringBuilder sb = new StringBuilder();
		for (Cesl_tramite radicado : ultimosRadicados) {
			sb.append(radicado.getCons_radi()).append(" y ");
		}
		sb.delete(sb.length() - 3, sb.length());
		return sb.toString();
	}

	public void transladarPorCompetencias(Cesl_tramite tramiteSolicitud, List<Cesl_tramite> radicados) {
		InteropWSClient wsInteropClient = new InteropWSClient(Constantes.WS_INTEROP_USER, Constantes.WS_INTEROP_PASS,
				Constantes.URL_WS_INTEROP, 500, true);
		ResponseResponsable responseResponsable;
		ResponseResponsable responseResponsableOrigen;
		ResponsableDepe responsableDepe;
		ResponsableDepe responsableDepeOrigen;

		try {
			String observaciones = "Traslado automático por reiteraciones de requerimientos internos";
			Radicacion radiSalida = new Radicacion();
			radiSalida.setAnio(tramiteSolicitud.getAno_radi());
			radiSalida.setNumero(tramiteSolicitud.getNume_radi());
			radiSalida.setControl(null);
			Perfil perfilRadicacion = new Perfil();
			perfilRadicacion.setActuacion((short) 470);
			perfilRadicacion.setDependencia((short) 104);
			perfilRadicacion.setEvento((short) 0);
			perfilRadicacion.setTramite((short) 362);
			radiSalida.setPerfil(perfilRadicacion);
			radiSalida.setMedioEntrada(Constantes.WS_RADICACION_MEDIO_SALIDA);

			radiSalida.setTipoRadicacion("TR");
			radiSalida.setDependenciaDestino(tramiteSolicitud.getCodigoDependenciaDestino().shortValue());

			radiSalida.setIdFuncionario(tramiteSolicitud.getFunc_asignado());

			Persona radicador = new Persona();
			radicador.setId(tramiteSolicitud.getIden_pers());
			radicador.setRetornarSoloUltimosDatos(true);
			radicador = wsInteropClient.personaConsultar(radicador).getPersona();
			radiSalida.setRadicador(radicador);
			radiSalida.setTotalFolios(1);
			radiSalida.setObservaciones(observaciones);

			Persona funcionario = new Persona();
			funcionario.setId(tramiteSolicitud.getFunc_asignado());
			funcionario.setRetornarSoloUltimosDatos(true);
			funcionario = wsInteropClient.personaConsultar(funcionario).getPersona();

			responseResponsable = wsInteropClient
					.dependenciaResponsable(tramiteSolicitud.getCodigoDependenciaDestino());
			responsableDepe = responseResponsable.getResponsable();

			responseResponsableOrigen = wsInteropClient.dependenciaResponsable(104);
			responsableDepeOrigen = responseResponsableOrigen.getResponsable();

			ResponseRadicacion responseRadicacion = wsInteropClient.radicacionRegistrar(radiSalida);

			if (responseRadicacion.getCodigo() == 0) {
				if (DataBaseConnection.getInstance().setCeslTramiteEstado(tramiteSolicitud.getIdtramite(),
						EstadoTramite.FINALIZADO.getValue())) {
					radiSalida = responseRadicacion.getRadicacion();
					TemplateContent templateContent = new TemplateContent("Traslado");
					String consecutivos = this.getConsecutivos(radicados);
					String contentPDF = templateContent.buildTrasladoPDFTemplate(radiSalida,
							tramiteSolicitud.getIdtiposolicitud(), responsableDepeOrigen, responsableDepe, funcionario,
							observaciones, consecutivos);

					String subject = String.format("Radicación SIC %s", radiSalida.getShortNumeroRadicacion());
					String fullPathRadicadoEntrada = null;
					ByteArrayOutputStream fileContent = PDFGeneratorService.createPdf(contentPDF,
							tramiteSolicitud.getIdtiposolicitud().getDescripcion(), subject,
							String.format(Constantes.KEYWORDS_PDF_RADICACION,
									radiSalida.getFechaRadicacion().getYear()),
							false, Utility.getBarCode(tramiteSolicitud.getIdtramite(), radiSalida.getConsecutivo(),
									tramiteSolicitud.getIdtiposolicitud()));

					fullPathRadicadoEntrada = Functions.saveFile(radiSalida, fileContent);
					log.info("PDF Radicacion Entrada Copias: " + fullPathRadicadoEntrada);

					String fileName = String.format("%s.%s", radiSalida.getFullNumeroRadicacion(),
							Constantes.PDF_EXTENSION);
					radiSalida.addAdjunto(Utility.encodeBase64(fullPathRadicadoEntrada), fileName, true);

					ResponseRadicacion responseRadicacionAdjuntos = wsInteropClient
							.radicacionAdjuntosRegistrar(radiSalida);
					if (responseRadicacionAdjuntos.getCodigo() == 0) {
						List<String> adjuntos = new ArrayList<>();

						adjuntos.add(fullPathRadicadoEntrada);
						List<String> listaEmails = new ArrayList<>();
						for (Email e : radicador.getEmails()) {
							listaEmails.add(e.getDescripcion());
						}

						String htmlEmail = templateContent.buildEmailTemplateCotizacion(tramiteSolicitud,
								listaEmails.get(0));

						MailService.Send(listaEmails,
								"Traslado - " + tramiteSolicitud.getAno_radi() + "-" + tramiteSolicitud.getNume_radi(),
								htmlEmail, adjuntos);
						log.info("Solicitud trasaladada correctamente");
					} else {
						log.info(responseRadicacionAdjuntos.getMensaje());
					}
				} else {
					log.info("No fue posible cambioar el estado en el sistema de copias");
				}
			} else {
				log.info(responseRadicacion.getMensaje());
			}

		} catch (Exception ex) {
			log.error(ex.toString());
		}

	}

}
