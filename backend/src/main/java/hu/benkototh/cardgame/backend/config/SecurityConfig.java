package hu.benkototh.cardgame.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    final private String frontendLocalhost = "http://localhost:4200";
    final private String frontendHost = "https://cardhub-20f674fb9639.herokuapp.com";


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests((authorizeHttpRequests) ->
                        authorizeHttpRequests
                                .requestMatchers("/api/heartbeat", "/api/users/**").permitAll()
                                //.hasAnyAuthority("ROLE_USER")
                                .anyRequest().authenticated()

                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(
                        cors -> cors.configurationSource(request -> {
                            var corsConfig = new CorsConfiguration();
                            corsConfig.setAllowCredentials(true);
                            corsConfig.addAllowedOriginPattern("*");
                            corsConfig.addAllowedOrigin(frontendLocalhost);
                            corsConfig.addAllowedOrigin(frontendHost);
                            corsConfig.addAllowedHeader("*");
                            corsConfig.applyPermitDefaultValues();
                            corsConfig.addAllowedMethod("DELETE");
                            corsConfig.addAllowedMethod("PUT");
                            corsConfig.addAllowedMethod("PATCH");
                            corsConfig.addAllowedMethod("OPTIONS");
                            corsConfig.addAllowedMethod("HEAD");
                            corsConfig.addAllowedMethod("TRACE");
                            corsConfig.addAllowedMethod("CONNECT");
                            return corsConfig;
                        })

                )
                .httpBasic(Customizer.withDefaults())
                .build();
    }

    @Configuration
    public class WebConfig implements WebMvcConfigurer {
        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            registry.addResourceHandler("/**")
                    .addResourceLocations("classpath:/static/")
                    .setCacheControl(CacheControl.maxAge(1, TimeUnit.DAYS));
        }
    }
}
