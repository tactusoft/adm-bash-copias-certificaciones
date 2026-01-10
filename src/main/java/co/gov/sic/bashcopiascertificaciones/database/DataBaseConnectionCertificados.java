/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.gov.sic.bashcopiascertificaciones.database;

import java.sql.Connection;
import java.sql.Date;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import co.gov.sic.bashcopiascertificaciones.entities.Cesl_detalleSolicitud;
import co.gov.sic.bashcopiascertificaciones.entities.Cesl_tramite;
import co.gov.sic.bashcopiascertificaciones.entities.Sancion;
import co.gov.sic.bashcopiascertificaciones.enums.TipoSancion;
import co.gov.sic.bashcopiascertificaciones.utils.GetLogger;

/**
 *
 * @author elmos
 */
public class DataBaseConnectionCertificados {

	private static DataBaseConnectionCertificados cnx;
	private DataBaseConfig dbCfg;
	private final Logger log = GetLogger.getInstance("log4j.properties");
	private Connection dbConnection;

	private static final String SQL_REQUEST_PENDING = "SELECT idtramite, idtiposolicitud, ano_radi, nume_radi, cont_radi, cons_radi, estado, valor_total, "
			+ "fecha_creacion, fecha_modificacion, medio_respuesta, iden_pers, ano_recibo, num_recibo, func_asignado "
			+ "FROM cesl_tramite WHERE idtiposolicitud = 1 AND (estado = 6 or estado = 9) AND idtramite = 20667"
			+ "ORDER BY fecha_creacion";

	private static final String SQL_REQUEST_DETAIL = "select idtramite, tipo_docu, nume_docu, cantidad, anos, tipo_certifica "
			+ "from cesl_detallesolicitud " + "where idtramite = ?";

	private static final String SQL_UPDATE_ESTADO = "UPDATE CESL_TRAMITE SET estado=?,fecha_modificacion=current WHERE idtramite=?";

	private static final String SQL_SELECT_SANCIONES_1 = "SELECT m.fech_acto, m.tipo_acto, m.nume_acto, r.ano_radi, r.nume_radi, EXISTS (SELECT 'x' FROM pers_acto a "
			+ " WHERE  a.tipo_acto = m.tipo_acto AND a.nume_acto = m.nume_acto "
			+ " AND    a.fech_acto = m.fech_acto AND a.iden_pers = m.iden_pers "
			+ " AND    a.noti_indi is not null  AND a.ejec_indi is not null "
			+ " AND    a.fech_cons is not null AND a.cons_ejec = 'CE') AND NOT EXISTS (SELECT 'x' "
			+ " FROM   move_mult v WHERE v.cons_mult = m.cons_mult AND v.tipo_movi in ('MM','RV', 'NU') "
			+ " AND    v.valo_movi = m.valo_mult) as firmeza FROM acto_radi r, multa m, persona p"
			+ " WHERE  r.tipo_acto = m.tipo_acto AND r.nume_acto = m.nume_acto"
			+ " AND    r.fech_acto = m.fech_acto AND m.iden_pers = p.iden_pers"
			+ " AND    m.esta_mult in ('AC','FN') AND m.fech_acto >= '1998-01-01'"
			+ " AND    m.cons_mult >= 2925 AND p.tipo_docu = ? AND p.nume_docu = ?"
			+ " AND    m.fech_acto BETWEEN ? AND ? AND m.cons_mult NOT IN (SELECT c.cons_mult FROM cont_mult c) AND NOT EXISTS (SELECT 'x' FROM   move_mult v  WHERE  v.cons_mult = m.cons_mult AND v.tipo_movi in ('MM','RV', 'NU'))";

	private static final String SQL_SELECT_SANCIONES_3 = "SELECT m.fech_acto, m.tipo_acto, m.nume_acto, r.ano_radi, r.nume_radi, EXISTS (SELECT 'x' FROM pers_acto a"
			+ "			WHERE  a.tipo_acto = m.tipo_acto AND a.nume_acto = m.nume_acto"
			+ "			AND    a.fech_acto = m.fech_acto AND a.iden_pers = m.iden_pers"
			+ "			AND    a.noti_indi is not null  AND a.ejec_indi is not null"
			+ "			AND    a.fech_cons is not null AND a.cons_ejec = 'CE') AND NOT EXISTS (SELECT 'x'"
			+ "			FROM   move_mult v WHERE  v.cons_mult = m.cons_mult AND v.tipo_movi in ('MM','RV', 'NU')"
			+ "			AND    v.valo_movi = m.valo_mult) as firmeza FROM acto_radi r"
			+ "			INNER JOIN multa m ON r.tipo_acto = m.tipo_acto AND r.nume_acto = m.nume_acto AND r.fech_acto = m.fech_acto"
			+ "			INNER JOIN persona p ON m.iden_pers = p.iden_pers"
			+ "			INNER JOIN acto_subc a3 on a3.tipo_acto = r.tipo_acto and a3.nume_acto = r.nume_acto and r.fech_acto = a3.fech_acto"
			+ "			INNER JOIN radicacion r2 on r.ano_radi = r2.ano_radi and r.nume_radi = r2.nume_radi and r.cont_radi = r2.cont_radi and r.cons_radi = r2.cons_radi"
			+ "			WHERE  m.esta_mult in ('AC','FN') AND m.fech_acto >= '1998-01-01'"
			+ "			AND    m.cons_mult >= 2925 AND p.tipo_docu = ? AND p.nume_docu = ?"
			+ "			AND    m.fech_acto BETWEEN ? AND ? AND ("
			+ "				(a3.codi_clas = 5 AND a3.codi_subc in (54,24,28,64)) OR"
			+ "				(a3.codi_clas = 18 AND a3.codi_subc = 39) OR"
			+ "				(r2.codi_tram IN (355, 114, 304) AND ((a3.codi_clas = 18 AND a3.codi_subc = 40) OR a3.codi_clas = 12 AND a3.codi_subc IN (34,2))) OR"
			+ "				(a3.codi_clas = 12 AND a3.codi_subc = 30) OR (a3.codi_clas in (64, 1) AND a3.codi_subc = 1)"
			+ "			)"
			+ "			AND m.cons_mult NOT IN (SELECT c.cons_mult FROM cont_mult c) AND NOT EXISTS (SELECT 'x' FROM   move_mult v  WHERE  v.cons_mult = m.cons_mult AND v.tipo_movi in ('MM','RV', 'NU'))";

	private static final String SQL_SELECT_SANCIONES_2 = "select r.ano_radi, r.nume_Radi from partes pa "
			+ " inner join radicacion r on pa.ano_radi = r.ano_radi and pa.nume_radi = r.nume_radi and pa.cont_radi = r.cont_radi"
			+ " inner join acto_radi a2 on a2.ano_radi = r.ano_radi and a2.nume_radi = r.nume_radi and a2.cont_radi = r.cont_radi and a2.cons_radi = r.cons_radi"
			+ " inner join persona p on p.iden_pers = pa.iden_pers"
			+ " inner join acto_subc a3 on a3.tipo_acto = a2.tipo_acto and a3.nume_acto = a2.nume_acto and a3.fech_acto = a2.fech_acto"
			+ " where p.tipo_docu = ? AND p.nume_docu = ? AND pa.esta_regi = 'AC' AND pa.rol = 'IN' AND ("
			+ "	(a3.codi_clas = 5 AND (a3.codi_subc in (24,28,64) OR (a3.codi_subc = 54 and r.codi_tram in (400,385)))) OR"
			+ "	(a3.codi_clas = 18 AND a3.codi_subc in(39,40)) OR"
			+ "	(a3.codi_clas = 12 AND a3.codi_subc in(2,30,34)) OR	(a3.codi_clas in (64, 1) AND a3.codi_subc = 1) OR"
			+ "	(r.codi_tram in (228,328,383,391,350,351,342,187,381,356,367) AND r.codi_Actu in (460,653,706,707,500)) OR"
			+ "	(r.codi_tram = 384 AND r.codi_even in (328,330) AND r.codi_Actu in (460,653,706,707)) OR"
			+ "	(r.codi_tram in (388,389,390,105,414) AND r.codi_even in (356,357,358,327,325,328) AND r.codi_Actu in (460,653,706,707,500))"
			+ " ) AND a2.fech_acto BETWEEN ? AND ? group by r.ano_radi, r.nume_Radi";

	private static final String SQL_SELECT_SANCIONES_4 = "select r.ano_radi, r.nume_Radi from pers_acto pa "
			+ "  inner join acto_radi a2 on a2.tipo_acto = pa.tipo_acto and a2.nume_acto = pa.nume_acto and a2.fech_acto = pa.fech_acto"
			+ "	 inner join radicacion r on a2.ano_radi = r.ano_radi and a2.nume_radi = r.nume_radi and a2.cont_radi = r.cont_radi and a2.cons_radi = r.cons_radi "
			+ " inner join persona p on p.iden_pers = pa.iden_pers"
			+ " inner join acto_subc a3 on a3.tipo_acto = a2.tipo_acto and a3.nume_acto = a2.nume_acto and a3.fech_acto = a2.fech_acto"
			+ " where p.tipo_docu = ? AND p.nume_docu = ? AND  pa.tipo_vinc = 'IN' AND ("
			+ "	(a3.codi_clas = 5 AND (a3.codi_subc in (24,28,64) OR (a3.codi_subc = 54 and r.codi_tram in (400,385)))) OR"
			+ "	(a3.codi_clas = 18 AND a3.codi_subc in(39,40)) OR"
			+ "	(a3.codi_clas = 12 AND a3.codi_subc in(2,30,34)) OR (a3.codi_clas in (64, 1) AND a3.codi_subc = 1) OR"
			+ "	(r.codi_tram in (228,328,383,391,350,351,342,187,381,356,367) AND r.codi_Actu in (460,653,706,707,500)) OR"
			+ "	(r.codi_tram = 384 AND r.codi_even in (328,330) AND r.codi_Actu in (460,653,706,707)) OR"
			+ "	(r.codi_tram in (388,389,390,105,414) AND r.codi_even in (356,357,358,327,325,328) AND r.codi_Actu in (460,653,706,707,500))"
			+ " ) AND a2.fech_acto BETWEEN ? AND ? group by r.ano_radi, r.nume_Radi";

	private static final String SQL_SELECT_SANCIONES_5 = "select r.ano_radi, r.nume_Radi from tgpersona pa "
			+ " inner join radicacion r on pa.pitranor = r.ano_radi and pa.pitrnura = r.nume_radi and pa.control = r.cont_radi"
			+ " inner join acto_radi a2 on a2.ano_radi = r.ano_radi and a2.nume_radi = r.nume_radi and a2.cont_radi = r.cont_radi and a2.cons_radi = r.cons_radi"
			+ " inner join persona p on p.iden_pers = pa.ide_per"
			+ " inner join acto_subc a3 on a3.tipo_acto = a2.tipo_acto and a3.nume_acto = a2.nume_acto and a3.fech_acto = a2.fech_acto"
			+ " where p.tipo_docu = ? AND p.nume_docu = ? and pa.rol = 'IN' AND ("
			+ "	(a3.codi_clas = 5 AND (a3.codi_subc in (24,28,64) OR (a3.codi_subc = 54 and r.codi_tram in (400,385)))) OR"
			+ "	(a3.codi_clas = 18 AND a3.codi_subc in(39,40)) OR"
			+ "	(a3.codi_clas = 12 AND a3.codi_subc in(2,30,34)) OR	(a3.codi_clas in (64, 1) AND a3.codi_subc = 1) OR"
			+ "	(r.codi_tram in (228,328,383,391,350,351,342,187,381,356,367) AND r.codi_Actu in (460,653,706,707,500)) OR"
			+ "	(r.codi_tram = 384 AND r.codi_even in (328,330) AND r.codi_Actu in (460,653,706,707)) OR"
			+ "	(r.codi_tram in (388,389,390,105,414) AND r.codi_even in (356,357,358,327,325,328) AND r.codi_Actu in (460,653,706,707,500))"
			+ " ) AND a2.fech_acto BETWEEN ? AND ? group by r.ano_radi, r.nume_Radi";

	private DataBaseConnectionCertificados() {
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

	public static DataBaseConnectionCertificados getInstance() {
		if (cnx == null) {
			cnx = new DataBaseConnectionCertificados();
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

	public List<Cesl_tramite> getRequestPending() {
		List<Cesl_tramite> response = new ArrayList<>();
		try {
			PreparedStatement stmt = dbConnection.prepareStatement(SQL_REQUEST_PENDING);
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
				tramite.setIdtiposolicitud(rs.getInt("idtiposolicitud"));
				response.add(tramite);
			}

		} catch (SQLException e) {
			log.error(e.toString());
		}
		return response;

	}

	public Cesl_detalleSolicitud getRequestDetail(Integer idtramite) {
		try {
			PreparedStatement stmt = dbConnection.prepareStatement(SQL_REQUEST_DETAIL);
			stmt.setLong(1, idtramite);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				Cesl_detalleSolicitud detalle = new Cesl_detalleSolicitud();
				detalle.setIdtramite(rs.getInt("idtramite"));
				detalle.setTipo_docu(rs.getString("tipo_docu"));
				detalle.setNume_docu(rs.getLong("nume_docu"));
				detalle.setCantidad(rs.getInt("cantidad"));
				detalle.setAnos(rs.getInt("anos"));
				if (detalle.getAnos() != null) {
					detalle.setAnos_descripcion(detalle.getAnos() == 1 ? "Último año"
							: String.format("Últimos %s años", detalle.getAnos()));
				}
				detalle.setTipo_certifica(rs.getString("tipo_certifica"));
				return detalle;
			}
		} catch (SQLException e) {
			log.error(e.toString());
		}
		return null;
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

	public List<Sancion> consultarSanciones(String tipoDocumento, long numeroDocumento, TipoSancion tipoSancion,
			LocalDateTime fechaInicial, LocalDateTime fechaFinal) throws Exception {
		List<Sancion> response = new ArrayList<>();
		List<String> documentos = new ArrayList<>();
		documentos.add(String.valueOf(numeroDocumento));
		// Si la busqueda es un NIT, se hace la busqueda del numero de documento con la
		// combinacion de todos los posibles digitos de verificacion
		// ya que existen la posibilidad de que el nit se haya registrado con el digito
		// de verificacion pegado y ademas que este no sea el correcto.
		// El numero de documento para un NIT siempre tiene 9 disgitos + el digito de
		// verificacion
		if ("NI".equals(tipoDocumento)) {
			for (int digitoVerificacion = 0; digitoVerificacion <= 9; digitoVerificacion++) {
				documentos.add(String.format("%s%s", numeroDocumento, digitoVerificacion));
			}
		}
		Date fechaInicialSql = Date.valueOf(fechaInicial.toLocalDate());
		Date fechaFinalSql = Date.valueOf(fechaFinal.toLocalDate());
		for (String numDoc : documentos) {
			if (tipoSancion == null || tipoSancion == TipoSancion.Multa) {
				try (PreparedStatement stmt = dbConnection.prepareStatement(SQL_SELECT_SANCIONES_1)) {
					stmt.setString(1, tipoDocumento);
					stmt.setString(2, numDoc);
					stmt.setDate(3, fechaInicialSql);
					stmt.setDate(4, fechaFinalSql);

					try (ResultSet rs = stmt.executeQuery()) {
						while (rs.next()) {
							Sancion s = new Sancion();
							s.setTipo(TipoSancion.Multa);
							s.setFechaActo(rs.getDate("fech_acto"));
							s.setNumeroActo(rs.getInt("nume_acto"));
							s.setAnoRadicacion(rs.getShort("ano_radi"));
							s.setNumeroRadicacion(rs.getInt("nume_radi"));
							s.setTipoActo(rs.getString("tipo_acto"));
							s.setFirmeza(rs.getBoolean("firmeza"));
							if (!response.contains(s)) {
								response.add(s);
							}
						}
					}
				}
			}
			if (tipoSancion == null || tipoSancion == TipoSancion.Demanda) {
				try (PreparedStatement stmt = dbConnection.prepareStatement(SQL_SELECT_SANCIONES_2)) {
					stmt.setString(1, tipoDocumento);
					stmt.setString(2, numDoc);
					stmt.setDate(3, fechaInicialSql);
					stmt.setDate(4, fechaFinalSql);
					try (ResultSet rs = stmt.executeQuery()) {
						while (rs.next()) {
							Sancion s = new Sancion();
							s.setTipo(TipoSancion.Demanda);
							s.setAnoRadicacion(rs.getShort("ano_radi"));
							s.setNumeroRadicacion(rs.getInt("nume_radi"));
							if (!response.contains(s)) {
								// logger.info(String.format("Q2 %s - %s", s.getAnoRadicacion(),
								// s.getNumeroRadicacion()));
								response.add(s);
							}
						}

					}
				}
				try (PreparedStatement stmt = dbConnection.prepareStatement(SQL_SELECT_SANCIONES_4)) {
					stmt.setString(1, tipoDocumento);
					stmt.setString(2, numDoc);
					stmt.setDate(3, fechaInicialSql);
					stmt.setDate(4, fechaFinalSql);
					try (ResultSet rs = stmt.executeQuery()) {
						while (rs.next()) {
							Sancion s = new Sancion();
							s.setTipo(TipoSancion.Demanda);
							s.setAnoRadicacion(rs.getShort("ano_radi"));
							s.setNumeroRadicacion(rs.getInt("nume_radi"));
							if (!response.contains(s)) {
								// logger.info(String.format("Q4 %s - %s", s.getAnoRadicacion(),
								// s.getNumeroRadicacion()));
								response.add(s);
							}
						}
					}
				}
				try (PreparedStatement stmt = dbConnection.prepareStatement(SQL_SELECT_SANCIONES_5)) {
					stmt.setString(1, tipoDocumento);
					stmt.setString(2, numDoc);
					stmt.setDate(3, fechaInicialSql);
					stmt.setDate(4, fechaFinalSql);
					try (ResultSet rs = stmt.executeQuery()) {
						while (rs.next()) {
							Sancion s = new Sancion();
							s.setTipo(TipoSancion.Demanda);
							s.setAnoRadicacion(rs.getShort("ano_radi"));
							s.setNumeroRadicacion(rs.getInt("nume_radi"));
							if (!response.contains(s)) {
								// logger.info(String.format("Q5 %s - %s", s.getAnoRadicacion(),
								// s.getNumeroRadicacion()));
								response.add(s);
							}
						}
					}
				}
			}
			if (tipoSancion == TipoSancion.ProteccionCompetencia) {
				try (PreparedStatement stmt = dbConnection.prepareStatement(SQL_SELECT_SANCIONES_3)) {
					stmt.setString(1, tipoDocumento);
					stmt.setString(2, numDoc);
					stmt.setDate(3, fechaInicialSql);
					stmt.setDate(4, fechaFinalSql);
					try (ResultSet rs = stmt.executeQuery()) {
						while (rs.next()) {
							Sancion s = new Sancion();
							s.setTipo(TipoSancion.Multa);
							s.setFechaActo(rs.getDate("fech_acto"));
							s.setNumeroActo(rs.getInt("nume_acto"));
							s.setAnoRadicacion(rs.getShort("ano_radi"));
							s.setNumeroRadicacion(rs.getInt("nume_radi"));
							s.setTipoActo(rs.getString("tipo_acto"));
							s.setFirmeza(rs.getBoolean("firmeza"));
							if (!response.contains(s)) {
								response.add(s);
							}
						}
					}
				}
			}
		}
		Collections.sort(response);
		return response;
	}

}
