package co.gov.sic.bashcopiascertificaciones.utils;


import co.gov.sic.bashcopiascertificaciones.enums.TipoAmbienteEnum;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;



public class Constantes {
    public static String AMBIENTE;

    public static TipoAmbienteEnum AMBIENTE_ACTIVO;// = TipoAmbienteEnum.DESARROLLO

    public static long WS_RADICACION_FUNCIONARIO_RADICADOR_ID;
    public static String WS_INTEROP_USER;
    public static String WS_INTEROP_PASS;
    public static String WS_RECAUDOS_PASS;
    public static String WS_RECAUDOS_USER;
    public static String WS_SIGN_USER;
    public static String WS_SIGN_PASS;
    public static String URL_WS_INTEROP;
    public static String URL_RECAUDOS_BASE;
    public static String URL_WEB_SERVICIOS_EN_LINEA;
    public static String URL_WEB_SERVICIOS_EN_LINEA_CREAR_USUARIO;
    public static String URL_WEB_SERVICIOS_EN_LINEA_RECORDAR_USUARIO;
    public static String URL_WEB_CONSULTA_TRAMITE;
    public static String URL_WS_SIGN;
    public static int WS_SIGN_ID_POLITICA_CON_ESTAMPA;
    public static int WS_SIGN_ID_POLITICA_SIN_ESTAMPA;
    public static String WS_SIGN_NOMBRE_SECRETARIO_AD_HOC;
    public static String TEXTO_LEGALIZACION_FIRMA_SECRETARIO_AD_HOC;

    public static String WS_SIGN_CARGO_SECRETARIO_AD_HOC;
    public static String WS_CANCILLERIA_ID_AUTORIDAD_SECRETARIO_AD_HOC;
    public static String URL_SONDA;
    public static String PATH_ARCHIVOS_FORMULARIO_COPIAS_DRIVE;
    
    public static String PATH_ARCHIVOS_OTROS_FORMULARIOS;

    public static String MAIL_HOST;
    public static String MAIL_FROM;
    public static boolean SEND_MAIL_ENABLE;

    public static double MAX_SIZE_ATTACHMENTS;

    public static String UTF_8;
    public static String CONNECTION_STRING_JNDI;
    public static String TIPO_RADICACION_ENTRADA;
    public static String TIPO_RADICACION_SALIDA;
    public static String COD_SISTEMA_SERVICIOS_LINEA;
    public static String MEDIO_RESPUESTA_ELECTRONICO;
    public static String LLAVE_SESION_USUARIO;
    public static String DEFAULT_MIME_TYPE;
    public static String PDF_MIME_TYPE;
    public static String SENDFILE_HEADER;
    //Extensiones, deben ser mayusculas, sino, el servidor de apache no encuentra los archivos por que es case sensitive
    public static String PDF_EXTENSION;
    public static String ZIP_EXTENSION;

    public static int DEFAULT_SENDFILE_BUFFER_SIZE;
    public static String ROL_REPRESENTANTE_LEGAL;
    public static String ROL_SUPLENTE;
    public static String ROL_SECRETARIO;

    public static String TIPO_PERSONA_EMPRESA;
    public static String TIPO_PERSONA_NATURAL;
    public static String TIPO_PERSONA_FUNCIONARIO;
    public static int ID_SISTEMA_SEDE_ELECTRONICA_PAGO_PSE;

    public static char ENCODE_PAD_CHAR1;
    public static char ENCODE_PAD_CHAR2;
    public static char ENCODE_PAD_CHAR3;

    //Formatos de Fecha
    public static String DATE_TIME_FORMAT_DD_MM_YYYY;
    public static String DATE_TIME_FORMAT_DD_MM_YYYY_AM_PM;
    public static String DATE_TIME_FORMAT_YYYY_MM_DD;
    public static String DATE_TIME_FORMAT_YYYYMMDD;
    public static String DATE_TIME_FORMAT_YYYY_MM_DD_HH_MM_SS;
    public static String DATE_TIME_FORMAT_DD_MMM_YYYY;
    public static String DATE_TIME_FORMAT_D_MMM_YYYY;
    public static String DATE_TIME_FORMAT_DD_MMMM_YYYY;
    public static String DATE_TIME_FORMAT_D_MMMM_YYYY;
    public static String DATE_TIME_FORMAT_MMMM;
    //Nombres Tramites
    public static String NOMBRE_TRAMITE_CERTIFICADO_SANCIONES;
    public static String NOMBRE_TRAMITE_CERTIFICADO_REPRESENTACION_CAMARAS;
    public static String NOMBRE_TRAMITE_CERTIFICADO_FIRMAS_SECRETARIOS_CAMARAS;
    public static String NOMBRE_TRAMITE_COPIAS_SIMPLES;
    public static String NOMBRE_TRAMITE_CORRECCION_REPRESENTACION_CAMARAS;
    public static String NOMBRE_TRAMITE_LISTADOS_INFORMACION;
    //Localizacion
    public static String ES_CO;
    public static Locale LOCALE_ES_CO;

    //Estados de la pasarela
    public static String ESTADO_TRAMITE_CREATED;
    public static String ESTADO_TRAMITE_PENDING;
    public static String ESTADO_TRAMITE_OK;
    public static String ESTADO_TRAMITE_FAILED;
    public static String ESTADO_TRAMITE_EXPIRED;
    public static String ESTADO_TRAMITE_CAPTURED;
    public static String ESTADO_TRAMITE_NOT_AUTHORIZED;
    public static String ESTADO_TRAMITE_CREATED_DESC;
    public static String ESTADO_TRAMITE_PENDING_DESC;
    public static String ESTADO_TRAMITE_OK_DESC;
    public static String ESTADO_TRAMITE_FAILED_DESC;
    public static String ESTADO_TRAMITE_EXPIRED_DESC;
    public static String ESTADO_TRAMITE_CAPTURED_DESC;
    public static String ESTADO_TRAMITE_NOT_AUTHORIZED_DESC;
    //Estados de la aplicacion
    public static String ESTADO_TRAMITE_PRESENTADO;
    public static String ESTADO_TRAMITE_RADICADO_ENTRADA;
    public static String ESTADO_TRAMITE_RADICADO_SALIDA;
    public static String ESTADO_TRAMITE_CERTIFICADO_GENERADO;
    public static String ESTADO_TRAMITE_PENDIENTE_CONFIRMACION_PAGO;
    public static String ESTADO_TRAMITE_RECIBO_CAJA;
    public static String ESTADO_TRAMITE_FINALIZADO;
    public static String ESTADO_TRAMITE_ASIGNADA;
    public static String ESTADO_TRAMITE_COMPLEMENTAR;
    public static String ESTADO_TRAMITE_DESISTIDA;
    public static String ESTADO_TRAMITE_TRASLADO_COMPETENCIA;
    public static String ESTADO_TRAMITE_RTA_SOLICITANTE;
    public static String ESTADO_TRAMITE_SOL_PRORROGA;
    public static String ESTADO_TRAMITE_RTA_SOL_PRORROGA;
    public static String ESTADO_TRAMITE_RTA_SOL_INFO_AREA_INTERNA;
    public static String ESTADO_TRAMITE_SOL_INFO_AREA_INTERNA;
    public static String ESTADO_TRAMITE_RTA_SOL_DIGI_INFO;
    public static String ESTADO_TRAMITE_COTIZACION_ENVIADA;
    public static String ESTADO_TRAMITE_RTA_INFO_AREA_INTERNA;
    public static String ESTADO_TRAMITE_PAGADO;
    public static Short CODI_ACTUACION_ASIGNACION;
    public static Short CODI_ACTUACION_TRASLADO;
    //PSE
    public static int CODIGO_PSE;
    public static String RECAUDOS_TIPO_PAGO_PSE;
    public static String RECAUDOS_COD_BANCO_DE_BOGOTA;
    public static String RECAUDOS_COD_SUCURSAL_CENTRO_INTERNACIONAL;
    public static String RECAUDOS_NUMERO_CUENTA;

    public static int WS_RADICACION_CONS_TASA;
    public static String WS_RADICACION_MEDIO_ENTRADA;
    public static String WS_RADICACION_MEDIO_SALIDA;
    public static String CONTENT_TYPE_JSON;
    public static String CONTENT_TYPE_HTML;
    public static String HTTP_METHOD_GET;
    public static String HTTP_METHOD_POST;
    public static String STR_EMPTY = "";

    public static String DEFAULT_PDF_NO_DATA_MESSAGE;
    public static String AUTHOR;
    public static String KEYWORDS_PDF_RADICACION;
    public static String KEYWORDS_PDF_DEMANDAS;
    public static String KEYWORDS_PDF_CAMARAS;
    public static String KEYWORDS_PDF_FIRMA_SECRETARIO;
    public static String URL_WS_RECAUDOS;

    public static String URL_DOWNLOAD_RECIBO_RECAUDOS;

    public static String[] UNIDADES = {"", "Un ", "Dos ", "Tres ", "Cuatro ", "Cinco ", "Seis ", "Siete ", "Ocho ", "Nueve ", "Diez ", "Once ", "Doce ", "Trece ", "Catorce ", "Quince ", "Dieciséis", "Diecisiete", "Dieciocho", "Diecinueve", "Veinte"};
    public static String[] DECENAS = {"Veinti", "Treinta ", "Cuarenta ", "Cincuenta ", "Sesenta ", "Setenta ", "Ochenta ", "Noventa ", "Cien "};
    public static String[] CENTENAS = {"Ciento ", "Doscientos ", "Trescientos ", "Cuatrocientos ", "Quinientos ", "Seiscientos ", "Setecientos ", "Ochocientos ", "Novecientos "};

    public static String URL_PROTOCOL_HTTPS;
    public static String URL_SIC;
    public static String URL_DATOS_PERSONALES;
    public static String URL_ENCUESTA;

    public static String CERTIFICATE_SIGNER_CC_FIELD_NAME;
    public static String CERTIFICATE_SIGNER_NIT_FIELD_NAME;

    public static String COORDINADOR_SCC;
    public static String RESPONSABLE_SCC;
    public static String VENTANILLA_SE;
    public static String SECRE_NOTIFICA_NAME;
    public static String FILENAME_CHECKSUM_SEPARATOR;
    public static String EMAIL_SOPORTE;

    public static String WS_CANCILLERIA_CODIGO_CERTIFICADO_EXISTENCIA_REPRESENTACION_LEGAL;
    public static String WS_CANCILLERIA_NOMBRE_CAMPO_FIRMA_DIGITAL;
    public static float PDF_FOOTER_FONT_SIZE;
    public static String PDF_APOSTILLE_SUFIX;
    
    
    
    private static final org.apache.log4j.Logger log = GetLogger.getInstance("Constantes", "conf/log4j.properties");
     public Constantes() {
        FileInputStream fis = null;
        try {

            fis = new FileInputStream(new File("conf/Constans.properties"));
            Properties props = new Properties();
            props.load(new InputStreamReader(fis, Charset.forName("UTF-8")));
            AMBIENTE = props.getProperty("AMBIENTE_ACTIVO");

            if ("DE".equals(AMBIENTE)) {
                AMBIENTE_ACTIVO = TipoAmbienteEnum.DESARROLLO;
            } else if ("QA".equals(AMBIENTE)) {
                AMBIENTE_ACTIVO = TipoAmbienteEnum.PRUEBAS;
            } else {
                AMBIENTE_ACTIVO = TipoAmbienteEnum.PRODUCCION;
            }
            log.info(props.getProperty("WS_RADICACION_FUNCIONARIO_RADICADOR_ID"));
            WS_RADICACION_FUNCIONARIO_RADICADOR_ID = Long.parseLong(props.getProperty("WS_RADICACION_FUNCIONARIO_RADICADOR_ID"));
            WS_INTEROP_USER = props.getProperty("WS_INTEROP_USER");
            WS_INTEROP_PASS = props.getProperty("WS_INTEROP_PASS");
            WS_RECAUDOS_PASS = props.getProperty("WS_RECAUDOS_PASS");
            WS_RECAUDOS_USER = props.getProperty("WS_RECAUDOS_USER");
            WS_SIGN_USER = props.getProperty("WS_SIGN_USER");
            WS_SIGN_PASS = props.getProperty("WS_SIGN_PASS");
            URL_WS_INTEROP = props.getProperty("URL_WS_INTEROP");
            URL_RECAUDOS_BASE = props.getProperty("URL_RECAUDOS_BASE");
            URL_WEB_SERVICIOS_EN_LINEA = props.getProperty("URL_WEB_SERVICIOS_EN_LINEA");
            URL_WEB_SERVICIOS_EN_LINEA_CREAR_USUARIO = props.getProperty("URL_WEB_SERVICIOS_EN_LINEA_CREAR_USUARIO");
            URL_WEB_SERVICIOS_EN_LINEA_RECORDAR_USUARIO = props.getProperty("URL_WEB_SERVICIOS_EN_LINEA_RECORDAR_USUARIO");
            URL_WEB_CONSULTA_TRAMITE = props.getProperty("URL_WEB_CONSULTA_TRAMITE");
            URL_WS_SIGN = props.getProperty("URL_WS_SIGN");
            WS_SIGN_ID_POLITICA_CON_ESTAMPA = Integer.parseInt(props.getProperty("WS_SIGN_ID_POLITICA_CON_ESTAMPA"));
            WS_SIGN_ID_POLITICA_SIN_ESTAMPA = Integer.parseInt(props.getProperty("WS_SIGN_ID_POLITICA_SIN_ESTAMPA"));
            WS_SIGN_NOMBRE_SECRETARIO_AD_HOC = props.getProperty("WS_SIGN_NOMBRE_SECRETARIO_AD_HOC");
            TEXTO_LEGALIZACION_FIRMA_SECRETARIO_AD_HOC = props.getProperty("TEXTO_LEGALIZACION_FIRMA_SECRETARIO_AD_HOC");
            WS_SIGN_CARGO_SECRETARIO_AD_HOC = props.getProperty("WS_SIGN_CARGO_SECRETARIO_AD_HOC");
            WS_CANCILLERIA_ID_AUTORIDAD_SECRETARIO_AD_HOC = props.getProperty("WS_CANCILLERIA_ID_AUTORIDAD_SECRETARIO_AD_HOC");
            URL_SONDA = props.getProperty("URL_SONDA");
            PATH_ARCHIVOS_FORMULARIO_COPIAS_DRIVE = String.format(props.getProperty("PATH_ARCHIVOS_FORMULARIO_COPIAS_DRIVE"), AMBIENTE_ACTIVO.toString().substring(0, 4));
            
            PATH_ARCHIVOS_OTROS_FORMULARIOS = String.format(props.getProperty("PATH_ARCHIVOS_OTROS_FORMULARIOS"), AMBIENTE_ACTIVO.toString().substring(0, 4));

            MAIL_HOST = props.getProperty("MAIL_HOST");
            MAIL_FROM = props.getProperty("MAIL_FROM");
            SEND_MAIL_ENABLE = Boolean.parseBoolean(props.getProperty("SEND_MAIL_ENABLE"));
            MAX_SIZE_ATTACHMENTS = Double.parseDouble(props.getProperty("MAX_SIZE_ATTACHMENTS"));

            UTF_8 = props.getProperty("UTF_8");

            CONNECTION_STRING_JNDI = props.getProperty("CONNECTION_STRING_JNDI");
            TIPO_RADICACION_ENTRADA = props.getProperty("TIPO_RADICACION_ENTRADA");
            TIPO_RADICACION_SALIDA = props.getProperty("TIPO_RADICACION_SALIDA");
            COD_SISTEMA_SERVICIOS_LINEA = props.getProperty("COD_SISTEMA_SERVICIOS_LINEA");
            MEDIO_RESPUESTA_ELECTRONICO = props.getProperty("MEDIO_RESPUESTA_ELECTRONICO");
            LLAVE_SESION_USUARIO = props.getProperty("LLAVE_SESION_USUARIO");
            DEFAULT_MIME_TYPE = props.getProperty("DEFAULT_MIME_TYPE");
            PDF_MIME_TYPE = props.getProperty("PDF_MIME_TYPE");
            SENDFILE_HEADER = props.getProperty("SENDFILE_HEADER") + UTF_8 + props.getProperty("SENDFILE_HEADER_END");

            PDF_EXTENSION = props.getProperty("PDF_EXTENSION");
            ZIP_EXTENSION = props.getProperty("ZIP_EXTENSION");

            DEFAULT_SENDFILE_BUFFER_SIZE = Integer.parseInt(props.getProperty("DEFAULT_SENDFILE_BUFFER_SIZE"));
            ROL_REPRESENTANTE_LEGAL = props.getProperty("ROL_REPRESENTANTE_LEGAL");
            ROL_SUPLENTE = props.getProperty("ROL_SUPLENTE");
            ROL_SECRETARIO = props.getProperty("ROL_SECRETARIO");

            TIPO_PERSONA_EMPRESA = props.getProperty("TIPO_PERSONA_EMPRESA");
            TIPO_PERSONA_NATURAL = props.getProperty("TIPO_PERSONA_NATURAL");
            TIPO_PERSONA_FUNCIONARIO = props.getProperty("TIPO_PERSONA_FUNCIONARIO");
            ID_SISTEMA_SEDE_ELECTRONICA_PAGO_PSE = Integer.parseInt(props.getProperty("ID_SISTEMA_SEDE_ELECTRONICA_PAGO_PSE"));

            ENCODE_PAD_CHAR1 = props.getProperty("ENCODE_PAD_CHAR1").charAt(0);
            ENCODE_PAD_CHAR2 = props.getProperty("ENCODE_PAD_CHAR2").charAt(0);
            ENCODE_PAD_CHAR3 = props.getProperty("ENCODE_PAD_CHAR3").charAt(0);

            DATE_TIME_FORMAT_DD_MM_YYYY = props.getProperty("DATE_TIME_FORMAT_DD_MM_YYYY");
            DATE_TIME_FORMAT_DD_MM_YYYY_AM_PM = props.getProperty("DATE_TIME_FORMAT_DD_MM_YYYY_AM_PM");
            DATE_TIME_FORMAT_YYYY_MM_DD = props.getProperty("DATE_TIME_FORMAT_YYYY_MM_DD");
            DATE_TIME_FORMAT_YYYYMMDD = props.getProperty("DATE_TIME_FORMAT_YYYYMMDD");
            DATE_TIME_FORMAT_YYYY_MM_DD_HH_MM_SS = props.getProperty("DATE_TIME_FORMAT_YYYY_MM_DD_HH_MM_SS");
            DATE_TIME_FORMAT_DD_MMM_YYYY = props.getProperty("DATE_TIME_FORMAT_DD_MMM_YYYY");
            DATE_TIME_FORMAT_D_MMM_YYYY = props.getProperty("DATE_TIME_FORMAT_D_MMM_YYYY");
            DATE_TIME_FORMAT_DD_MMMM_YYYY = props.getProperty("DATE_TIME_FORMAT_DD_MMMM_YYYY");
            DATE_TIME_FORMAT_D_MMMM_YYYY = props.getProperty("DATE_TIME_FORMAT_D_MMMM_YYYY");
            DATE_TIME_FORMAT_MMMM = props.getProperty("DATE_TIME_FORMAT_MMMM");

            NOMBRE_TRAMITE_CERTIFICADO_SANCIONES = props.getProperty("NOMBRE_TRAMITE_CERTIFICADO_SANCIONES");
            NOMBRE_TRAMITE_CERTIFICADO_REPRESENTACION_CAMARAS = props.getProperty("NOMBRE_TRAMITE_CERTIFICADO_REPRESENTACION_CAMARAS");
            NOMBRE_TRAMITE_CERTIFICADO_FIRMAS_SECRETARIOS_CAMARAS = props.getProperty("NOMBRE_TRAMITE_CERTIFICADO_FIRMAS_SECRETARIOS_CAMARAS");
            NOMBRE_TRAMITE_COPIAS_SIMPLES = props.getProperty("NOMBRE_TRAMITE_COPIAS_SIMPLES");
            NOMBRE_TRAMITE_CORRECCION_REPRESENTACION_CAMARAS = props.getProperty("NOMBRE_TRAMITE_CORRECCION_REPRESENTACION_CAMARAS");
            NOMBRE_TRAMITE_LISTADOS_INFORMACION = props.getProperty("NOMBRE_TRAMITE_LISTADOS_INFORMACION");

            ES_CO = props.getProperty("ES_CO");
            LOCALE_ES_CO = Locale.forLanguageTag(ES_CO);

            ESTADO_TRAMITE_CREATED = props.getProperty("ESTADO_TRAMITE_CREATED");
            ESTADO_TRAMITE_PENDING = props.getProperty("ESTADO_TRAMITE_PENDING");
            ESTADO_TRAMITE_OK = props.getProperty("ESTADO_TRAMITE_OK");
            ESTADO_TRAMITE_FAILED = props.getProperty("ESTADO_TRAMITE_FAILED");
            ESTADO_TRAMITE_EXPIRED = props.getProperty("ESTADO_TRAMITE_EXPIRED");
            ESTADO_TRAMITE_CAPTURED = props.getProperty("ESTADO_TRAMITE_CAPTURED");
            ESTADO_TRAMITE_NOT_AUTHORIZED = props.getProperty("ESTADO_TRAMITE_NOT_AUTHORIZED");
            ESTADO_TRAMITE_CREATED_DESC = props.getProperty("ESTADO_TRAMITE_CREATED_DESC");
            ESTADO_TRAMITE_PENDING_DESC = props.getProperty("ESTADO_TRAMITE_PENDING_DESC");
            ESTADO_TRAMITE_OK_DESC = props.getProperty("ESTADO_TRAMITE_OK_DESC");
            ESTADO_TRAMITE_FAILED_DESC = props.getProperty("ESTADO_TRAMITE_FAILED_DESC");
            ESTADO_TRAMITE_EXPIRED_DESC = props.getProperty("ESTADO_TRAMITE_EXPIRED_DESC");
            ESTADO_TRAMITE_CAPTURED_DESC = props.getProperty("ESTADO_TRAMITE_CAPTURED_DESC");
            ESTADO_TRAMITE_NOT_AUTHORIZED_DESC = props.getProperty("ESTADO_TRAMITE_NOT_AUTHORIZED_DESC");
            ESTADO_TRAMITE_PRESENTADO = props.getProperty("ESTADO_TRAMITE_PRESENTADO");
            ESTADO_TRAMITE_RADICADO_ENTRADA = props.getProperty("ESTADO_TRAMITE_RADICADO_ENTRADA");
            ESTADO_TRAMITE_RADICADO_SALIDA = props.getProperty("ESTADO_TRAMITE_RADICADO_SALIDA");
            ESTADO_TRAMITE_CERTIFICADO_GENERADO = props.getProperty("ESTADO_TRAMITE_CERTIFICADO_GENERADO");
            ESTADO_TRAMITE_PENDIENTE_CONFIRMACION_PAGO = props.getProperty("ESTADO_TRAMITE_PENDIENTE_CONFIRMACION_PAGO");
            ESTADO_TRAMITE_RECIBO_CAJA = props.getProperty("ESTADO_TRAMITE_RECIBO_CAJA");
            ESTADO_TRAMITE_FINALIZADO = props.getProperty("ESTADO_TRAMITE_FINALIZADO");
            ESTADO_TRAMITE_ASIGNADA = props.getProperty("ESTADO_TRAMITE_ASIGNADA");
            ESTADO_TRAMITE_COMPLEMENTAR = props.getProperty("ESTADO_TRAMITE_COMPLEMENTAR");
            ESTADO_TRAMITE_DESISTIDA = props.getProperty("ESTADO_TRAMITE_DESISTIDA");
            ESTADO_TRAMITE_TRASLADO_COMPETENCIA = props.getProperty("ESTADO_TRAMITE_TRASLADO_COMPETENCIA");
            ESTADO_TRAMITE_RTA_SOLICITANTE = props.getProperty("ESTADO_TRAMITE_RTA_SOLICITANTE");
            ESTADO_TRAMITE_SOL_PRORROGA = props.getProperty("ESTADO_TRAMITE_SOL_PRORROGA");
            ESTADO_TRAMITE_RTA_SOL_PRORROGA = props.getProperty("ESTADO_TRAMITE_RTA_SOL_PRORROGA");
            ESTADO_TRAMITE_RTA_SOL_INFO_AREA_INTERNA = props.getProperty("ESTADO_TRAMITE_RTA_SOL_INFO_AREA_INTERNA");
            ESTADO_TRAMITE_RTA_SOL_DIGI_INFO = props.getProperty("ESTADO_TRAMITE_RTA_SOL_DIGI_INFO");
            ESTADO_TRAMITE_COTIZACION_ENVIADA = props.getProperty("ESTADO_TRAMITE_COTIZACION_ENVIADA");
            ESTADO_TRAMITE_RTA_INFO_AREA_INTERNA = props.getProperty("ESTADO_TRAMITE_RTA_INFO_AREA_INTERNA");
            ESTADO_TRAMITE_PAGADO = props.getProperty("ESTADO_TRAMITE_PAGADO");
            CODI_ACTUACION_ASIGNACION = Short.parseShort(props.getProperty("CODI_ACTUACION_ASIGNACION"));
            CODI_ACTUACION_TRASLADO = Short.parseShort(props.getProperty("CODI_ACTUACION_TRASLADO"));

            CODIGO_PSE = Integer.parseInt(props.getProperty("CODIGO_PSE"));
            RECAUDOS_TIPO_PAGO_PSE = props.getProperty("RECAUDOS_TIPO_PAGO_PSE");
            RECAUDOS_COD_BANCO_DE_BOGOTA = props.getProperty("RECAUDOS_COD_BANCO_DE_BOGOTA");
            RECAUDOS_COD_SUCURSAL_CENTRO_INTERNACIONAL = props.getProperty("RECAUDOS_COD_SUCURSAL_CENTRO_INTERNACIONAL");
            RECAUDOS_NUMERO_CUENTA = props.getProperty("RECAUDOS_NUMERO_CUENTA");

            WS_RADICACION_CONS_TASA = Integer.parseInt(props.getProperty("WS_RADICACION_CONS_TASA"));
            WS_RADICACION_MEDIO_ENTRADA = props.getProperty("WS_RADICACION_MEDIO_ENTRADA");
            WS_RADICACION_MEDIO_SALIDA = props.getProperty("WS_RADICACION_MEDIO_SALIDA");
            CONTENT_TYPE_JSON = props.getProperty("CONTENT_TYPE_JSON") + UTF_8;
            CONTENT_TYPE_HTML = props.getProperty("CONTENT_TYPE_HTML") + UTF_8;
            HTTP_METHOD_GET = props.getProperty("HTTP_METHOD_GET");
            HTTP_METHOD_POST = props.getProperty("HTTP_METHOD_POST");

            DEFAULT_PDF_NO_DATA_MESSAGE = props.getProperty("DEFAULT_PDF_NO_DATA_MESSAGE");
            AUTHOR = props.getProperty("AUTHOR");
            KEYWORDS_PDF_RADICACION = props.getProperty("KEYWORDS_PDF_RADICACION");
            KEYWORDS_PDF_DEMANDAS = props.getProperty("KEYWORDS_PDF_DEMANDAS");
            KEYWORDS_PDF_CAMARAS = props.getProperty("KEYWORDS_PDF_CAMARAS");
            KEYWORDS_PDF_FIRMA_SECRETARIO = props.getProperty("KEYWORDS_PDF_FIRMA_SECRETARIO");
            URL_WS_RECAUDOS = URL_RECAUDOS_BASE + props.getProperty("URL_WS_RECAUDOS");

            URL_DOWNLOAD_RECIBO_RECAUDOS = URL_RECAUDOS_BASE + props.getProperty("URL_DOWNLOAD_RECIBO_RECAUDOS_1") + WS_RECAUDOS_USER + props.getProperty("URL_DOWNLOAD_RECIBO_RECAUDOS_2") + WS_RECAUDOS_PASS;

            URL_PROTOCOL_HTTPS = props.getProperty("URL_PROTOCOL_HTTPS");
            URL_SIC = URL_PROTOCOL_HTTPS + props.getProperty("URL_SIC");
            URL_DATOS_PERSONALES = URL_SIC + props.getProperty("URL_DATOS_PERSONALES");
            URL_ENCUESTA = props.getProperty("URL_ENCUESTA");

            CERTIFICATE_SIGNER_CC_FIELD_NAME = props.getProperty("CERTIFICATE_SIGNER_CC_FIELD_NAME");
            CERTIFICATE_SIGNER_NIT_FIELD_NAME = props.getProperty("CERTIFICATE_SIGNER_NIT_FIELD_NAME");

            COORDINADOR_SCC = props.getProperty("COORDINADOR_SCC");
            RESPONSABLE_SCC = props.getProperty("RESPONSABLE_SCC");
            VENTANILLA_SE = props.getProperty("VENTANILLA_SE");
            SECRE_NOTIFICA_NAME = props.getProperty("SECRE_NOTIFICA_NAME");
            FILENAME_CHECKSUM_SEPARATOR = props.getProperty("FILENAME_CHECKSUM_SEPARATOR");
            EMAIL_SOPORTE = props.getProperty("EMAIL_SOPORTE");

            WS_CANCILLERIA_CODIGO_CERTIFICADO_EXISTENCIA_REPRESENTACION_LEGAL = props.getProperty("WS_CANCILLERIA_CODIGO_CERTIFICADO_EXISTENCIA_REPRESENTACION_LEGAL");
            WS_CANCILLERIA_NOMBRE_CAMPO_FIRMA_DIGITAL = props.getProperty("WS_CANCILLERIA_NOMBRE_CAMPO_FIRMA_DIGITAL");
            PDF_FOOTER_FONT_SIZE = Float.parseFloat(props.getProperty("PDF_FOOTER_FONT_SIZE"));
            PDF_APOSTILLE_SUFIX = props.getProperty("PDF_APOSTILLE_SUFIX");

            log.debug(AMBIENTE);
            log.debug(String.valueOf(WS_RADICACION_FUNCIONARIO_RADICADOR_ID));
            log.debug(WS_INTEROP_USER);
            log.debug(WS_INTEROP_PASS);
            log.debug(WS_RECAUDOS_PASS);
            log.debug(WS_RECAUDOS_USER);
            log.debug(WS_SIGN_USER);
            log.debug(WS_SIGN_PASS);
            log.debug(URL_WS_INTEROP);
            log.debug(URL_RECAUDOS_BASE);
            log.debug(URL_WEB_SERVICIOS_EN_LINEA);
            log.debug(URL_WEB_SERVICIOS_EN_LINEA_CREAR_USUARIO);
            log.debug(URL_WEB_SERVICIOS_EN_LINEA_RECORDAR_USUARIO);
            log.debug(URL_WEB_CONSULTA_TRAMITE);
            log.debug(URL_WS_SIGN);
            log.debug(String.valueOf(WS_SIGN_ID_POLITICA_CON_ESTAMPA));
            log.debug(String.valueOf(WS_SIGN_ID_POLITICA_SIN_ESTAMPA));
            log.debug(WS_SIGN_NOMBRE_SECRETARIO_AD_HOC);
            log.debug(TEXTO_LEGALIZACION_FIRMA_SECRETARIO_AD_HOC);
            log.debug(WS_SIGN_CARGO_SECRETARIO_AD_HOC);
            log.debug(WS_CANCILLERIA_ID_AUTORIDAD_SECRETARIO_AD_HOC);
            log.debug(URL_SONDA);
            log.debug(PATH_ARCHIVOS_FORMULARIO_COPIAS_DRIVE);
            log.debug(PATH_ARCHIVOS_OTROS_FORMULARIOS);
            log.debug(MAIL_HOST);
            log.debug(MAIL_FROM);
            log.debug(String.valueOf(SEND_MAIL_ENABLE));
            log.debug(String.valueOf(MAX_SIZE_ATTACHMENTS));
            log.debug(UTF_8);
            log.debug(CONNECTION_STRING_JNDI);
            log.debug(TIPO_RADICACION_ENTRADA);
            log.debug(TIPO_RADICACION_SALIDA);
            log.debug(COD_SISTEMA_SERVICIOS_LINEA);
            log.debug(MEDIO_RESPUESTA_ELECTRONICO);
            log.debug(LLAVE_SESION_USUARIO);
            log.debug(DEFAULT_MIME_TYPE);
            log.debug(PDF_MIME_TYPE);
            log.debug(SENDFILE_HEADER);
            log.debug(PDF_EXTENSION);
            log.debug(ZIP_EXTENSION);
            log.debug(String.valueOf(DEFAULT_SENDFILE_BUFFER_SIZE));
            log.debug(ROL_REPRESENTANTE_LEGAL);
            log.debug(ROL_SUPLENTE);
            log.debug(ROL_SECRETARIO);
            log.debug(TIPO_PERSONA_EMPRESA);
            log.debug(TIPO_PERSONA_NATURAL);
            log.debug(TIPO_PERSONA_FUNCIONARIO);
            log.debug(String.valueOf(ID_SISTEMA_SEDE_ELECTRONICA_PAGO_PSE));
            log.debug(String.valueOf(ENCODE_PAD_CHAR1));
            log.debug(String.valueOf(ENCODE_PAD_CHAR2));
            log.debug(String.valueOf(ENCODE_PAD_CHAR3));
            log.debug(DATE_TIME_FORMAT_DD_MM_YYYY);
            log.debug(DATE_TIME_FORMAT_DD_MM_YYYY_AM_PM);
            log.debug(DATE_TIME_FORMAT_YYYY_MM_DD);
            log.debug(DATE_TIME_FORMAT_YYYYMMDD);
            log.debug(DATE_TIME_FORMAT_YYYY_MM_DD_HH_MM_SS);
            log.debug(DATE_TIME_FORMAT_DD_MMM_YYYY);
            log.debug(DATE_TIME_FORMAT_D_MMM_YYYY);
            log.debug(DATE_TIME_FORMAT_DD_MMMM_YYYY);
            log.debug(DATE_TIME_FORMAT_D_MMMM_YYYY);
            log.debug(DATE_TIME_FORMAT_MMMM);
            log.debug(NOMBRE_TRAMITE_CERTIFICADO_SANCIONES);
            log.debug(NOMBRE_TRAMITE_CERTIFICADO_REPRESENTACION_CAMARAS);
            log.debug(NOMBRE_TRAMITE_CERTIFICADO_FIRMAS_SECRETARIOS_CAMARAS);
            log.debug(NOMBRE_TRAMITE_COPIAS_SIMPLES);
            log.debug(NOMBRE_TRAMITE_CORRECCION_REPRESENTACION_CAMARAS);
            log.debug(NOMBRE_TRAMITE_LISTADOS_INFORMACION);
            log.debug(ES_CO);
            log.debug(ESTADO_TRAMITE_CREATED);
            log.debug(ESTADO_TRAMITE_PENDING);
            log.debug(ESTADO_TRAMITE_OK);
            log.debug(ESTADO_TRAMITE_FAILED);
            log.debug(ESTADO_TRAMITE_EXPIRED);
            log.debug(ESTADO_TRAMITE_CAPTURED);
            log.debug(ESTADO_TRAMITE_NOT_AUTHORIZED);
            log.debug(ESTADO_TRAMITE_CREATED_DESC);
            log.debug(ESTADO_TRAMITE_PENDING_DESC);
            log.debug(ESTADO_TRAMITE_OK_DESC);
            log.debug(ESTADO_TRAMITE_FAILED_DESC);
            log.debug(ESTADO_TRAMITE_EXPIRED_DESC);
            log.debug(ESTADO_TRAMITE_CAPTURED_DESC);
            log.debug(ESTADO_TRAMITE_NOT_AUTHORIZED_DESC);
            log.debug(ESTADO_TRAMITE_PRESENTADO);
            log.debug(ESTADO_TRAMITE_RADICADO_ENTRADA);
            log.debug(ESTADO_TRAMITE_RADICADO_SALIDA);
            log.debug(ESTADO_TRAMITE_CERTIFICADO_GENERADO);
            log.debug(ESTADO_TRAMITE_PENDIENTE_CONFIRMACION_PAGO);
            log.debug(ESTADO_TRAMITE_RECIBO_CAJA);
            log.debug(ESTADO_TRAMITE_FINALIZADO);
            log.debug(ESTADO_TRAMITE_ASIGNADA);
            log.debug(ESTADO_TRAMITE_COMPLEMENTAR);
            log.debug(ESTADO_TRAMITE_DESISTIDA);
            log.debug(ESTADO_TRAMITE_TRASLADO_COMPETENCIA);
            log.debug(ESTADO_TRAMITE_RTA_SOLICITANTE);
            log.debug(ESTADO_TRAMITE_SOL_PRORROGA);
            log.debug(ESTADO_TRAMITE_RTA_SOL_PRORROGA);
            log.debug(ESTADO_TRAMITE_RTA_SOL_INFO_AREA_INTERNA);
            log.debug(ESTADO_TRAMITE_RTA_SOL_DIGI_INFO);
            log.debug(ESTADO_TRAMITE_COTIZACION_ENVIADA);
            log.debug(ESTADO_TRAMITE_RTA_INFO_AREA_INTERNA);
            log.debug(ESTADO_TRAMITE_PAGADO);
            log.debug(String.valueOf(CODI_ACTUACION_ASIGNACION));
            log.debug(String.valueOf(CODI_ACTUACION_TRASLADO));
            log.debug(String.valueOf(CODIGO_PSE));
            log.debug(RECAUDOS_TIPO_PAGO_PSE);
            log.debug(RECAUDOS_COD_BANCO_DE_BOGOTA);
            log.debug(RECAUDOS_COD_SUCURSAL_CENTRO_INTERNACIONAL);
            log.debug(RECAUDOS_NUMERO_CUENTA);
            log.debug(String.valueOf(WS_RADICACION_CONS_TASA));
            log.debug(WS_RADICACION_MEDIO_ENTRADA);
            log.debug(WS_RADICACION_MEDIO_SALIDA);
            log.debug(CONTENT_TYPE_JSON);
            log.debug(CONTENT_TYPE_HTML);
            log.debug(HTTP_METHOD_GET);
            log.debug(HTTP_METHOD_POST);
            log.debug(DEFAULT_PDF_NO_DATA_MESSAGE);
            log.debug(AUTHOR);
            log.debug(KEYWORDS_PDF_RADICACION);
            log.debug(KEYWORDS_PDF_DEMANDAS);
            log.debug(KEYWORDS_PDF_CAMARAS);
            log.debug(KEYWORDS_PDF_FIRMA_SECRETARIO);
            log.debug(URL_WS_RECAUDOS);
            log.debug(URL_DOWNLOAD_RECIBO_RECAUDOS);
            log.debug(URL_PROTOCOL_HTTPS);
            log.debug(URL_SIC);
            log.debug(URL_DATOS_PERSONALES);
            log.debug(URL_ENCUESTA);
            log.debug(CERTIFICATE_SIGNER_CC_FIELD_NAME);
            log.debug(CERTIFICATE_SIGNER_NIT_FIELD_NAME);
            log.debug(COORDINADOR_SCC);
            log.debug(RESPONSABLE_SCC);
            log.debug(VENTANILLA_SE);
            log.debug(SECRE_NOTIFICA_NAME);
            log.debug(FILENAME_CHECKSUM_SEPARATOR);
            log.debug(EMAIL_SOPORTE);
            log.debug(WS_CANCILLERIA_CODIGO_CERTIFICADO_EXISTENCIA_REPRESENTACION_LEGAL);
            log.debug(WS_CANCILLERIA_NOMBRE_CAMPO_FIRMA_DIGITAL);
            log.debug(String.valueOf(PDF_FOOTER_FONT_SIZE));
            log.debug(PDF_APOSTILLE_SUFIX);

        } catch (FileNotFoundException ex) {
            Logger.getLogger(Constantes.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Constantes.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fis.close();
            } catch (IOException ex) {
                Logger.getLogger(Constantes.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

    }
}
