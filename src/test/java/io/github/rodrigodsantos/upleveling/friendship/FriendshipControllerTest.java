package io.github.rodrigodsantos.upleveling.friendship;

import com.jayway.jsonpath.JsonPath;
import io.github.rodrigodsantos.upleveling.ApiTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FriendshipControllerTest extends ApiTest {

    @Test
    void everyAccountGetsAFriendCode() throws Exception {
        String token = registerAndLogin("Ana", "ana@mail.com");

        mockMvc.perform(get("/api/me").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.friendCode", matchesPattern("UPL-[2-9A-HJ-NP-Z]{6}")));
    }

    @Test
    void requestThenAcceptMakesBothFriendsWithLevel() throws Exception {
        String ana = registerAndLogin("Ana", "ana@mail.com");
        String bruno = registerAndLogin("Bruno", "bruno@mail.com");
        giveXp(bruno, 30);

        String body = sendRequest(ana, friendCode(bruno))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Bruno"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn().getResponse().getContentAsString();
        Integer requestId = JsonPath.read(body, "$.id");

        // Antes do aceite, ninguém aparece como amigo; o pedido aparece dos dois lados
        friends(ana).andExpect(jsonPath("$.length()").value(0));
        requests(ana)
                .andExpect(jsonPath("$.sent[0].name").value("Bruno"))
                .andExpect(jsonPath("$.received.length()").value(0));
        requests(bruno)
                .andExpect(jsonPath("$.received[0].id").value(requestId))
                .andExpect(jsonPath("$.received[0].name").value("Ana"));

        accept(bruno, requestId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ana"));

        friends(ana)
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Bruno"))
                .andExpect(jsonPath("$[0].progress.totalXp").value(30))
                .andExpect(jsonPath("$[0].progress.level").value(1))
                .andExpect(jsonPath("$[0].friendsSince").isNotEmpty());
        friends(bruno).andExpect(jsonPath("$[0].name").value("Ana"));
        requests(bruno).andExpect(jsonPath("$.received.length()").value(0));
    }

    @Test
    void codeIsAcceptedInAnyCaseAndWithoutThePrefix() throws Exception {
        String ana = registerAndLogin("Ana", "ana@mail.com");
        String bruno = registerAndLogin("Bruno", "bruno@mail.com");
        String typed = " " + friendCode(bruno).substring("UPL-".length()).toLowerCase() + " ";

        sendRequest(ana, typed).andExpect(status().isCreated());
    }

    @Test
    void sendingBackToWhoAlreadyAskedAcceptsTheirRequest() throws Exception {
        String ana = registerAndLogin("Ana", "ana@mail.com");
        String bruno = registerAndLogin("Bruno", "bruno@mail.com");
        sendRequest(ana, friendCode(bruno)).andExpect(status().isCreated());

        sendRequest(bruno, friendCode(ana))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));

        friends(ana).andExpect(jsonPath("$[0].name").value("Bruno"));
        requests(ana).andExpect(jsonPath("$.sent.length()").value(0));
    }

    @Test
    void rejectsInvalidRequests() throws Exception {
        String ana = registerAndLogin("Ana", "ana@mail.com");
        String bruno = registerAndLogin("Bruno", "bruno@mail.com");

        sendRequest(ana, "UPL-ZZZZZZ")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Nenhuma conta com o código 'UPL-ZZZZZZ'"));
        sendRequest(ana, friendCode(ana))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value("Esse é o seu próprio código de amigo"));
        sendRequest(ana, "")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.friendCode").value("é obrigatório"));

        sendRequest(ana, friendCode(bruno)).andExpect(status().isCreated());
        sendRequest(ana, friendCode(bruno))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Você já enviou um pedido para Bruno"));
    }

    @Test
    void alreadyFriendsIsAConflict() throws Exception {
        String ana = registerAndLogin("Ana", "ana@mail.com");
        String bruno = registerAndLogin("Bruno", "bruno@mail.com");
        makeFriends(ana, bruno);

        sendRequest(ana, friendCode(bruno))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Você e Bruno já são amigos"));
    }

    /** Quem enviou não aceita o próprio pedido, e uma terceira pessoa não mexe no pedido dos outros. */
    @Test
    void onlyTheAddresseeAcceptsAndOutsidersGetNotFound() throws Exception {
        String ana = registerAndLogin("Ana", "ana@mail.com");
        String bruno = registerAndLogin("Bruno", "bruno@mail.com");
        String carla = registerAndLogin("Carla", "carla@mail.com");
        Integer requestId = requestId(sendRequest(ana, friendCode(bruno)));

        accept(ana, requestId).andExpect(status().isNotFound());
        accept(carla, requestId).andExpect(status().isNotFound());
        deleteRequest(carla, requestId).andExpect(status().isNotFound());

        accept(bruno, requestId).andExpect(status().isOk());
        accept(bruno, requestId).andExpect(status().isNotFound()); // já respondido
    }

    @Test
    void declineAndCancelRemoveTheRequestAndAllowAskingAgain() throws Exception {
        String ana = registerAndLogin("Ana", "ana@mail.com");
        String bruno = registerAndLogin("Bruno", "bruno@mail.com");

        Integer first = requestId(sendRequest(ana, friendCode(bruno)));
        deleteRequest(bruno, first).andExpect(status().isNoContent()); // Bruno recusa
        requests(ana).andExpect(jsonPath("$.sent.length()").value(0));

        Integer second = requestId(sendRequest(ana, friendCode(bruno)));
        deleteRequest(ana, second).andExpect(status().isNoContent()); // Ana cancela
        requests(bruno).andExpect(jsonPath("$.received.length()").value(0));

        sendRequest(ana, friendCode(bruno)).andExpect(status().isCreated());
    }

    @Test
    void removingAFriendRemovesBothSides() throws Exception {
        String ana = registerAndLogin("Ana", "ana@mail.com");
        String bruno = registerAndLogin("Bruno", "bruno@mail.com");
        makeFriends(ana, bruno);
        Integer brunoId = userId(bruno);

        mockMvc.perform(delete("/api/friends/" + brunoId).header(HttpHeaders.AUTHORIZATION, ana))
                .andExpect(status().isNoContent());

        friends(ana).andExpect(jsonPath("$.length()").value(0));
        friends(bruno).andExpect(jsonPath("$.length()").value(0));
        mockMvc.perform(delete("/api/friends/" + brunoId).header(HttpHeaders.AUTHORIZATION, ana))
                .andExpect(status().isNotFound());
    }

    @Test
    void friendsRequireLogin() throws Exception {
        mockMvc.perform(get("/api/friends")).andExpect(status().isUnauthorized());
    }

    private void makeFriends(String requester, String addressee) throws Exception {
        accept(addressee, requestId(sendRequest(requester, friendCode(addressee)))).andExpect(status().isOk());
    }

    /** Cria um hábito com esse XP e faz um check-in hoje. */
    private void giveXp(String token, int xp) throws Exception {
        String body = mockMvc.perform(post("/api/habits")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Ler", "xpReward": %d}
                                """.formatted(xp)))
                .andReturn().getResponse().getContentAsString();
        Integer habitId = JsonPath.read(body, "$.id");
        mockMvc.perform(post("/api/habits/" + habitId + "/check-ins").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isCreated());
    }

    private String friendCode(String token) throws Exception {
        return JsonPath.read(me(token), "$.friendCode");
    }

    private Integer userId(String token) throws Exception {
        return JsonPath.read(me(token), "$.id");
    }

    private String me(String token) throws Exception {
        return mockMvc.perform(get("/api/me").header(HttpHeaders.AUTHORIZATION, token))
                .andReturn().getResponse().getContentAsString();
    }

    private Integer requestId(ResultActions result) throws Exception {
        return JsonPath.read(result.andReturn().getResponse().getContentAsString(), "$.id");
    }

    private ResultActions sendRequest(String token, String friendCode) throws Exception {
        return mockMvc.perform(post("/api/friends/requests")
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"friendCode": "%s"}
                        """.formatted(friendCode)));
    }

    private ResultActions accept(String token, Integer requestId) throws Exception {
        return mockMvc.perform(post("/api/friends/requests/" + requestId + "/accept")
                .header(HttpHeaders.AUTHORIZATION, token));
    }

    private ResultActions deleteRequest(String token, Integer requestId) throws Exception {
        return mockMvc.perform(delete("/api/friends/requests/" + requestId).header(HttpHeaders.AUTHORIZATION, token));
    }

    private ResultActions friends(String token) throws Exception {
        return mockMvc.perform(get("/api/friends").header(HttpHeaders.AUTHORIZATION, token));
    }

    private ResultActions requests(String token) throws Exception {
        return mockMvc.perform(get("/api/friends/requests").header(HttpHeaders.AUTHORIZATION, token));
    }
}
