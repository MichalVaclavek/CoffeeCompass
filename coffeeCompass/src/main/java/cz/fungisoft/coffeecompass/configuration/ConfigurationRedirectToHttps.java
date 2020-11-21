package cz.fungisoft.coffeecompass.configuration;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Místo pro Configuration Beans apod. Zde jde o konfiguraci pro pripad https protokolu, kdy je potreba rici vestavenemu Tomcat
 * serveru, aby presmeroval pozadavky na port 8080 (http) na port 8443 (https)
 * 
 * @author Michal Václavek
 */
@Configuration
@Profile({"prod_https","dev_https"})
public class ConfigurationRedirectToHttps 
{  
    @Bean
    public ServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory tomcat =
            new TomcatServletWebServerFactory() {
                @Override
                protected void postProcessContext(Context context) {
                   SecurityConstraint securityConstraint = new SecurityConstraint();
                   securityConstraint.setUserConstraint("CONFIDENTIAL");
                   SecurityCollection collection = new SecurityCollection();
                   collection.addPattern("/*");
                   securityConstraint.addCollection(collection);
                   context.addConstraint(securityConstraint);
                }
            };
        tomcat.addAdditionalTomcatConnectors(redirectConnector());
        return tomcat;
    }

    /**
     * HTTPS - redirect port 8080 to port 8443 (https)
     * It also requires redirecting of 443 port to 8443 port on linux server
     * (redhat, centos6) # iptables -t nat -A PREROUTING -p tcp --dport 443 -j REDIRECT --to-ports 8443
     */
    private Connector redirectConnector() {
        Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        connector.setScheme("http");
        connector.setPort(8080);
        connector.setSecure(false);
        connector.setRedirectPort(8443);
        return connector;
    }
}