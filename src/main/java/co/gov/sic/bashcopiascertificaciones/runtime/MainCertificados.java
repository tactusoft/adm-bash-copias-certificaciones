package co.gov.sic.bashcopiascertificaciones.runtime;

import java.sql.SQLException;

import co.gov.sic.bashcopiascertificaciones.database.DataBaseConfig;
import co.gov.sic.bashcopiascertificaciones.database.DataBaseConnection;
import co.gov.sic.bashcopiascertificaciones.database.DataBaseConnectionCertificados;
import co.gov.sic.bashcopiascertificaciones.procesos.ProcessManagerCertificados;
import co.gov.sic.bashcopiascertificaciones.utils.Constantes;

public class MainCertificados {

	public static void main(String[] args) {
		try {
			new Constantes();
			DataBaseConfig.getInstance();
			DataBaseConnectionCertificados.getInstance();
			ProcessManagerCertificados pMng = new ProcessManagerCertificados();
			pMng.pending3();
		} catch (Exception e) {
			return;
		}

		try {
			DataBaseConnection.getInstance().CloseConnxtion();
		} catch (SQLException ex) {
		}

	}

}
