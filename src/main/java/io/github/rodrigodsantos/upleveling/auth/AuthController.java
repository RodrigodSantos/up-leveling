package io.github.rodrigodsantos.upleveling.auth;

import io.github.rodrigodsantos.upleveling.auth.dto.LoginRequest;
import io.github.rodrigodsantos.upleveling.auth.dto.RegisterRequest;
import io.github.rodrigodsantos.upleveling.auth.dto.TokenResponse;
import io.github.rodrigodsantos.upleveling.user.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticação")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirements // rota pública: sem cadeado no Swagger
    @Operation(summary = "Cria uma conta")
    public UserResponse register(@RequestBody @Valid RegisterRequest request) {
        return service.register(request);
    }

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(summary = "Faz login e devolve o token JWT")
    public TokenResponse login(@RequestBody @Valid LoginRequest request) {
        return service.login(request);
    }
}
