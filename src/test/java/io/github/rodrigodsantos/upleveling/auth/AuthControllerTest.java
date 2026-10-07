package io.github.rodrigodsantos.upleveling.auth;

import io.github.rodrigodsantos.upleveling.ApiTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Fluxo real de ponta a ponta: cadastro → login → token no header. Sem atalhos de autenticação.
 */
class AuthControllerTest extends ApiTest {

    @Test
    void registerCreatesAccountWithoutExposingPassword() throws Exception {
        register("Ana", "  Ana@Mail.com ", PASSWORD)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").value("ana@mail.com"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void registerRejectsEmailAlreadyUsedIgnoringCase() throws Exception {
        register("Ana", "ana@mail.com", PASSWORD).andExpect(status().isCreated());

        register("Outra Ana", "ANA@mail.com", PASSWORD)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Já existe uma conta com o e-mail 'ana@mail.com'"));
    }

    @Test
    void registerValidatesFields() throws Exception {
        register("", "nao-e-email", "curta")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Erro de validação"))
                .andExpect(jsonPath("$.fields.name").value("é obrigatório"))
                .andExpect(jsonPath("$.fields.email").value("deve ser um e-mail válido"))
                .andExpect(jsonPath("$.fields.password").value("deve ter entre 8 e 72 caracteres"));
    }

    @Test
    void passwordAcceptsUpTo72Characters() throws Exception {
        register("Ana", "ana@mail.com", "a".repeat(72)).andExpect(status().isCreated());
        register("Bia", "bia@mail.com", "a".repeat(73)).andExpect(status().isBadRequest());
    }

    @Test
    void loginReturnsTokenThatOpensProtectedRoutes() throws Exception {
        register("Ana", "ana@mail.com", PASSWORD);

        login("ANA@mail.com", PASSWORD)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.expiresAt").exists());

        mockMvc.perform(get("/api/me").header(HttpHeaders.AUTHORIZATION, bearer("ana@mail.com", PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ana"));
    }

    @Test
    void loginGivesSameAnswerForWrongPasswordAndUnknownEmail() throws Exception {
        register("Ana", "ana@mail.com", PASSWORD);

        login("ana@mail.com", "senha-errada")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("E-mail ou senha inválidos"));

        login("ninguem@mail.com", PASSWORD)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("E-mail ou senha inválidos"));
    }

    @Test
    void protectedRoutesRequireValidToken() throws Exception {
        mockMvc.perform(get("/api/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Não autenticado"));

        mockMvc.perform(get("/api/me").header(HttpHeaders.AUTHORIZATION, "Bearer token-falso"))
                .andExpect(status().isUnauthorized());
    }
}
