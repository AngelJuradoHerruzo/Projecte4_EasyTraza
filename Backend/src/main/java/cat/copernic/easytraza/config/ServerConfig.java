package cat.copernic.easytraza.config;

import org.apache.catalina.connector.Connector;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * CONFIGURACIÓ DEL SERVIDOR
 *
 * Manté el connector HTTPS principal per a la interfície web
 * i habilita un connector HTTP addicional per a l'API mòbil.
 */
@Configuration
public class ServerConfig {

    private static final int PORT_HTTP_MOBILE = 8080;
    private static final int PORT_HTTPS_WEB = 8443;


    // ---------------------------- CONNECTOR HTTP ADDICIONAL ----------------------------
    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> connectorHttpMobile() {
        return factory -> factory.addAdditionalConnectors(crearConnectorHttp());
    }


    // CREA EL CONNECTOR HTTP UTILITZAT PER LES PETICIONS DE L'APP MÒBIL
    private Connector crearConnectorHttp() {

        Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);

        connector.setScheme("http");
        connector.setPort(PORT_HTTP_MOBILE);
        connector.setSecure(false);
        connector.setRedirectPort(PORT_HTTPS_WEB);

        return connector;
    }
}