package hu.benkototh.cardgame.backend.config;

import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.function.support.RouterFunctionMapping;

@Configuration
public class OpenApiConfig {

    @Bean
    @ConditionalOnMissingBean(name = "delegatingHandlerMapping")
    public HandlerMapping delegatingHandlerMapping() {
        return new RouterFunctionMapping();
    }

    @Bean
    public OpenAPI cardGameOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Card Game API")
                        .description("API for the Card Game application")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Card Game Support")
                                .email("cardgame.noreply@gmail.com")
                                .url("https://cardhub-ff690e54a14d.herokuapp.com/support"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")));
    }
}