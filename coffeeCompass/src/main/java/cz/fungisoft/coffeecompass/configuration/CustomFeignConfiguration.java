package cz.fungisoft.coffeecompass.configuration;

import cz.fungisoft.coffeecompass.exceptions.CustomFeignClientErrorDecoder;
import feign.Client;
import feign.Feign;
import feign.Logger;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.util.ResourceUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

@Configuration
@Slf4j
public class CustomFeignConfiguration {

    @Value("${myfeign.ssl.key-store-password}")
    private String keyStorePassword;

    @Value("${myfeign.ssl.key-store-type}")
    private String keyStoreType;

    @Value("${myfeign.ssl.key-store}")
    private String keyStoreFile;

    @Value("${http.client.ssl.trust-store-password}")
    private String keyTrustStorePassword;

    @Value("${http.client.ssl.trust-store-type}")
    private String keyTrustStoreType;

    @Value("${http.client.ssl.trust-store}")
    private String keyTrustStoreFile;

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return new CustomFeignClientErrorDecoder();
    }

    @Bean
    @Profile({"prod_docker_https", "dev"})
    public Feign.Builder feignBuilder() {
        return Feign.builder()
                .retryer(Retryer.NEVER_RETRY)
                .client(new Client.Default(getSSLSocketFactory(), null));
    }

    SSLSocketFactory getSSLSocketFactory() {
        char[] allPassword = keyStorePassword.toCharArray();
        SSLContext sslContext = null;
        try {
            sslContext = SSLContextBuilder
                    .create()
                    .setKeyStoreType(keyStoreType)
                    .loadKeyMaterial(ResourceUtils.getFile(keyStoreFile), allPassword, allPassword)
                    .loadTrustMaterial(ResourceUtils.getFile(keyTrustStoreFile), allPassword)
                    .build();
        } catch (Exception e) {
            log.error("SSL configuration error: " + e.getMessage()); }
        return sslContext.getSocketFactory();
    }

}
