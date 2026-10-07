package io.github.rodrigodsantos.upleveling.gamification;

import com.jayway.jsonpath.JsonPath;
import io.github.rodrigodsantos.upleveling.ApiTest;
import io.github.rodrigodsantos.upleveling.TestClockConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * "Hoje" é controlado pelo MutableClock: começa na quarta, 07/10/2026, e alguns testes avançam os dias.
 */
class CheckInControllerTest extends ApiTest {

    private static final LocalDate TODAY = TestClockConfiguration.TODAY;

    private String ana;

    @BeforeEach
    void loginAsAna() throws Exception {
        ana = registerAndLogin("Ana", "ana@mail.com");
    }

    @Test
    void checkInGivesXpAndCompletesTheDay() throws Exception {
        Long ler = habit("Ler", 30, 1);

        checkIn(ler, null)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.date").value("2026-10-07"))
                .andExpect(jsonPath("$.xpChange").value(30))
                .andExpect(jsonPath("$.bonusXp").value(0))
                .andExpect(jsonPath("$.dayCount").value(1))
                .andExpect(jsonPath("$.dayCompleted").value(true))
                .andExpect(jsonPath("$.streak").value(1))
                .andExpect(jsonPath("$.leveledUp").value(false))
                .andExpect(jsonPath("$.progress.totalXp").value(30))
                .andExpect(jsonPath("$.progress.level").value(1))
                .andExpect(jsonPath("$.progress.progressPercent").value(30));
    }

    @Test
    void severalCheckInsPerDayUpToTheDailyTarget() throws Exception {
        Long agua = habit("Beber água", 5, 3);

        checkIn(agua, null).andExpect(jsonPath("$.dayCount").value(1)).andExpect(jsonPath("$.dayCompleted").value(false));
        checkIn(agua, null).andExpect(jsonPath("$.dayCount").value(2)).andExpect(jsonPath("$.streak").value(0));
        checkIn(agua, null)
                .andExpect(jsonPath("$.dayCount").value(3))
                .andExpect(jsonPath("$.dayCompleted").value(true))
                .andExpect(jsonPath("$.streak").value(1))
                .andExpect(jsonPath("$.progress.totalXp").value(15));

        checkIn(agua, null)
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value("Meta do dia já atingida (3/3)"));
    }

    @Test
    void checkInOnlyOnScheduledDays() throws Exception {
        Long academia = habit("""
                {"name": "Academia", "xpReward": 20, "days": ["MONDAY", "FRIDAY"]}
                """);

        checkIn(academia, null)
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value("O hábito 'Academia' não está agendado para quarta-feira"));
    }

    @Test
    void retroactiveCheckInOnlyForYesterday() throws Exception {
        Long ler = habit("Ler", 10, 1);

        checkIn(ler, TODAY.minusDays(1))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.date").value("2026-10-06"));
        checkIn(ler, TODAY.minusDays(2))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value("Só é possível usar a data de hoje ou de ontem"));
        checkIn(ler, TODAY.plusDays(1)).andExpect(status().isUnprocessableContent());
    }

    @Test
    void pausedHabitDoesNotAcceptCheckIn() throws Exception {
        Long ler = habit("Ler", 10, 1);
        mockMvc.perform(patch("/api/habits/{id}/status", ler)
                .header(HttpHeaders.AUTHORIZATION, ana)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"status": "PAUSED"}
                        """));

        checkIn(ler, null)
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value("O hábito 'Ler' está pausado. Reative-o para fazer check-in"));
    }

    @Test
    void undoRemovesTheLastCheckInAndGivesXpBack() throws Exception {
        Long agua = habit("Beber água", 5, 3);
        checkIn(agua, null);
        checkIn(agua, null);

        undo(agua, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.xpChange").value(-5))
                .andExpect(jsonPath("$.dayCount").value(1))
                .andExpect(jsonPath("$.progress.totalXp").value(5));
        undo(agua, null).andExpect(jsonPath("$.dayCount").value(0));

        undo(agua, null)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Nenhum check-in de 'Beber água' em 07/10/2026"));
    }

    @Test
    void reachingTheXpOfTheNextLevelLevelsUp() throws Exception {
        Long maratona = habit("Treino longo", 100, 1);

        checkIn(maratona, null)
                .andExpect(jsonPath("$.leveledUp").value(true))
                .andExpect(jsonPath("$.progress.level").value(2))
                .andExpect(jsonPath("$.progress.currentLevelXp").value(100))
                .andExpect(jsonPath("$.progress.nextLevelXp").value(300))
                .andExpect(jsonPath("$.progress.progressPercent").value(0));
    }

    @Test
    void seventhDayInARowGivesTheStreakBonus() throws Exception {
        Long ler = habit("Ler", 20, 1);

        for (int day = 0; day < 6; day++) {
            clock.setToday(TODAY.plusDays(day));
            checkIn(ler, null)
                    .andExpect(jsonPath("$.streak").value(day + 1))
                    .andExpect(jsonPath("$.bonusXp").value(0));
        }

        clock.setToday(TODAY.plusDays(6));
        checkIn(ler, null)
                .andExpect(jsonPath("$.streak").value(7))
                .andExpect(jsonPath("$.bonusXp").value(10))
                .andExpect(jsonPath("$.xpChange").value(30))
                .andExpect(jsonPath("$.progress.totalXp").value(7 * 20 + 10));

        // Desfazer o 7º dia leva o bônus junto
        undo(ler, null)
                .andExpect(jsonPath("$.xpChange").value(-30))
                .andExpect(jsonPath("$.progress.totalXp").value(6 * 20));
    }

    @Test
    void anotherUsersHabitAnswers404() throws Exception {
        String bruno = registerAndLogin("Bruno", "bruno@mail.com");
        Long habitoDoBruno = habitOf(bruno, """
                {"name": "Ler", "xpReward": 10}
                """);

        checkIn(habitoDoBruno, null).andExpect(status().isNotFound());
        undo(habitoDoBruno, null).andExpect(status().isNotFound());
    }

    @Test
    void xpFromDeletedHabitsIsKept() throws Exception {
        Long ler = habit("Ler", 40, 1);
        checkIn(ler, null);
        mockMvc.perform(delete("/api/habits/{id}", ler).header(HttpHeaders.AUTHORIZATION, ana));

        mockMvc.perform(get("/api/me/progress").header(HttpHeaders.AUTHORIZATION, ana))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalXp").value(40));
    }

    @Test
    void progressStartsAtLevelOne() throws Exception {
        mockMvc.perform(get("/api/me/progress").header(HttpHeaders.AUTHORIZATION, ana))
                .andExpect(jsonPath("$.totalXp").value(0))
                .andExpect(jsonPath("$.level").value(1))
                .andExpect(jsonPath("$.nextLevelXp").value(100))
                .andExpect(jsonPath("$.progressPercent").value(0));
    }

    // ---------- atalhos ----------

    private Long habit(String name, int xpReward, int dailyTarget) throws Exception {
        return habit("""
                {"name": "%s", "xpReward": %d, "dailyTarget": %d}
                """.formatted(name, xpReward, dailyTarget));
    }

    private Long habit(String json) throws Exception {
        return habitOf(ana, json);
    }

    private Long habitOf(String token, String json) throws Exception {
        String body = mockMvc.perform(post("/api/habits")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }

    private ResultActions checkIn(Long habitId, LocalDate date) throws Exception {
        var request = post("/api/habits/{id}/check-ins", habitId).header(HttpHeaders.AUTHORIZATION, ana);
        if (date != null) {
            request.contentType(MediaType.APPLICATION_JSON).content("""
                    {"date": "%s"}
                    """.formatted(date));
        }
        return mockMvc.perform(request);
    }

    private ResultActions undo(Long habitId, LocalDate date) throws Exception {
        var request = delete("/api/habits/{id}/check-ins", habitId).header(HttpHeaders.AUTHORIZATION, ana);
        if (date != null) {
            request.param("date", date.toString());
        }
        return mockMvc.perform(request);
    }
}
