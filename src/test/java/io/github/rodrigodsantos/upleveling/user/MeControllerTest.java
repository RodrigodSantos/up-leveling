package io.github.rodrigodsantos.upleveling.user;

import io.github.rodrigodsantos.upleveling.ApiTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MeControllerTest extends ApiTest {

    @Test
    void updateProfileChangesNameAndEmail() throws Exception {
        String token = registerAndLogin("Ana", "ana@mail.com");

        updateProfile(token, """
                {"name": "Ana Souza", "email": "ANA.SOUZA@mail.com"}
                """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ana Souza"))
                .andExpect(jsonPath("$.email").value("ana.souza@mail.com"));

        login("ana.souza@mail.com", PASSWORD).andExpect(status().isOk());
    }

    @Test
    void updateProfileKeepsOwnEmailButRejectsAnotherAccountEmail() throws Exception {
        registerAndLogin("Bruno", "bruno@mail.com");
        String token = registerAndLogin("Ana", "ana@mail.com");

        updateProfile(token, """
                {"name": "Ana Souza", "email": "ana@mail.com"}
                """)
                .andExpect(status().isOk());

        updateProfile(token, """
                {"name": "Ana", "email": "bruno@mail.com"}
                """)
                .andExpect(status().isConflict());
    }

    /** No projeto antigo, mandar só um dos campos derrubava a API com NullPointerException (500). */
    @Test
    void updateProfileRequiresBothFields() throws Exception {
        String token = registerAndLogin("Ana", "ana@mail.com");

        updateProfile(token, """
                {"email": "novo@mail.com"}
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.name").value("é obrigatório"));
    }

    @Test
    void changePasswordRequiresCurrentPassword() throws Exception {
        String token = registerAndLogin("Ana", "ana@mail.com");

        changePassword(token, "senha-errada", "nova-senha-456")
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value("A senha atual está incorreta"));

        changePassword(token, PASSWORD, "nova-senha-456").andExpect(status().isNoContent());

        login("ana@mail.com", PASSWORD).andExpect(status().isUnauthorized());
        login("ana@mail.com", "nova-senha-456").andExpect(status().isOk());
    }

    @Test
    void accentColorsStartEmptyAndAreReplacedInTheOrderSent() throws Exception {
        String token = registerAndLogin("Ana", "ana@mail.com");

        accentColors(token)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hues.length()").value(0));

        replaceAccentColors(token, "[330, 200]")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hues[0]").value(330))
                .andExpect(jsonPath("$.hues[1]").value(200));

        // Tirar uma cor = mandar a lista sem ela
        replaceAccentColors(token, "[200]").andExpect(status().isOk());
        accentColors(token)
                .andExpect(jsonPath("$.hues.length()").value(1))
                .andExpect(jsonPath("$.hues[0]").value(200));
    }

    @Test
    void accentColorsAreSeparatedPerUser() throws Exception {
        String bruno = registerAndLogin("Bruno", "bruno@mail.com");
        String ana = registerAndLogin("Ana", "ana@mail.com");

        replaceAccentColors(ana, "[120]").andExpect(status().isOk());

        accentColors(bruno).andExpect(jsonPath("$.hues.length()").value(0));
    }

    @Test
    void accentColorsAcceptAtMostFiveValidHuesWithoutRepetition() throws Exception {
        String token = registerAndLogin("Ana", "ana@mail.com");

        replaceAccentColors(token, "[10, 60, 120, 180, 240, 300]")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.hues").value("pode ter no máximo 5 cores"));

        replaceAccentColors(token, "[360]")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields['hues[0]']").value("cada cor deve ser uma matiz de 0 a 359"));

        replaceAccentColors(token, "[200, 200]")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("A mesma cor aparece mais de uma vez"));

        // Nenhuma das tentativas inválidas gravou nada
        accentColors(token).andExpect(jsonPath("$.hues.length()").value(0));
    }

    private ResultActions accentColors(String token) throws Exception {
        return mockMvc.perform(get("/api/me/accent-colors").header(HttpHeaders.AUTHORIZATION, token));
    }

    private ResultActions replaceAccentColors(String token, String hues) throws Exception {
        return mockMvc.perform(put("/api/me/accent-colors")
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"hues": %s}
                        """.formatted(hues)));
    }

    private ResultActions updateProfile(String token, String json) throws Exception {
        return mockMvc.perform(put("/api/me")
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    private ResultActions changePassword(String token, String current, String newPassword) throws Exception {
        return mockMvc.perform(put("/api/me/password")
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"currentPassword": "%s", "newPassword": "%s"}
                        """.formatted(current, newPassword)));
    }
}
