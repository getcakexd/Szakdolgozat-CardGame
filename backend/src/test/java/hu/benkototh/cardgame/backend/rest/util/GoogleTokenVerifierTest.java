package hu.benkototh.cardgame.backend.rest.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoogleTokenVerifierTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private GoogleTokenVerifier googleTokenVerifier;

    private final String testClientId = "test-client-id";
    private final String testToken = "test-token";
    private final String tokenInfoUrl = "https://oauth2.googleapis.com/tokeninfo?id_token=";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(googleTokenVerifier, "clientId", testClientId);
        ReflectionTestUtils.setField(googleTokenVerifier, "restTemplate", restTemplate);
    }

    @Test
    void testVerifyValidToken() {
        Map<String, String> tokenInfo = new HashMap<>();
        tokenInfo.put("aud", testClientId);
        tokenInfo.put("sub", "123456789");
        tokenInfo.put("email", "test@example.com");
        
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(tokenInfo, HttpStatus.OK);
        
        when(restTemplate.exchange(
                eq(tokenInfoUrl + testToken),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(responseEntity);
        
        Map result = googleTokenVerifier.verify(testToken);
        
        assertNotNull(result);
        assertEquals(testClientId, result.get("aud"));
        assertEquals("123456789", result.get("sub"));
        assertEquals("test@example.com", result.get("email"));
    }

    @Test
    void testVerifyInvalidClientId() {
        Map<String, String> tokenInfo = new HashMap<>();
        tokenInfo.put("aud", "invalid-client-id");
        tokenInfo.put("sub", "123456789");
        
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(tokenInfo, HttpStatus.OK);
        
        when(restTemplate.exchange(
                eq(tokenInfoUrl + testToken),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(responseEntity);
        
        Map result = googleTokenVerifier.verify(testToken);
        
        assertNull(result);
    }

    @Test
    void testVerifyNullResponse() {
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(null, HttpStatus.OK);
        
        when(restTemplate.exchange(
                eq(tokenInfoUrl + testToken),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(responseEntity);
        
        Map result = googleTokenVerifier.verify(testToken);
        
        assertNull(result);
    }

    @Test
    void testVerifyRestClientException() {
        when(restTemplate.exchange(
                eq(tokenInfoUrl + testToken),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenThrow(new RestClientException("API error"));
        
        Map result = googleTokenVerifier.verify(testToken);
        
        assertNull(result);
    }
}