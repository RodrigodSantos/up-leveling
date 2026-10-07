package io.github.rodrigodsantos.upleveling.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import tools.jackson.databind.ObjectMapper;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
public class SecurityConfig {

    private static final String[] PUBLIC_DOCS = {
            "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/actuator/health"
    };

    private final SecretKey key;

    public SecurityConfig(@Value("${upleveling.jwt.secret}") String secret) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("upleveling.jwt.secret precisa ter no mínimo 32 caracteres (HS256)");
        }
        this.key = new SecretKeySpec(bytes, "HmacSHA256");
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationEntryPoint unauthorized) throws Exception {
        return http
                // API stateless com token no header: não usa sessão nem cookie, então CSRF não se aplica
                .csrf(AbstractHttpConfigurer::disable)
                // Usa o CorsConfigurationSource do CorsConfig; precisa vir antes da autenticação,
                // porque o navegador manda o "preflight" (OPTIONS) sem token
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/").permitAll()
                        .requestMatchers(PUBLIC_DOCS).permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth -> oauth
                        .jwt(jwt -> jwt.decoder(jwtDecoder()))
                        .authenticationEntryPoint(unauthorized))
                .exceptionHandling(e -> e.authenticationEntryPoint(unauthorized))
                .build();
    }

    /** Responde 401 no mesmo formato RFC 9457 do resto da API (o filtro de segurança roda antes do controller advice). */
    @Bean
    public AuthenticationEntryPoint unauthorized(ObjectMapper objectMapper) {
        return (request, response, ex) -> {
            ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED,
                    "Token ausente, inválido ou expirado");
            problem.setTitle("Não autenticado");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            objectMapper.writeValue(response.getOutputStream(), problem);
        };
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        return new NimbusJwtEncoder(new ImmutableSecret<>(key));
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
    }

    /**
     * A força é o custo do hash: cada +1 dobra o tempo. 10 (padrão) em produção, para dificultar ataques de força
     * bruta; os testes usam 4, porque fazem centenas de cadastros e logins e ali a segurança não importa.
     */
    @Bean
    public PasswordEncoder passwordEncoder(@Value("${upleveling.bcrypt-strength:10}") int strength) {
        return new BCryptPasswordEncoder(strength);
    }
}
