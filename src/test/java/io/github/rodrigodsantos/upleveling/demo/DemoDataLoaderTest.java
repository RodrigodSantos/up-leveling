package io.github.rodrigodsantos.upleveling.demo;

import com.jayway.jsonpath.JsonPath;
import io.github.rodrigodsantos.upleveling.ApiTest;
import io.github.rodrigodsantos.upleveling.friendship.FriendshipRepository;
import io.github.rodrigodsantos.upleveling.gamification.CheckInRepository;
import io.github.rodrigodsantos.upleveling.habit.HabitRepository;
import io.github.rodrigodsantos.upleveling.shared.DemoAccount;
import io.github.rodrigodsantos.upleveling.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * O loader só existe com o perfil "demo"; aqui ele é criado na mão, com os beans do contexto de teste.
 */
class DemoDataLoaderTest extends ApiTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private HabitRepository habitRepository;
    @Autowired
    private CheckInRepository checkInRepository;
    @Autowired
    private FriendshipRepository friendshipRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private DemoDataLoader loader;

    @BeforeEach
    void createLoader() {
        loader = new DemoDataLoader(userRepository, habitRepository, checkInRepository, friendshipRepository,
                passwordEncoder, clock);
    }

    @Test
    void demoAccountHasDataToShow() throws Exception {
        loader.reset();
        String token = bearer(DemoAccount.EMAIL, DemoAccount.PASSWORD);

        // Quarta-feira: os 5 hábitos valem hoje (Academia é seg/qua/sex, Estudar Java é seg a sex)
        mockMvc.perform(get("/api/today").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(5))
                .andExpect(jsonPath("$.completedCount").value(1))
                .andExpect(jsonPath("$.progress.level", greaterThan(3)));

        mockMvc.perform(get("/api/me/xp-history").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(jsonPath("$.length()").value(30));
    }

    @Test
    void resetIsRepeatableAndDoesNotDuplicateData() throws Exception {
        loader.reset();
        String token = bearer(DemoAccount.EMAIL, DemoAccount.PASSWORD);
        long xp = totalXp(token);

        loader.reset();

        assertThat(totalXp(token)).isEqualTo(xp);
        Long demoId = userRepository.findByEmail(DemoAccount.EMAIL).orElseThrow().getId();
        assertThat(habitRepository.findByUserId(demoId)).hasSize(5);
    }

    @Test
    void demoAccountShowsSavedAccentColorsButVisitorsCannotChangeThem() throws Exception {
        loader.reset();
        String token = bearer(DemoAccount.EMAIL, DemoAccount.PASSWORD);

        mockMvc.perform(get("/api/me/accent-colors").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hues[0]").value(200))
                .andExpect(jsonPath("$.hues[1]").value(330));

        mockMvc.perform(put("/api/me/accent-colors")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"hues": []}
                                """))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value("A conta de demonstração não pode alterar as cores salvas"));
    }

    @Test
    void visitorsCannotLockTheDemoAccount() throws Exception {
        loader.reset();
        String token = bearer(DemoAccount.EMAIL, DemoAccount.PASSWORD);

        mockMvc.perform(put("/api/me/password")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"currentPassword": "demo1234", "newPassword": "outra-senha-123"}
                                """))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value("A conta de demonstração não pode ter o e-mail nem a senha alterados"));

        mockMvc.perform(put("/api/me")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Invasor", "email": "invasor@mail.com"}
                                """))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void demoAccountShowsFriendsButVisitorsCannotChangeThem() throws Exception {
        loader.reset();
        loader.reset(); // recriar não duplica os amigos
        String token = bearer(DemoAccount.EMAIL, DemoAccount.PASSWORD);

        mockMvc.perform(get("/api/friends").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].name").value("Ana Lima"))
                .andExpect(jsonPath("$[0].progress.totalXp", greaterThan(0)))
                .andExpect(jsonPath("$[1].name").value("Bruno Costa"))
                .andExpect(jsonPath("$[2].name").value("Jardiel Carlos"));

        Long anaId = userRepository.findByEmail("ana.demo@upleveling.local").orElseThrow().getId();
        mockMvc.perform(delete("/api/friends/" + anaId).header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value("A conta de demonstração não pode alterar amizades"));

        // Ninguém entra nas contas dos amigos fictícios
        login("ana.demo@upleveling.local", "").andExpect(status().isBadRequest());
        login("ana.demo@upleveling.local", "qualquer-senha").andExpect(status().isUnauthorized());
    }

    private long totalXp(String token) throws Exception {
        String body = mockMvc.perform(get("/api/me/progress").header(HttpHeaders.AUTHORIZATION, token))
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.totalXp")).longValue();
    }
}
