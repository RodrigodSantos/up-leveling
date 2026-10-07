package io.github.rodrigodsantos.upleveling.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Up Leveling API")
                        .description("Hábitos gamificados: cumpra seus hábitos, ganhe XP e suba de nível.")
                        .version("0.0.1"));
    }
}
