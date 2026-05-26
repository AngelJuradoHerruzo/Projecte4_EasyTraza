package cat.copernic.easytraza.config;

import java.time.Duration;
import java.util.Locale;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

/**
 * CONFIGURACIÓ WEB
 *
 * Permet accedir des del navegador als fitxers guardats dins la carpeta uploads del backend.
 *
 * @author Ángel Jurado
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    // CONFIGURAR L'IDIOMA DE LA INTERFÍCIE I CONSERVAR LA SELECCIÓ EN UNA COOKIE
    @Bean
    public LocaleResolver localeResolver() {

        CookieLocaleResolver localeResolver = new CookieLocaleResolver("EASYTRAZA_LANG");

        localeResolver.setDefaultLocale(Locale.forLanguageTag("ca"));
        localeResolver.setCookiePath("/");
        localeResolver.setCookieMaxAge(Duration.ofDays(365));
        localeResolver.setCookieHttpOnly(true);
        localeResolver.setCookieSecure(true);

        return localeResolver;
    }

    // PERMETRE EL CANVI D'IDIOMA MITJANÇANT EL PARÀMETRE ?lang=ca O ?lang=es
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {

        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();

        interceptor.setParamName("lang");

        return interceptor;
    }

    // REGISTRAR L'INTERCEPTOR DE CANVI D'IDIOMA A TOTES LES PETICIONS WEB
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }

    // MOSTRAR ELS FITXERS GUARDATS A LA CARPETA UPLOADS
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:backend/uploads/");
    }
}
