package io.github.rodrigodsantos.upleveling.user;

import io.github.rodrigodsantos.upleveling.user.dto.ChangePasswordRequest;
import io.github.rodrigodsantos.upleveling.user.dto.UpdateProfileRequest;
import io.github.rodrigodsantos.upleveling.user.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * "/me" em vez de "/users/{id}": o id vem do token, então ninguém consegue pedir os dados de outra pessoa.
 */
@RestController
@RequestMapping("/api/me")
@Tag(name = "Minha conta")
public class MeController {

    private final UserService service;

    public MeController(UserService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Dados do usuário logado")
    public UserResponse me() {
        return service.me();
    }

    @PutMapping
    @Operation(summary = "Atualiza nome e e-mail")
    public UserResponse updateProfile(@RequestBody @Valid UpdateProfileRequest request) {
        return service.updateProfile(request);
    }

    @PutMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Troca a senha (exige a senha atual)")
    public void changePassword(@RequestBody @Valid ChangePasswordRequest request) {
        service.changePassword(request);
    }
}
