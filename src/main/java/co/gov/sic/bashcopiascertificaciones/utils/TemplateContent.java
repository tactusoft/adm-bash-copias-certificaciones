/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.gov.sic.bashcopiascertificaciones.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.DefaultTemplateResolver;

import co.gov.sic.bashcopiascertificaciones.entities.Cesl_config;
import co.gov.sic.bashcopiascertificaciones.entities.Cesl_detalleSolicitud;
import co.gov.sic.bashcopiascertificaciones.entities.Cesl_tramite;
import co.gov.sic.bashcopiascertificaciones.entities.Sancion;
import co.gov.sic.bashcopiascertificaciones.enums.TipoSancion;
import co.gov.sic.bashcopiascertificaciones.enums.TipoTramite;
import sic.ws.interop.entities.Direccion;
import sic.ws.interop.entities.Email;
import sic.ws.interop.entities.Perfil;
import sic.ws.interop.entities.Persona;
import sic.ws.interop.entities.ResponsableDepe;
import sic.ws.interop.entities.Telefono;
import sic.ws.interop.entities.radicacion.Radicacion;

/**
 *
 * @author elmos
 */
public class TemplateContent {

	private static final Logger log = GetLogger.getInstance("ProcessManager", "conf/log4j.properties");
	public final static DateTimeFormatter DD_MM_YYYY_HH_MM_AA_LD = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a",
			Locale.forLanguageTag("es-CO"));
	private final TemplateEngine templateEngine;
	private String templateName;

	public TemplateContent(String templateName) {
		this.templateName = templateName;
		DefaultTemplateResolver defTemplateResolver = new DefaultTemplateResolver();
		try {
			String fileName = "co/gov/sic/bashcopiascertificaciones/resource/templates/" + templateName + ".PDF.html";
			log.info("Buscando Plantilla utilizada: " + fileName);
			ClassLoader loader = TemplateContent.class.getClassLoader();
			StringBuffer sb = new StringBuffer();
			InputStream stream = loader.getResourceAsStream(fileName);
			InputStreamReader isReader = new InputStreamReader(stream);
			BufferedReader reader = new BufferedReader(isReader);
			String str;
			while ((str = reader.readLine()) != null) {
				sb.append(str);
			}
			defTemplateResolver.setTemplate(sb.toString());
			log.info("Plantilla generada");
		} catch (IOException ex) {
			log.error(ex.toString());
		}
		defTemplateResolver.setTemplateMode(TemplateMode.HTML);
		this.templateEngine = new TemplateEngine();
		this.templateEngine.addDialect(new Java8TimeDialect());
		this.templateEngine.setTemplateResolver(defTemplateResolver);
	}

	public String buildRadicacionPDFTemplate(Radicacion radi, Cesl_tramite tramite, LocalDateTime fechaModificacion) {
		Context context = new Context();
		context.setVariable("anonimo", radi.getRadicador().getAnonimo());
		context.setVariable("expediente", radi.getFullNumeroRadicacion());
		context.setVariable("fecha", DD_MM_YYYY_HH_MM_AA_LD.format(radi.getFechaRadicacion()));
		context.setVariable("fecha_inicial", DD_MM_YYYY_HH_MM_AA_LD.format(tramite.getFecha_creacion()));
		context.setVariable("fecha_modificacion", DD_MM_YYYY_HH_MM_AA_LD.format(fechaModificacion));
		Perfil perfil = radi.getPerfil();
		context.setVariable("codi_tramite", perfil.getTramite());
		context.setVariable("tramite", String.format("%s - %s", perfil.getTramite(), perfil.getNombreTramite()));
		context.setVariable("codi_evento", perfil.getEvento());
		context.setVariable("evento", String.format("%s - %s", perfil.getEvento(), perfil.getNombreEvento()));
		context.setVariable("codi_actuacion", perfil.getActuacion());
		context.setVariable("actuacion", String.format("%s - %s", perfil.getActuacion(), perfil.getNombreActuacion()));
		context.setVariable("codi_dependencia", perfil.getDependencia());
		context.setVariable("dependencia",
				String.format("%s - %s", perfil.getDependencia(), perfil.getNombreDependencia()));
		context.setVariable("folios", radi.getTotalFolios());
		context.setVariable(String.format("nombre%s", Constantes.STR_EMPTY), radi.getRadicador().getFullName());
		Email currentEmail = radi.getRadicador().getEmails().size() > 0 ? radi.getRadicador().getEmails().get(0) : null;
		context.setVariable("email", currentEmail);
		context.setVariable("asunto", tramite.getIdtiposolicitud().getDescripcion());
		context.setVariable("depe_resp", Constantes.SECRE_NOTIFICA_NAME);
		System.out.println(
				this.templateEngine.process(String.format("../templates/%s.%s", this.templateName, "PDF"), context));
		return this.templateEngine.process(String.format("../templates/%s.%s", this.templateName, "PDF.html"), context);
	}

	public String buildInfoAreaInternaPDFTemplate(Radicacion radi, TipoTramite tipoTramite, Persona personaResponsable,
			ResponsableDepe responsableDepe, Persona radicador, Cesl_config cfg, List<String> busnessDays,
			Persona funcionario, LocalDateTime fechaModificacion) throws Exception {
		Context context = new Context();
		context.setVariable("expediente", radi.getFullNumeroRadicacion());
		context.setVariable("fecha", Utility.DD_MM_YYYY_HH_MM_AA_LD.format(radi.getFechaRadicacion()));
		Perfil perfil = radi.getPerfil();
		context.setVariable("codi_tramite", perfil.getTramite());
		context.setVariable("tramite", String.format("%s - %s", perfil.getTramite(), perfil.getNombreTramite()));
		context.setVariable("codi_evento", perfil.getEvento());
		context.setVariable("evento", String.format("%s - %s", perfil.getEvento(), perfil.getNombreEvento()));
		context.setVariable("codi_actuacion", perfil.getActuacion());
		context.setVariable("actuacion", String.format("%s - %s", perfil.getActuacion(), perfil.getNombreActuacion()));
		context.setVariable("codi_dependencia", perfil.getDependencia());
		context.setVariable("dependencia",
				String.format("%s - %s", perfil.getDependencia(), perfil.getNombreDependencia()));
		context.setVariable("folios", radi.getTotalFolios());
		context.setVariable("nombre", personaResponsable.getFullName());
		context.setVariable("cargo", responsableDepe.getCargo());
		context.setVariable("solicitante", radicador.getFullName());
		context.setVariable("func_asig", funcionario.getFullName());
		context.setVariable("depe_resp", Constantes.SECRE_NOTIFICA_NAME);

		int days = 1;
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		LocalDateTime today = radi.getFechaRadicacion();
		if (cfg.isBusinessDays()) {
			while (days <= cfg.getValor()) {
				today = today.plusDays(1);
				if (!busnessDays.contains(today.format(formatter)) && today.getDayOfWeek() != DayOfWeek.SATURDAY
						&& today.getDayOfWeek() != DayOfWeek.SUNDAY) {
					days++;
				}
			}
		} else {
			today = today.plusDays(cfg.getValor());
		}

		context.setVariable("fecha_radicado", Utility.DD_MM_YYYY_HH_MM_AA_LD.format(fechaModificacion));
		context.setVariable("fecha_vencimiento", Utility.DD_MM_YYYY_HH_MM_AA_LD.format(today));

		return this.templateEngine.process(String.format("PDF/%s.%s", this.templateName, Constantes.PDF_EXTENSION),
				context);
	}

	private void AddRadicacion(Radicacion radi, Context context) {
		context.setVariable("anonimo", radi.getRadicador().getAnonimo());
		context.setVariable("expediente", radi.getFullNumeroRadicacion());
		context.setVariable("fecha", Utility.DD_MM_YYYY_HH_MM_AA_LD.format(radi.getFechaRadicacion()));
		Perfil perfil = radi.getPerfil();
		context.setVariable("codi_tramite", perfil.getTramite());
		context.setVariable("tramite", String.format("%s - %s", perfil.getTramite(), perfil.getNombreTramite()));
		context.setVariable("codi_evento", perfil.getEvento());
		context.setVariable("evento", String.format("%s - %s", perfil.getEvento(), perfil.getNombreEvento()));
		context.setVariable("codi_actuacion", perfil.getActuacion());
		context.setVariable("actuacion", String.format("%s - %s", perfil.getActuacion(), perfil.getNombreActuacion()));
		context.setVariable("codi_dependencia", perfil.getDependencia());
		context.setVariable("dependencia",
				String.format("%s - %s", perfil.getDependencia(), perfil.getNombreDependencia()));
		context.setVariable("folios", radi.getTotalFolios());
	}

	private void AddPersona(Persona pers, Context context, String sufijo) throws Exception {
		if (pers != null) {
			if (sufijo == null) {
				sufijo = Constantes.STR_EMPTY;
			}
			context.setVariable(String.format("nombre%s", sufijo), pers.getFullName());
			if (pers.getAnonimo() == null || !pers.getAnonimo()) {
				context.setVariable(String.format("identificacion%s", sufijo), String.format("%s %s",
						pers.getTipoDocumento(), Utility.tryFormatCurrencyNumber(pers.getNumeroDocumento(), false)));
				Direccion curretnDir = this.getDireccion(pers.getDirecciones());
				context.setVariable(String.format("direccion%s", sufijo),
						curretnDir != null ? curretnDir.getDescripcion() : Constantes.DEFAULT_PDF_NO_DATA_MESSAGE);

				if (curretnDir != null) {
					String region = curretnDir.getCodigoRegionDesc();
					String ciudad = curretnDir.getCodigoCiudadDesc();
					if (!Utility.isNullOrEmptyTrim(ciudad) && !Utility.isNullOrEmptyTrim(region)) {
						region = String.format("%s - %s", region, ciudad);
					} else {
						region = Constantes.STR_EMPTY;
					}
					context.setVariable(String.format("region%s", sufijo), region);
				}

				Telefono currentTel = curretnDir != null && curretnDir.getTelefonos().size() > 0
						? curretnDir.getTelefonos().get(0)
						: null;
				context.setVariable(String.format("telefono%s", sufijo),
						currentTel != null ? currentTel.getNumero() : Constantes.DEFAULT_PDF_NO_DATA_MESSAGE);
			}
			Email currentEmail = pers.getEmails().size() > 0 ? pers.getEmails().get(0) : null;
			context.setVariable(String.format("email%s", sufijo),
					currentEmail != null ? currentEmail.getDescripcion() : Constantes.DEFAULT_PDF_NO_DATA_MESSAGE);
		}
	}

	private Direccion getDireccion(List<Direccion> direcciones) {
		Direccion currentDir = null;
		for (Direccion direccion : direcciones) {
			if ("PE".equals(direccion.getTipo())) {
				currentDir = direccion;
				break;
			}
		}
		if (currentDir == null && !direcciones.isEmpty()) {
			currentDir = direcciones.get(0);
		}

		return currentDir;
	}

	public String buildTrasladoPDFTemplate(Radicacion radi, TipoTramite tipoTramite,
			ResponsableDepe responsableDepeOrigen, ResponsableDepe responsableDepe, Persona funcionario,
			String observaciones, String consecutivos) throws Exception {
		String nombrePdf = "traslado";
		Context context = new Context();
		AddRadicacion(radi, context);

		context.setVariable("nombre", responsableDepe.getNombreResponsable());
		context.setVariable("cargo", responsableDepe.getCargo());
		context.setVariable("solicitante", radi.getRadicador().getFullName());
		context.setVariable("observaciones", observaciones);
		context.setVariable("depe_resp", responsableDepeOrigen.getNombreResponsable());
		context.setVariable("func_asig", funcionario.getFullName());
		context.setVariable("consecutivos", consecutivos);
		return this.templateEngine.process(String.format("PDF/%s.%s", nombrePdf, Constantes.PDF_EXTENSION), context);
	}

	public String buildEmailTemplateCotizacion(Cesl_tramite tramite, String email) throws Exception {

		Context context = new Context();
		context.setVariable("nombre", tramite.getNombreSolicitante());
		context.setVariable("email", email);
		context.setVariable("expediente",
				String.valueOf(tramite.getAno_radi()) + "-" + String.valueOf(tramite.getNume_radi()));
		context.setVariable("tramite", 362);
		context.setVariable("evento", 0);
		context.setVariable("actuacion", 344);
		context.setVariable("fecha", tramite.getFecha_creacion());

		return this.templateEngine.process(String.format("EMAIL/Cotizacion.%s.EMAIL", "SA"), context);
	}

	public String buildRespuestaComplementarPDFTemplate(Radicacion radi, ResponsableDepe responsableDepe,
			Persona funcionario, String observaciones) throws Exception {
		String nombrePdf = "RespuestaComplemSol";
		Context context = new Context();
		AddRadicacion(radi, context);
		AddPersona(radi.getRadicador(), context, null);
		context.setVariable("observaciones", observaciones);
		context.setVariable("depe_resp", responsableDepe.getNombreResponsable());
		context.setVariable("func_asig", funcionario.getFullName());
		return this.templateEngine.process(String.format("PDF/%s.%s", nombrePdf, Constantes.PDF_EXTENSION), context);
	}

	public String buildCertificadoSancionesPDFTemplate(Cesl_detalleSolicitud detalle, TipoSancion tipoSancion,
			List<Sancion> sanciones, LocalDateTime fechaInicial, LocalDateTime fechaRadicacion) throws Exception {
		List<Sancion> demandas = new ArrayList<>();
		List<Sancion> multas = new ArrayList<>();
		for (Sancion s : sanciones) {
			if (s.getTipo() == TipoSancion.Demanda) {
				demandas.add(s);
			} else {
				multas.add(s);
			}
		}
		boolean showMultas = !multas.isEmpty();
		boolean showDemandas = !demandas.isEmpty();

		String periodo = String.format("durante %s %s, periodo comprendido entre el %s y el %s",
				detalle.getAnos() == 1 ? "el" : "los", detalle.getAnos_descripcion().toLowerCase(),
				Utility.DD_MM_YYYY.format(fechaInicial), Utility.DD_MM_YYYY.format(fechaRadicacion));
		String tipoDocDesc = "la cédula de ciudadanía";
		if (null != detalle.getTipo_docu()) {
			switch (detalle.getTipo_docu()) {
			case "NI":
				tipoDocDesc = "el NIT";
				break;
			case "CE":
				tipoDocDesc = "la cédula de extranjeria";
				break;
			case "PA":
				tipoDocDesc = "el pasaporte";
				break;
			default:
				break;
			}
		}
		tipoDocDesc = String.format("%s número %s ", tipoDocDesc, detalle.getNume_docu_descripcion());
		String part = "Una vez revisada la información disponible en el Sistema de Trámites y en las bases de datos ";
		if (tipoSancion == TipoSancion.ProteccionCompetencia) {
			part = "del Grupo para la Protección y Promoción de la Competencia, del Grupo de Prácticas Restrictivas de la Competencia y del Grupo Élite contra Colusiones, ";
			if (showDemandas || showMultas) {
				part += String.format(
						"se encontró que %s presenta las siguientes sanciones por infracciones del régimen de libre competencia económica %s:",
						tipoDocDesc, periodo);
			} else {
				part += String.format(
						"no se encontró ninguna actuación administrativa en la que se le haya impuesto sanción al %s por infracciones del régimen de libre competencia económica %s.",
						tipoDocDesc, periodo);
			}
		} else {
			part = String.format("%s de la entidad, %s", part, tipoDocDesc);
			if (showDemandas || showMultas) {
				part += "presenta la(s) siguiente(s) " + String.valueOf(sanciones.size());
			} else {
				part += "no presenta";
			}
			String temp = " demanda(s), investigacion(es) o sancion(es)";
			if (tipoSancion == TipoSancion.Multa) {
				temp = " sancion(es)";
			} else if (tipoSancion == TipoSancion.Demanda) {
				temp = " demanda(s) y/o investigacion(es)";
			}
			part += String.format("%s en la Superintendencia de Industria y Comercio, %s%s", temp, periodo,
					(showDemandas || showMultas) ? ":" : ".");
		}

		Context context = new Context();
		context.setVariable("texto_informativo", part);
		context.setVariable("showDemandas", showDemandas);
		context.setVariable("showMultas", showMultas);
		context.setVariable("demandas", demandas);
		context.setVariable("multas", multas);
		context.setVariable("dia", String.format("%s (%s)",
				Utility.convertNumberToLetter(fechaRadicacion.getDayOfMonth()), fechaRadicacion.getDayOfMonth()));
		context.setVariable("mes", Utility.MMMMM.format(fechaRadicacion));
		context.setVariable("anio", String.format("%s (%s)", Utility.convertNumberToLetter(fechaRadicacion.getYear()),
				fechaRadicacion.getYear()));
		context.setVariable("nombre_secretario", Constantes.WS_SIGN_NOMBRE_SECRETARIO_AD_HOC);
		context.setVariable("cargo_secretario", Constantes.WS_SIGN_CARGO_SECRETARIO_AD_HOC);
		context.setVariable("texto_legal", Constantes.TEXTO_LEGALIZACION_FIRMA_SECRETARIO_AD_HOC);
		log.info(String.format("co/gov/sic/bashcopiascertificaciones/resource/FIRMA_SECRETARIO_AD_HOC_%s.PNG",
				Constantes.WS_CANCILLERIA_ID_AUTORIDAD_SECRETARIO_AD_HOC));
		String imagenBase64 = getImageAsBase64(
				String.format("co/gov/sic/bashcopiascertificaciones/resource/FIRMA_SECRETARIO_AD_HOC_%s.PNG",
						Constantes.WS_CANCILLERIA_ID_AUTORIDAD_SECRETARIO_AD_HOC));
		context.setVariable("firma_base64", imagenBase64);
		return this.templateEngine.process(String.format("../templates/%s.%s", this.templateName, "PDF.html"), context);
	}

	private String getImageAsBase64(String resourcePath) {
		try {
			ClassLoader loader = TemplateContent.class.getClassLoader();
			InputStream inputStream = loader.getResourceAsStream(resourcePath);
			if (inputStream != null) {
				byte[] imageBytes = inputStream.readAllBytes();
				inputStream.close();
				String base64Image = Base64.getEncoder().encodeToString(imageBytes);
				log.info("Imagen cargada exitosamente: " + resourcePath + " (tamaño: " + imageBytes.length + " bytes)");
				return base64Image;
			} else {
				log.error("No se encontró la imagen en el classpath: " + resourcePath);
				return "";
			}
		} catch (IOException e) {
			log.error("Error al cargar imagen: " + resourcePath + " - " + e.getMessage(), e);
			return "";
		}
	}
}
