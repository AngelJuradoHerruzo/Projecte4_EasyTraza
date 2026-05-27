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
 * CONFIGURACIÓ WEB.
 *
 * Configurats els recursos accessibles des del navegador i el mecanisme
 * de selecció d'idioma de la interfície web, mantenint la preferència
 * escollida per l'usuari.
 *
 * @author Ángel Jurado Herruz
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {


    /**
     * IDIOMA DE LA INTERFÍCIE.
     *
     * Configurat el català com a idioma predeterminat i conservada
     * la selecció de l'usuari mitjançant una cookie segura.
     *
     * @return resolutor encarregat de gestionar l'idioma seleccionat
     */
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


    /**
     * CANVI D'IDIOMA.
     *
     * Creat l'interceptor que permet modificar l'idioma de la interfície
     * mitjançant el paràmetre rebut a la petició web.
     *
     * @return interceptor configurat per processar el canvi d'idioma
     */
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {

        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();

        interceptor.setParamName("lang");

        return interceptor;
    }


    /**
     * REGISTRE DE L'INTERCEPTOR D'IDIOMA.
     *
     * Registrat l'interceptor de canvi d'idioma perquè pugui actuar
     * sobre les peticions rebudes per la interfície web.
     *
     * @param registry registre d'interceptors de l'aplicació web
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }


    /**
     * ACCÉS ALS FITXERS PUJATS.
     *
     * Habilitat l'accés des del navegador als fitxers guardats
     * dins la carpeta d'uploads del backend.
     *
     * @param registry registre de recursos accessibles des de la web
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:backend/uploads/");
    }
}
