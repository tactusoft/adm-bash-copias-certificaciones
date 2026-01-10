/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.gov.sic.bashcopiascertificaciones.database;


import co.gov.sic.bashcopiascertificaciones.utils.DesEncrypter;
import co.gov.sic.bashcopiascertificaciones.utils.GetLogger;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;
import org.apache.log4j.Logger;

/**
 *
 * @author elmos
 */
public class DataBaseConfig {

    private static DataBaseConfig dbCfg = null;

    private static Properties fileProperties = null;
    private static Logger log;
    private static String URL;
    private static String Hostname;
    private static String Port;
    private static String Password;
    private static String Driver;
    private static String Username;
    private static String DataServerName;
    private static DesEncrypter des;
    private static String DbName;

    private DataBaseConfig() {
        log = GetLogger.getInstance("conf/log4j.properties");
        des = new DesEncrypter();
        fileProperties = new Properties();
    }

    public static boolean saveParameterConfig(String parameter) {
        try {
            OutputStream output = new FileOutputStream("conf/database.properties");
            log.info("Configurando parametro " + parameter.substring(0, parameter.indexOf("=")));
            if (parameter.contains("Database.Password")) {
                log.info("Cifrando password");
                fileProperties.setProperty(parameter.substring(0, parameter.indexOf("=")), des.encrypt(parameter.substring(parameter.indexOf("=") + 1, parameter.length())));
                
            } else {
                fileProperties.setProperty(parameter.substring(0, parameter.indexOf("=")), parameter.substring(parameter.indexOf("=") + 1, parameter.length()));
            }
            
            fileProperties.store(output, null);
            log.info("Parametro " + parameter.substring(0, parameter.indexOf("=")) + " actualizado correctamente");
        } catch (FileNotFoundException ex) {
            log.error(ex.toString());
        } catch (IOException ex) {
            log.error(ex.toString());
        }
        return false;
    }

    public static void loadConfig() {

        try {

            fileProperties.load(new FileInputStream("conf/database.properties"));
            log.info("Cargando propiedades de base de datos");

            Hostname = fileProperties.getProperty("Database.Hostname");
            log.debug("Database.Hostname = " + Hostname);

            Port = fileProperties.getProperty("Database.Port");
            log.debug("Database.Port = " + Port);

            Driver = fileProperties.getProperty("Database.Driver");
            log.debug("Database.Driver = " + Driver);

            Username = fileProperties.getProperty("Database.Username");
            log.debug("Database.Username = " + Username);

            Password = des.decrypt(fileProperties.getProperty("Database.Password"));
            log.debug("Database.Password = " + fileProperties.getProperty("Database.Password"));
            
            DataServerName = fileProperties.getProperty("DataBase.DataServer");
            log.debug("DataBase.DataServer = " + DataServerName);

            DbName = fileProperties.getProperty("Database.DbName");
            log.debug("DataBase.DbName = " + DbName);

            URL = "jdbc:informix-sqli://" + Hostname + ":" + Port + "/" + DbName + ":INFORMIXSERVER=" + DataServerName + ";user=" + Username + ";password=" + fileProperties.getProperty("Database.Password");
            log.info("DataBase.URL = " + URL);
            URL = "jdbc:informix-sqli://" + Hostname + ":" + Port + "/" + DbName + ":INFORMIXSERVER=" + DataServerName + ";user=" + Username + ";password=" + Password;

            log.info("Datos cargados conexito");

        } catch (FileNotFoundException e) {
            log.error(e.toString());
            log.error("El archivo de configuracion no existe");
        } catch (IOException e) {
            log.error(e.toString());
            log.error("El archivo de configuracion esta vacio");
        }
    }

    public static DataBaseConfig getInstance() {
        if (dbCfg == null) {
            dbCfg = new DataBaseConfig();
            loadConfig();
        }
        return dbCfg;
    }

    /**
     * @return the URL
     */
    public static String getURL() {
        return URL;
    }

    /**
     * @return the Hostname
     */
    public static String getHostname() {
        return Hostname;
    }

    /**
     * @return the Port
     */
    public static String getPort() {
        return Port;
    }

    /**
     * @return the Password
     */
    public static String getPassword() {
        return Password;
    }

    /**
     * @return the Driver
     */
    public static String getDriver() {
        return Driver;
    }

    /**
     * @return the Username
     */
    public static String getUsername() {
        return Username;
    }

    /**
     * @return the DataServerName
     */
    public static String getDataServerName() {
        return DataServerName;
    }

    /**
     * @return the DbName
     */
    public static String getDbName() {
        return DbName;
    }

}
