package hu.benkototh.cardgame.backend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Configuration
@Profile("heroku")
public class HerokuWebSocketConfig {

    @Value("${DYNO:unknown}")
    private String dyno;

    @Bean
    public OncePerRequestFilter herokuWebSocketFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
                if (request.getRequestURI().contains("/api/ws")) {
                    response.addHeader("Access-Control-Allow-Origin", "*");
                    response.addHeader("Access-Control-Allow-Headers", "origin, content-type, accept, authorization");
                    response.addHeader("Access-Control-Allow-Credentials", "true");
                    response.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");

                    if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
                        response.setStatus(HttpServletResponse.SC_OK);
                        return;
                    }
                }

                filterChain.doFilter(request, response);
            }

        };
    }
}
