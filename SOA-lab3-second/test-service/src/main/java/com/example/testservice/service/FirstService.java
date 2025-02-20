package com.example.testservice.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Collections;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import javax.net.ssl.HttpsURLConnection;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class FirstService {
    
    private static final Logger log = LoggerFactory.getLogger(FirstService.class);
    private final RestTemplate restTemplate;
    private final String baseUrl;

    public FirstService(@Qualifier("restTemplate") RestTemplate restTemplate,
                           @Value("${first-service.base-url}") String baseUrl) {
        log.info("Initializing FirstService with baseUrl: {}", baseUrl);
        this.restTemplate = createSslRestTemplate();
        this.baseUrl = baseUrl;
    }

    private RestTemplate createSslRestTemplate() {
        try {
            // Создаем SSLContext который доверяет всем сертификатам
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return null; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) { }
                    public void checkServerTrusted(X509Certificate[] certs, String authType) { }
                }
            };
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            // Создаем SSL соединение которое использует наш SSLContext
            CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLContext(sslContext)
                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                .build();

            HttpComponentsClientHttpRequestFactory requestFactory = 
                new HttpComponentsClientHttpRequestFactory();
            requestFactory.setHttpClient(httpClient);
            requestFactory.setConnectTimeout(10000); // 10 seconds
            requestFactory.setReadTimeout(10000);    // 10 seconds

            log.info("Created SSL RestTemplate with disabled certificate validation");
            return new RestTemplate(requestFactory);
        } catch (Exception e) {
            log.error("Failed to create SSL RestTemplate", e);
            throw new RuntimeException("Could not create SSL RestTemplate", e);
        }
    }

    public ResponseEntity<String> getTest() {
        return restTemplate.getForEntity(baseUrl + "/test", String.class);
    }

    public ResponseEntity<List<Map<String, Object>>> getMoviesWithoutOscars() {
        log.debug("Calling getMoviesWithoutOscars with URL: {}", baseUrl + "/movies?filter=oscarsCount[eq]=null");
        try {
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                baseUrl + "/movies?filter=oscarsCount[eq]=null",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );
            log.debug("Got response from getMoviesWithoutOscars: {}", response.getStatusCode());
            return response;
        } catch (Exception e) {
            log.error("Error in getMoviesWithoutOscars", e);
            throw e;
        }
    }

    public ResponseEntity<List<Map<String, Object>>> getAllMovies() {
        log.debug("Calling getAllMovies with URL: {}", baseUrl + "/movies");
        try {
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                baseUrl + "/movies",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );
            log.debug("Got response from getAllMovies: {}", response.getStatusCode());
            return response;
        } catch (Exception e) {
            log.error("Error in getAllMovies", e);
            throw e;
        }
    }
    
    
} 