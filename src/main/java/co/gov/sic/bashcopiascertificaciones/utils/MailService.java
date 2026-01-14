package co.gov.sic.bashcopiascertificaciones.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.log4j.Logger;

import co.gov.sic.bashcopiascertificaciones.enums.TipoAmbienteEnum;
import sic.ws.interop.entities.Adjunto;
import sic.ws.interop.entities.radicacion.Radicacion;

public class MailService {

	private static final Logger logger = GetLogger.getInstance("MailService", "conf/log4j.properties");

	public static void Send(String to, String subject, String content, String fullPath) {
		List<String> files = new ArrayList<>();
		files.add(fullPath);
		Send(to, subject, content, files);
	}

	public static void Send(Radicacion radi, String subject, String content) {
		String to = radi.getRadicador().getEmails().get(0).getDescripcion();
		List<String> tos = new ArrayList<>();
		tos.add(to);
		Send(subject, content, tos, radi.getAdjuntos());
	}

	public static void Send(Radicacion radi, String subject, String content, List<String> filesPath) {
		String to = radi.getRadicador().getEmails().get(0).getDescripcion();
		List<String> tos = new ArrayList<>();
		tos.add(to);
		Send(tos, subject, content, filesPath);
	}

	public static void Send(String to, String subject, String content) {
		List<String> tos = new ArrayList<>();
		tos.add(to);
		Send(tos, subject, content, null);
	}

	public static void Send(String to, String subject, String content, List<String> filesPath) {
		List<String> tos = new ArrayList<>();
		tos.add(to);
		Send(tos, subject, content, filesPath);
	}

	public static void Send(String subject, String content, List<String> to, List<Adjunto> files) {
		List<String> filesPath = null;
		if (files != null && files.size() > 0) {
			filesPath = new ArrayList<>();
			for (Adjunto adj : files) {
				filesPath.add(adj.getPathToRead());
			}
		}
		Send(to, subject, content, filesPath);
	}

	public static void Send(List<String> to, String subject, String content, List<String> filesPath) {

		if (Constantes.SEND_MAIL_ENABLE) {
			// Get system properties
			Properties properties = System.getProperties();

			// Setup mail server
			properties.setProperty("mail.smtp.host", Constantes.MAIL_HOST);

			// Get the default Session object.
			Session session = Session.getDefaultInstance(properties);

			try {
				System.setProperty("java.net.preferIPv4Stack", "true");
				MimeMessage message = new MimeMessage(session);
				message.setFrom(new InternetAddress(Constantes.MAIL_FROM));
				for (int index = 0; index < to.size(); index++) {
					message.addRecipient(Message.RecipientType.TO, new InternetAddress(to.get(index)));
				}

				String nombreAmbiente = Constantes.STR_EMPTY;
				if (Constantes.AMBIENTE_ACTIVO != TipoAmbienteEnum.PRODUCCION) {
					nombreAmbiente = Constantes.AMBIENTE_ACTIVO.toString() + ": ";
				}

				message.setSubject(nombreAmbiente + subject);

				// Create the message part
				BodyPart messageBodyPart = new MimeBodyPart();

				// Now set the actual message
				messageBodyPart.setContent(content, Constantes.CONTENT_TYPE_HTML);

				// Create a multipar message
				Multipart multipart = new MimeMultipart();

				// Set text message part
				multipart.addBodyPart(messageBodyPart);

				// Part two is attachment
				if (filesPath != null && filesPath.size() > 0) {
					for (String filePath : filesPath) {
						if (!Utility.isNullOrEmptyTrim(filePath)) {
							File file = new File(filePath);
							if (file.exists()) {
								messageBodyPart = new MimeBodyPart();
								DataSource source = new FileDataSource(filePath);
								messageBodyPart.setDataHandler(new DataHandler(source));
								messageBodyPart.setFileName(file.getName());
								multipart.addBodyPart(messageBodyPart);
							}
						}
					}
				}
				// Send the complete message parts
				message.setContent(multipart);

				// Send message
				Transport.send(message);
			} catch (MessagingException mex) {
				logger.error(mex);
			}
		}
	}
}
