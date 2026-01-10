package co.gov.sic.bashcopiascertificaciones.runtime;

import java.io.IOException;
import java.sql.SQLException;

import com.itextpdf.text.BadElementException;

import co.gov.sic.bashcopiascertificaciones.database.DataBaseConfig;
import co.gov.sic.bashcopiascertificaciones.database.DataBaseConnection;
import co.gov.sic.bashcopiascertificaciones.database.DataBaseConnectionCertificados;
import co.gov.sic.bashcopiascertificaciones.enums.EstadoTramite;
import co.gov.sic.bashcopiascertificaciones.procesos.ProcessManagerCertificados;
import co.gov.sic.bashcopiascertificaciones.utils.Constantes;

public class MainCertificados {

	public static void main(String[] args) {
		try {
			new Constantes();
			DataBaseConfig.getInstance();
			DataBaseConnectionCertificados.getInstance();
			ProcessManagerCertificados pMng = new ProcessManagerCertificados();
			pMng.pending();
		} catch (BadElementException | IOException | NullPointerException e) {
			return;
		}

		try {
			DataBaseConnection.getInstance().CloseConnxtion();
		} catch (SQLException ex) {
		}

	}

}
