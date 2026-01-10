/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.gov.sic.bashcopiascertificaciones.runtime;

import java.io.IOException;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.itextpdf.text.BadElementException;

import co.gov.sic.bashcopiascertificaciones.database.DataBaseConfig;
import co.gov.sic.bashcopiascertificaciones.database.DataBaseConnection;
import co.gov.sic.bashcopiascertificaciones.enums.EstadoTramite;
import co.gov.sic.bashcopiascertificaciones.procesos.ProcessManager;
import co.gov.sic.bashcopiascertificaciones.utils.Constantes;
import co.gov.sic.bashcopiascertificaciones.utils.GetLogger;

/**
 *
 * @author csarmiento
 */
public class Main {

	/**
	 * @param args the command line arguments
	 * @throws com.itextpdf.text.BadElementException
	 * @throws java.io.IOException
	 */
	public static void main(String[] args) throws BadElementException, IOException {
		Logger log = GetLogger.getInstance("Main", "conf/log4j.properties");
		try {
			new Constantes();
			DataBaseConfig.getInstance();
			DataBaseConnection.getInstance();
			if (args.length > 0) {
				log.info("Cambio de parametros");
				DataBaseConfig.getInstance();
				for (String s : args) {
					DataBaseConfig.saveParameterConfig(s);
				}
				log.info("Finalizando ejecucion");
			} else {
				log.info("Inicio Ejecución proceso batch Copias y certificaciones. Versión 2024-12-04.");
				ProcessManager pMng = new ProcessManager();
				pMng.desistirSolicitudes();
				pMng.respuestaRequerimientoInterno(EstadoTramite.SOL_INFO_AREA_INTERNA.getValue());
				pMng.requerimientoInterno(EstadoTramite.SOL_INFO_AREA_INTERNA.getValue());
			}
		} catch (BadElementException | IOException | NullPointerException e) {
			log.error("El archivo de configuracion de base de datos esta vacio o no existe " + e.toString());
			return;
		}

		try {
			DataBaseConnection.getInstance().CloseConnxtion();
		} catch (SQLException ex) {
			log.error(ex.toString());
		}
	}

}
