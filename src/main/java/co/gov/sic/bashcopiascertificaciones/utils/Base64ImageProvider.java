package co.gov.sic.bashcopiascertificaciones.utils;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Image;
import com.itextpdf.tool.xml.pipeline.html.AbstractImageProvider;
import java.io.IOException;
import java.util.Base64;
import org.apache.log4j.Logger;

/**
 * Proveedor de imágenes personalizado que maneja imágenes base64 embebidas en HTML.
 * Extiende AbstractImageProvider de iText para procesar correctamente las imágenes
 * codificadas en base64 dentro de los data URLs.
 */
public class Base64ImageProvider extends AbstractImageProvider {

    private static final Logger logger = GetLogger.getInstance("Base64ImageProvider", "conf/log4j.properties");
    private final String rootPath;

    public Base64ImageProvider(String rootPath) {
        this.rootPath = rootPath;
    }

    @Override
    public Image retrieve(String src) {
        try {
            // Verificar si es una imagen base64
            if (src.startsWith("data:image")) {
                logger.info("Procesando imagen base64 desde data URL");

                // Extraer el contenido base64 del data URL
                // Formato: data:image/png;base64,iVBORw0KGgoAAAANS...
                int commaIndex = src.indexOf(',');
                if (commaIndex != -1) {
                    String base64Data = src.substring(commaIndex + 1);

                    // Decodificar base64
                    byte[] imageBytes = Base64.getDecoder().decode(base64Data);
                    logger.info("Imagen base64 decodificada correctamente (" + imageBytes.length + " bytes)");

                    // Crear imagen de iText desde los bytes
                    Image image = Image.getInstance(imageBytes);
                    return image;
                } else {
                    logger.error("Formato de data URL inválido: no se encontró la coma separadora");
                }
            } else {
                // Para rutas normales, intentar cargar desde el classpath o filesystem
                logger.info("Intentando cargar imagen desde ruta: " + src);
                return Image.getInstance(src);
            }
        } catch (BadElementException | IOException e) {
            logger.error("Error al procesar imagen: " + src + " - " + e.getMessage(), e);
        }

        return null;
    }

    @Override
    public String getImageRootPath() {
        return rootPath;
    }
}
