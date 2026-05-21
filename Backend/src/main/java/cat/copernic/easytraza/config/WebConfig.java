package cat.copernic.easytraza.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CONFIGURACIÓ WEB
 *
 * Permet accedir des del navegador als fitxers guardats dins la carpeta uploads del backend.
 *
 * @author Ángel Jurado
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    // MOSTRAR ELS FITXERS GUARDATS A LA CARPETA UPLOADS
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:backend/uploads/");
    }
}
