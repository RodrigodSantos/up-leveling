package io.github.rodrigodsantos.upleveling.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BEARER = "bearer-jwt";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Up Leveling API")
                        .description("""
                                Hábitos gamificados: cumpra seus hábitos, ganhe XP e suba de nível.

                                **Para testar:** crie uma conta em `POST /api/auth/register`, faça login em \
                                `POST /api/auth/login`, copie o `token` e clique em **Authorize**.""")
                        .version("0.0.1"))
                // Cadeado em todas as rotas; as públicas tiram com @SecurityRequirements vazio
                .components(new Components().addSecuritySchemes(BEARER, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER));
    }
}
