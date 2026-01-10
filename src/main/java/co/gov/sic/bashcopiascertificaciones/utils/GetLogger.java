/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.gov.sic.bashcopiascertificaciones.utils;


import org.apache.log4j.*;

/**
 *
 * @author Ernesto.Mosquera
 */
public class GetLogger {

    private static Logger l = null;

    public static synchronized Logger getInstance(String... name) {

        if (l == null) {
            l = Logger.getLogger(name[0]);
            PropertyConfigurator.configure(name[1]);
        }

        return l;
    }
}
