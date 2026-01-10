/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.gov.sic.bashcopiascertificaciones.database;

import co.gov.sic.bashcopiascertificaciones.entities.Cesl_config;
import co.gov.sic.bashcopiascertificaciones.entities.Cesl_tramite;
import co.gov.sic.bashcopiascertificaciones.utils.GetLogger;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author elmos
 */
public class DataBaseConnection {

	private static DataBaseConnection cnx;
	private DataBaseConfig dbCfg;
	private final Logger log = GetLogger.getInstance("log4j.properties");
	private Connection dbConnection;

	private static final String SQL_REQUEST_BY_PERSON = "SELECT distinct tramite.idtramite,tramite.ano_radi,tramite.nume_radi,tramite.cont_radi,tramite.cons_radi"
			+ ",tramite.estado,tramite.valor_total,tramite.fecha_creacion,tramite.fecha_modificacion"
			+ ",tramite.medio_respuesta,tramite.idtiposolicitud,tramite.iden_pers"
			+ ",tramite.ano_recibo,tramite.num_recibo,tramite.func_asignado"
			+ ",busc_pers.nomb_pers,persona.tipo_docu,persona.nume_docu," + "(select r.codi_dede "
			+ "from radicacion r " + "inner join ( " + "    select t.ano_radi, t.nume_radi, max(t.cons_radi) cons_radi "
			+ "	from radicacion t " + "	where t.ano_radi = tramite.ano_radi "
			+ "	and t.nume_radi = tramite.nume_radi " + "	and t.codi_depe = 104 " + "	and t.codi_tram = 362 "
			+ "	and t.codi_even = 0 " + "	and t.codi_actu = 431 " + "	group by t.ano_radi, t.nume_radi "
			+ ") rd on rd.ano_radi = r.ano_radi and rd.nume_radi = r.nume_radi and rd.cons_radi = r.cons_radi) codi_dede "
			+ "FROM cesl_tramite tramite "
			+ "inner join busc_pers busc_pers on (tramite.iden_pers = busc_pers.iden_pers) "
			+ "inner join persona persona on (persona.iden_pers = tramite.iden_pers) "
			+ "where  tramite.idtiposolicitud=5 and tramite.estado=?" + "order by idtramite asc";

	private static final String SQL_REQUEST_BY_DESISTIMIENTOS = "SELECT distinct tramite.idtramite,tramite.ano_radi,tramite.nume_radi,tramite.cont_radi,tramite.cons_radi"
			+ ",tramite.estado,tramite.valor_total,tramite.fecha_creacion,tramite.fecha_modificacion"
			+ ",tramite.medio_respuesta,tramite.idtiposolicitud,tramite.iden_pers"
			+ ",tramite.ano_recibo,tramite.num_recibo,tramite.func_asignado"
			+ ",busc_pers.nomb_pers,persona.tipo_docu,persona.nume_docu," + "(select r.codi_dede "
			+ "from radicacion r " + "inner join ( " + "    select t.ano_radi, t.nume_radi, max(t.cons_radi) cons_radi "
			+ "	from radicacion t " + "	where t.ano_radi = tramite.ano_radi "
			+ "	and t.nume_radi = tramite.nume_radi " + "	and t.codi_depe = 104 " + "	and t.codi_tram = 362 "
			+ "	and t.codi_even = 0 " + "	and t.codi_actu = 431 " + "	group by t.ano_radi, t.nume_radi "
			+ ") rd on rd.ano_radi = r.ano_radi and rd.nume_radi = r.nume_radi and rd.cons_radi = r.cons_radi) codi_dede "
			+ "FROM cesl_tramite tramite "
			+ "inner join busc_pers busc_pers on (tramite.iden_pers = busc_pers.iden_pers) "
			+ "inner join persona persona on (persona.iden_pers = tramite.iden_pers) "
			+ "where  tramite.idtiposolicitud=5 and tramite.estado in (15,23)" + "order by idtramite asc";

	private static final String SQL_REQUEST_BY_REGISTERED = "SELECT t.ano_radi, t.nume_radi FROM radicacion t "
			+ "WHERE t.codi_depe = 104 and t.codi_tram = 362 and t.codi_even = 0 and t.codi_actu = 451 "
			+ "and t.ano_radi = ? and t.nume_radi = ? and t.codi_deor = ?";

	private static final String SQL_REQUEST_BY_INTERN_REQUERIMENTS = "SELECT t.ano_radi, t.nume_radi, t.cons_radi FROM radicacion t "
			+ "WHERE t.codi_depe = 104 AND t.codi_tram = 362 AND t.codi_even = 0 AND t.codi_actu = 431 "
			+ "AND t.ano_radi = ? AND t.nume_radi = ? AND t.codi_dede = ? AND NOT EXISTS ("
			+ "    SELECT 1 FROM radicacion r WHERE r.ano_radi = t.ano_radi AND r.nume_radi = t.nume_radi "
			+ "    AND r.codi_actu = 451 AND r.codi_deor = t.codi_dede AND r.cons_radi > t.cons_radi) "
			+ "ORDER BY t.cons_radi";

	private static final String SQL_GET_ALL_PARAMETERS = "SELECT llave" + ",valor " + "FROM cesl_config where llave=?";

	private static final String SQL_UPDATE_ESTADO = "UPDATE CESL_TRAMITE SET estado=?,fecha_modificacion=current WHERE idtramite=?";

	private DataBaseConnection() {
		dbCfg = DataBaseConfig.getInstance();
		log.info("Estableciendo conexión con la base de datos");

		try {
			DriverManager.registerDriver((Driver) Class.forName(DataBaseConfig.getDriver()).newInstance());
			log.debug("Driver = " + DataBaseConfig.getDriver() + " ... Registrado Correctamente");

			dbConnection = DriverManager.getConnection(DataBaseConfig.getURL(), DataBaseConfig.getUsername(),
					DataBaseConfig.getPassword());
			log.info("Conexión con la base de datos establecida correctamente");

		} catch (SQLException | ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
			log.error(ex.toString());
		}

	}

	public static DataBaseConnection getInstance() {
		if (cnx == null) {
			cnx = new DataBaseConnection();
		}
		return cnx;
	}

	public void CloseConnxtion() throws SQLException {
		try {
			if (cnx != null && !dbConnection.isClosed()) {
				dbConnection.close();
				dbConnection = null;
			}

		} catch (Exception e) {
			log.error("Error al cerrar la conexion a base de datos, ya esta cerrada o no se ha establecido ninguna ");
			log.debug(e.toString());
		}

	}

	public List<Cesl_tramite> getRequestPendingComplement(int estado) {
		List<Cesl_tramite> response = new ArrayList<>();
		try {
			PreparedStatement stmt = dbConnection.prepareStatement(SQL_REQUEST_BY_PERSON);
			stmt.setInt(1, estado);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				Cesl_tramite tramite = new Cesl_tramite();
				tramite.setIdtramite(rs.getInt("idtramite"));
				tramite.setAno_radi(rs.getShort("ano_radi"));
				tramite.setNume_radi(rs.getInt("nume_radi"));
				tramite.setFunc_asignado(rs.getLong("func_asignado"));
				tramite.setIden_pers(rs.getLong("iden_pers"));
				Timestamp fechaModi = rs.getTimestamp("fecha_modificacion");
				if (fechaModi != null) {
					tramite.setFecha_modificacion(fechaModi.toLocalDateTime());
				}
				Timestamp fechaCreacion = rs.getTimestamp("fecha_creacion");
				if (fechaCreacion != null) {
					tramite.setFecha_creacion(fechaCreacion.toLocalDateTime());
				}
				tramite.setNumeroIdentificacion(rs.getString("nume_docu"));
				tramite.setNombreSolicitante(rs.getString("nomb_pers").trim());
				tramite.setIdtiposolicitud(rs.getInt("idtiposolicitud"));
				tramite.setCodigoDependenciaDestino(rs.getInt("codi_dede"));
				response.add(tramite);
			}

		} catch (SQLException e) {
			log.error(e.toString());
		}
		return response;
	}

	public List<Cesl_tramite> getRequestDesistimientos() {
		List<Cesl_tramite> response = new ArrayList<>();
		try {
			PreparedStatement stmt = dbConnection.prepareStatement(SQL_REQUEST_BY_DESISTIMIENTOS);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				Cesl_tramite tramite = new Cesl_tramite();
				tramite.setIdtramite(rs.getInt("idtramite"));
				tramite.setAno_radi(rs.getShort("ano_radi"));
				tramite.setNume_radi(rs.getInt("nume_radi"));
				tramite.setFunc_asignado(rs.getLong("func_asignado"));
				tramite.setIden_pers(rs.getLong("iden_pers"));
				Timestamp fechaModi = rs.getTimestamp("fecha_modificacion");
				if (fechaModi != null) {
					tramite.setFecha_modificacion(fechaModi.toLocalDateTime());
				}
				Timestamp fechaCreacion = rs.getTimestamp("fecha_creacion");
				if (fechaCreacion != null) {
					tramite.setFecha_creacion(fechaCreacion.toLocalDateTime());
				}
				tramite.setNumeroIdentificacion(rs.getString("nume_docu"));
				tramite.setNombreSolicitante(rs.getString("nomb_pers").trim());
				tramite.setIdtiposolicitud(rs.getInt("idtiposolicitud"));
				tramite.setCodigoDependenciaDestino(rs.getInt("codi_dede"));
				response.add(tramite);
			}

		} catch (SQLException e) {
			log.error(e.toString());
		}
		return response;

	}

	public List<Cesl_tramite> getRequestPendingComplement(Short anio, Integer radi, Integer codiDede) {
		List<Cesl_tramite> response = new ArrayList<>();

		try {

			PreparedStatement stmt = dbConnection.prepareStatement(SQL_REQUEST_BY_REGISTERED);
			stmt.setShort(1, anio);
			stmt.setInt(2, radi);
			stmt.setLong(3, codiDede);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				Cesl_tramite tramite = new Cesl_tramite();
				tramite.setAno_radi(rs.getShort("ano_radi"));
				tramite.setNume_radi(rs.getInt("nume_radi"));
				response.add(tramite);
			}

		} catch (SQLException e) {
			log.error(e.toString());
		}
		return response;

	}

	public List<Cesl_tramite> getInterRequeriment(Short anio, Integer radi, Integer codiDede) {
		List<Cesl_tramite> response = new ArrayList<>();
		try {
			PreparedStatement stmt = dbConnection.prepareStatement(SQL_REQUEST_BY_INTERN_REQUERIMENTS);
			stmt.setShort(1, anio);
			stmt.setInt(2, radi);
			stmt.setLong(3, codiDede);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				Cesl_tramite tramite = new Cesl_tramite();
				tramite.setAno_radi(rs.getShort("ano_radi"));
				tramite.setNume_radi(rs.getInt("nume_radi"));
				tramite.setCons_radi(rs.getInt("cons_radi"));
				response.add(tramite);
			}

		} catch (SQLException e) {
			log.error(e.toString());
		}
		return response;

	}

	public Cesl_config getDayConfigParameters(String llave) {
		Cesl_config cfg = new Cesl_config();

		try {
			PreparedStatement stmt = dbConnection.prepareStatement(SQL_GET_ALL_PARAMETERS);
			stmt.setString(1, llave);
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					Cesl_config conf = new Cesl_config();
					conf.setLlave(rs.getString("llave").trim());
					if (rs.getString("Valor").contains("_H") || rs.getString("Valor").contains("_C")) {
						conf.setValor(
								rs.getString("Valor").trim().substring(0, rs.getString("Valor").trim().indexOf("_")));
						String businessDays = rs.getString("Valor").trim().substring(
								rs.getString("Valor").trim().indexOf("_"), rs.getString("Valor").trim().length());
						if (businessDays.equals("_H")) {
							conf.setBusinessDays(true);
						} else {
							conf.setBusinessDays(false);
						}
					} else {
						conf.setBusinessDays(false);
						conf.setValor(rs.getString("Valor").trim());

					}

					cfg = conf;
				}
			}
		} catch (SQLException ex) {
			log.error(ex.toString());
		} catch (Exception ex) {
			log.error(ex.toString());
		}
		return cfg;
	}

	public boolean setCeslTramiteEstado(int idtramite, int estado) {

		try {

			PreparedStatement stmt = dbConnection.prepareStatement(SQL_UPDATE_ESTADO);
			stmt.setInt(1, estado);
			stmt.setInt(2, idtramite);
			int rta = stmt.executeUpdate();
			return rta > 0;

		} catch (SQLException ex) {
			log.error(ex.toString());
			return false;
		} catch (Exception ex) {
			log.error(ex.toString());
			return false;
		}

	}

}
