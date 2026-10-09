package io.github.rodrigodsantos.upleveling.dashboard;

import com.jayway.jsonpath.JsonPath;
import io.github.rodrigodsantos.upleveling.ApiTest;
import io.github.rodrigodsantos.upleveling.TestClockConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Hoje é quarta, 07/10/2026 (MutableClock).
 */
class DashboardControllerTest extends ApiTest {

    private static final LocalDate TODAY = TestClockConfiguration.TODAY;
    private static final LocalDate YESTERDAY = TODAY.minusDays(1);

    private String ana;

    @BeforeEach
    void loginAsAna() throws Exception {
        ana = registerAndLogin("Ana", "ana@mail.com");
    }

    @Test
    void todayShowsActiveHabitsScheduledForTheDayWithTheirProgress() throws Exception {
        Long ler = habit(ana, "Ler", 30, 1, "[]");
        Long agua = habit(ana, "Beber água", 5, 3, "[]");
        habit(ana, "Academia", 20, 1, "[\"MONDAY\", \"FRIDAY\"]");   // não vale na quarta
        Long meditar = habit(ana, "Meditar", 10, 1, "[]");
        pause(meditar);                                              // pausado não aparece

        checkIn(ana, ler, YESTERDAY);
        checkIn(ana, ler, null);
        checkIn(ana, agua, null);
        checkIn(ana, agua, null);

        fetch(ana, "/api/today")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2026-10-07"))
                .andExpect(jsonPath("$.dayOfWeek").value("WEDNESDAY"))
                .andExpect(jsonPath("$.habits.length()").value(2))
                .andExpect(jsonPath("$.habits[0].name").value("Beber água"))
                .andExpect(jsonPath("$.habits[0].count").value(2))
                .andExpect(jsonPath("$.habits[0].dailyTarget").value(3))
                .andExpect(jsonPath("$.habits[0].completed").value(false))
                .andExpect(jsonPath("$.habits[0].streak").value(0))
                .andExpect(jsonPath("$.habits[1].name").value("Ler"))
                .andExpect(jsonPath("$.habits[1].completed").value(true))
                .andExpect(jsonPath("$.habits[1].streak").value(2))
                .andExpect(jsonPath("$.completedCount").value(1))
                .andExpect(jsonPath("$.totalCount").value(2))
                .andExpect(jsonPath("$.xpEarnedToday").value(30 + 2 * 5))
                .andExpect(jsonPath("$.progress.totalXp").value(30 + 30 + 2 * 5));
    }

    @Test
    void todayAcceptsYesterdayButNothingOlder() throws Exception {
        fetch(ana, "/api/today?date=" + YESTERDAY)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dayOfWeek").value("TUESDAY"));

        fetch(ana, "/api/today?date=" + TODAY.minusDays(2)).andExpect(status().isUnprocessableContent());
    }

    @Test
    void historyIsNewestFirstWithHabitNameAndPagination() throws Exception {
        Long ler = habit(ana, "Ler", 30, 1, "[]");
        Long agua = habit(ana, "Beber água", 5, 3, "[]");
        checkIn(ana, ler, YESTERDAY);
        checkIn(ana, agua, null);
        checkIn(ana, ler, null);

        fetch(ana, "/api/check-ins?size=2")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].habitName").value("Ler"))
                .andExpect(jsonPath("$.content[0].date").value("2026-10-07"))
                .andExpect(jsonPath("$.content[1].habitName").value("Beber água"))
                .andExpect(jsonPath("$.page.totalElements").value(3))
                .andExpect(jsonPath("$.page.totalPages").value(2));

        fetch(ana, "/api/check-ins?habitId=" + ler)
                .andExpect(jsonPath("$.page.totalElements").value(2));
        fetch(ana, "/api/check-ins?from=" + TODAY + "&to=" + TODAY)
                .andExpect(jsonPath("$.page.totalElements").value(2));
    }

    @Test
    void historyKeepsTheNameOfDeletedHabits() throws Exception {
        Long ler = habit(ana, "Ler", 30, 1, "[]");
        checkIn(ana, ler, null);
        mockMvc.perform(delete("/api/habits/{id}", ler).header(HttpHeaders.AUTHORIZATION, ana));

        fetch(ana, "/api/check-ins")
                .andExpect(jsonPath("$.content[0].habitName").value("Ler"))
                .andExpect(jsonPath("$.content[0].xp").value(30));
    }

    @Test
    void historyRejectsAnInvertedRange() throws Exception {
        fetch(ana, "/api/check-ins?from=" + TODAY + "&to=" + YESTERDAY)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("A data inicial (2026-10-07) não pode ser depois da final (2026-10-06)"));
    }

    @Test
    void dailyHistoryGroupsTheCheckInsOfEachHabitWithinTheDay() throws Exception {
        Long ler = habit(ana, "Ler", 30, 1, "[]");
        Long agua = habit(ana, "Beber água", 5, 3, "[]");
        checkIn(ana, ler, YESTERDAY);
        checkIn(ana, agua, null);
        checkIn(ana, ler, null);
        checkIn(ana, agua, null);   // a água tem o check-in mais recente de hoje: vem primeiro
        checkIn(ana, agua, null);

        fetch(ana, "/api/check-ins/daily")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].date").value("2026-10-07"))
                .andExpect(jsonPath("$.content[0].xp").value(3 * 5 + 30))
                .andExpect(jsonPath("$.content[0].habits.length()").value(2))
                .andExpect(jsonPath("$.content[0].habits[0].habitName").value("Beber água"))
                .andExpect(jsonPath("$.content[0].habits[0].count").value(3))
                .andExpect(jsonPath("$.content[0].habits[0].xp").value(15))
                .andExpect(jsonPath("$.content[0].habits[0].bonusXp").value(0))
                .andExpect(jsonPath("$.content[0].habits[1].habitName").value("Ler"))
                .andExpect(jsonPath("$.content[0].habits[1].count").value(1))
                .andExpect(jsonPath("$.content[1].date").value("2026-10-06"))
                .andExpect(jsonPath("$.content[1].habits[0].habitName").value("Ler"))
                .andExpect(jsonPath("$.page.totalElements").value(2));   // 2 dias, não 5 check-ins
    }

    @Test
    void dailyHistoryPaginatesByDaySoADayIsNeverSplit() throws Exception {
        Long agua = habit(ana, "Beber água", 5, 3, "[]");
        for (int i = 0; i < 3; i++) {
            checkIn(ana, agua, YESTERDAY);
            checkIn(ana, agua, null);
        }

        // 1 dia por página: a página 0 tem hoje inteiro (3 check-ins), a 1 tem ontem inteiro
        fetch(ana, "/api/check-ins/daily?size=1")
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].date").value("2026-10-07"))
                .andExpect(jsonPath("$.content[0].habits[0].count").value(3))
                .andExpect(jsonPath("$.page.totalPages").value(2));
        fetch(ana, "/api/check-ins/daily?size=1&page=1")
                .andExpect(jsonPath("$.content[0].date").value("2026-10-06"))
                .andExpect(jsonPath("$.content[0].habits[0].count").value(3));
    }

    @Test
    void dailyHistoryFiltersByHabitAndPeriodAndKeepsDeletedHabits() throws Exception {
        Long ler = habit(ana, "Ler", 30, 1, "[]");
        Long agua = habit(ana, "Beber água", 5, 3, "[]");
        checkIn(ana, ler, YESTERDAY);
        checkIn(ana, agua, null);
        checkIn(ana, ler, null);
        mockMvc.perform(delete("/api/habits/{id}", ler).header(HttpHeaders.AUTHORIZATION, ana));

        fetch(ana, "/api/check-ins/daily?habitId=" + ler)
                .andExpect(jsonPath("$.page.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].habits.length()").value(1))
                .andExpect(jsonPath("$.content[0].habits[0].habitName").value("Ler"))
                .andExpect(jsonPath("$.content[0].xp").value(30));   // a água do mesmo dia não entra na soma
        fetch(ana, "/api/check-ins/daily?habitId=" + agua)
                .andExpect(jsonPath("$.page.totalElements").value(1));
        fetch(ana, "/api/check-ins/daily?from=" + YESTERDAY + "&to=" + YESTERDAY)
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].date").value("2026-10-06"));
    }

    @Test
    void dailyHistoryLimitsThePageSizeAndRejectsAnInvertedRange() throws Exception {
        fetch(ana, "/api/check-ins/daily?size=500")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.page.size").value(31));
        fetch(ana, "/api/check-ins/daily?from=" + TODAY + "&to=" + YESTERDAY)
                .andExpect(status().isBadRequest());
    }

    @Test
    void xpHistoryFillsDaysWithoutCheckInWithZero() throws Exception {
        Long ler = habit(ana, "Ler", 30, 1, "[]");
        checkIn(ana, ler, YESTERDAY);
        checkIn(ana, ler, null);

        fetch(ana, "/api/me/xp-history?from=" + TODAY.minusDays(3) + "&to=" + TODAY)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].date").value("2026-10-04"))
                .andExpect(jsonPath("$[0].xp").value(0))
                .andExpect(jsonPath("$[1].xp").value(0))
                .andExpect(jsonPath("$[2].xp").value(30))
                .andExpect(jsonPath("$[3].date").value("2026-10-07"))
                .andExpect(jsonPath("$[3].xp").value(30));
    }

    @Test
    void xpHistoryDefaultsToTheLast30DaysAndLimitsTheRange() throws Exception {
        fetch(ana, "/api/me/xp-history")
                .andExpect(jsonPath("$.length()").value(30))
                .andExpect(jsonPath("$[29].date").value("2026-10-07"));

        fetch(ana, "/api/me/xp-history?from=" + TODAY.minusDays(366) + "&to=" + TODAY)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("O período pode ter no máximo 366 dias"));
    }

    @Test
    void eachUserSeesOnlyTheirOwnData() throws Exception {
        String bruno = registerAndLogin("Bruno", "bruno@mail.com");
        Long habitoDoBruno = habit(bruno, "Correr", 50, 1, "[]");
        checkIn(bruno, habitoDoBruno, null);

        fetch(ana, "/api/today")
                .andExpect(jsonPath("$.habits").isEmpty())
                .andExpect(jsonPath("$.xpEarnedToday").value(0));
        fetch(ana, "/api/check-ins").andExpect(jsonPath("$.page.totalElements").value(0));
        fetch(ana, "/api/check-ins?habitId=" + habitoDoBruno).andExpect(jsonPath("$.page.totalElements").value(0));
        fetch(ana, "/api/check-ins/daily").andExpect(jsonPath("$.page.totalElements").value(0));
        fetch(ana, "/api/check-ins/daily?habitId=" + habitoDoBruno).andExpect(jsonPath("$.page.totalElements").value(0));
        fetch(ana, "/api/me/xp-history?from=" + TODAY + "&to=" + TODAY).andExpect(jsonPath("$[0].xp").value(0));
    }

    // ---------- atalhos ----------

    private ResultActions fetch(String token, String url) throws Exception {
        return mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, token));
    }

    private Long habit(String token, String name, int xpReward, int dailyTarget, String days) throws Exception {
        String body = mockMvc.perform(post("/api/habits")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "%s", "xpReward": %d, "dailyTarget": %d, "days": %s}
                                """.formatted(name, xpReward, dailyTarget, days)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }

    private void pause(Long habitId) throws Exception {
        mockMvc.perform(patch("/api/habits/{id}/status", habitId)
                        .header(HttpHeaders.AUTHORIZATION, ana)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "PAUSED"}
                                """))
                .andExpect(status().isOk());
    }

    private void checkIn(String token, Long habitId, LocalDate date) throws Exception {
        MockHttpServletRequestBuilder request = post("/api/habits/{id}/check-ins", habitId)
                .header(HttpHeaders.AUTHORIZATION, token);
        if (date != null) {
            request.contentType(MediaType.APPLICATION_JSON).content("""
                    {"date": "%s"}
                    """.formatted(date));
        }
        mockMvc.perform(request).andExpect(status().isCreated());
    }
}
