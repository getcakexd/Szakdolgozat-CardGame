package hu.benkototh.cardgame.backend.rest.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class GoogleTokenVerifier {

    private static final String GOOGLE_TOKEN_INFO_URL = "https://oauth2.googleapis.com/tokeninfo?id_token=";

    @Value("${google.client.id}")
    private String clientId;

    private final RestTemplate restTemplate = new RestTemplate();

    public Map verify(String idTokenString) {
        try {
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    GOOGLE_TOKEN_INFO_URL + idTokenString,
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            Map tokenInfo = response.getBody();

            if (tokenInfo != null && clientId.equals(tokenInfo.get("aud"))) {
                return tokenInfo;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}