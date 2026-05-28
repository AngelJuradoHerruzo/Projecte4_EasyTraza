package cat.copernic.easytraza.config;

import org.apache.catalina.connector.Connector;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * CONFIGURACIÓ DEL SERVIDOR.
 *
 * Mantingut el connector HTTPS principal per a la interfície web
 * i habilitat un connector HTTP addicional per a l'API mòbil.
 *
 * @author Ángel Jurado Herruzo
 */
@Configuration
public class ServerConfig {

    private static final int PORT_HTTP_MOBILE = 8080;
    private static final int PORT_HTTPS_WEB = 8443;


    /**
     * CONNECTOR HTTP MÒBIL.
     *
     * Incorporat al servidor el connector HTTP addicional utilitzat
     * per rebre les peticions procedents de l'aplicació mòbil.
     *
     * @return personalitzador que afegeix el connector HTTP al servidor
     */
    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> connectorHttpMobile() {
        return factory -> factory.addAdditionalConnectors(crearConnectorHttp());
    }


    /**
     * CREACIÓ DEL CONNECTOR HTTP.
     *
     * Creat el connector HTTP destinat a l'API mòbil i configurada
     * la redirecció corresponent cap al port HTTPS de la interfície web.
     *
     * @return connector HTTP configurat per a l'aplicació mòbil
     */
    private Connector crearConnectorHttp() {

        Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);

        connector.setScheme("http");
        connector.setPort(PORT_HTTP_MOBILE);
        connector.setSecure(false);
        connector.setRedirectPort(PORT_HTTPS_WEB);

        return connector;
    }
}
