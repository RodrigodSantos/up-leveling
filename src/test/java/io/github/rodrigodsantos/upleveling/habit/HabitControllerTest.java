package io.github.rodrigodsantos.upleveling.habit;

import com.jayway.jsonpath.JsonPath;
import io.github.rodrigodsantos.upleveling.ApiTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.matchesRegex;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class HabitControllerTest extends ApiTest {

    private String ana;

    @BeforeEach
    void loginAsAna() throws Exception {
        ana = registerAndLogin("Ana", "ana@mail.com");
    }

    @Test
    void createReturns201WithLocationAndOrderedDays() throws Exception {
        create(ana, """
                {"name": "  Ler 20 páginas ", "xpReward": 30, "days": ["FRIDAY", "MONDAY", "WEDNESDAY"]}
                """)
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, matchesRegex(".*/api/habits/\\d+")))
                .andExpect(jsonPath("$.name").value("Ler 20 páginas"))
                .andExpect(jsonPath("$.xpReward").value(30))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.days[0]").value("MONDAY"))
                .andExpect(jsonPath("$.days[1]").value("WEDNESDAY"))
                .andExpect(jsonPath("$.days[2]").value("FRIDAY"));
    }

    @Test
    void createWithoutDaysMeansEveryDay() throws Exception {
        create(ana, """
                {"name": "Beber água", "xpReward": 5}
                """)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.days").isEmpty());
    }

    @Test
    void createValidatesFields() throws Exception {
        create(ana, """
                {"name": " ", "xpReward": 101}
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.name").value("é obrigatório"))
                .andExpect(jsonPath("$.fields.xpReward").value("deve estar entre 1 e 100"));

        create(ana, """
                {"name": "Correr", "xpReward": 10, "days": ["SEGUNDA"]}
                """)
                .andExpect(status().isBadRequest());
    }

    @Test
    void listShowsOnlyOwnHabitsSortedByNameAndFiltersByStatus() throws Exception {
        String bruno = registerAndLogin("Bruno", "bruno@mail.com");
        createId(bruno, "Hábito do Bruno");
        createId(ana, "Meditar");
        Long ler = createId(ana, "Ler");
        changeStatus(ana, ler, "PAUSED");

        mockMvc.perform(get("/api/habits").header(HttpHeaders.AUTHORIZATION, ana))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Ler"))
                .andExpect(jsonPath("$[1].name").value("Meditar"));

        mockMvc.perform(get("/api/habits").param("status", "PAUSED").header(HttpHeaders.AUTHORIZATION, ana))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Ler"));
    }

    @Test
    void anotherUsersHabitAnswers404Everywhere() throws Exception {
        String bruno = registerAndLogin("Bruno", "bruno@mail.com");
        Long habitDoBruno = createId(bruno, "Hábito do Bruno");

        mockMvc.perform(get("/api/habits/{id}", habitDoBruno).header(HttpHeaders.AUTHORIZATION, ana))
                .andExpect(status().isNotFound());
        update(ana, habitDoBruno, """
                {"name": "Invadido", "xpReward": 1}
                """)
                .andExpect(status().isNotFound());
        changeStatus(ana, habitDoBruno, "PAUSED").andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/habits/{id}", habitDoBruno).header(HttpHeaders.AUTHORIZATION, ana))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/habits/{id}", habitDoBruno).header(HttpHeaders.AUTHORIZATION, bruno))
                .andExpect(jsonPath("$.name").value("Hábito do Bruno"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void nameIsUniquePerUserIgnoringCaseButFreedAfterDelete() throws Exception {
        Long ler = createId(ana, "Ler");

        create(ana, """
                {"name": "LER", "xpReward": 10}
                """)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Você já tem um hábito chamado 'LER'"));

        // Outro usuário pode ter um hábito com o mesmo nome
        createId(registerAndLogin("Bruno", "bruno@mail.com"), "Ler");

        mockMvc.perform(delete("/api/habits/{id}", ler).header(HttpHeaders.AUTHORIZATION, ana))
                .andExpect(status().isNoContent());
        create(ana, """
                {"name": "Ler", "xpReward": 10}
                """)
                .andExpect(status().isCreated());
    }

    @Test
    void updateReplacesNameXpAndDays() throws Exception {
        Long id = createId(ana, "Ler");
        createId(ana, "Meditar");

        update(ana, id, """
                {"name": "Ler 30 páginas", "xpReward": 40, "days": ["SATURDAY", "SUNDAY"]}
                """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ler 30 páginas"))
                .andExpect(jsonPath("$.xpReward").value(40))
                .andExpect(jsonPath("$.days.length()").value(2))
                .andExpect(jsonPath("$.days[0]").value("SATURDAY"));

        // Manter o próprio nome é permitido; usar o nome de outro hábito meu, não
        update(ana, id, """
                {"name": "Ler 30 páginas", "xpReward": 50}
                """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.days").isEmpty());
        update(ana, id, """
                {"name": "meditar", "xpReward": 50}
                """)
                .andExpect(status().isConflict());
    }

    @Test
    void pauseAndResumeAreIdempotent() throws Exception {
        Long id = createId(ana, "Ler");

        changeStatus(ana, id, "PAUSED").andExpect(status().isOk()).andExpect(jsonPath("$.status").value("PAUSED"));
        changeStatus(ana, id, "PAUSED").andExpect(status().isOk()).andExpect(jsonPath("$.status").value("PAUSED"));
        changeStatus(ana, id, "ACTIVE").andExpect(status().isOk()).andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void statusCannotBeSetToDeleted() throws Exception {
        Long id = createId(ana, "Ler");

        changeStatus(ana, id, "DELETED")
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value("Para excluir um hábito, use DELETE /api/habits/{id}"));
    }

    @Test
    void deletedHabitDisappears() throws Exception {
        Long id = createId(ana, "Ler");

        mockMvc.perform(delete("/api/habits/{id}", id).header(HttpHeaders.AUTHORIZATION, ana))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/habits").header(HttpHeaders.AUTHORIZATION, ana))
                .andExpect(jsonPath("$").isEmpty());
        mockMvc.perform(get("/api/habits/{id}", id).header(HttpHeaders.AUTHORIZATION, ana))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/habits/{id}", id).header(HttpHeaders.AUTHORIZATION, ana))
                .andExpect(status().isNotFound());
    }

    // ---------- atalhos ----------

    private Long lastId;

    private ResultActions create(String token, String json) throws Exception {
        ResultActions result = mockMvc.perform(post("/api/habits")
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
        String body = result.andReturn().getResponse().getContentAsString();
        if (result.andReturn().getResponse().getStatus() == 201) {
            lastId = ((Number) JsonPath.read(body, "$.id")).longValue();
        }
        return result;
    }

    private Long createId(String token, String name) throws Exception {
        create(token, """
                {"name": "%s", "xpReward": 10}
                """.formatted(name));
        return lastId;
    }

    private ResultActions update(String token, Long id, String json) throws Exception {
        return mockMvc.perform(put("/api/habits/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    private ResultActions changeStatus(String token, Long id, String status) throws Exception {
        return mockMvc.perform(patch("/api/habits/{id}/status", id)
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"status": "%s"}
                        """.formatted(status)));
    }
}
