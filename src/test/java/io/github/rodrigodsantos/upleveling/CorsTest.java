package io.github.rodrigodsantos.upleveling;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * O "preflight" é o OPTIONS que o navegador manda sozinho antes de uma chamada de outro endereço,
 * perguntando "posso?". Ele vem sem token, então não pode cair no 401.
 */
class CorsTest extends ApiTest {

    @Test
    void allowedOriginPassesThePreflight() throws Exception {
        mockMvc.perform(options("/api/habits")
                        .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "authorization,content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173"));
    }

    @Test
    void unknownOriginIsRejected() throws Exception {
        mockMvc.perform(options("/api/habits")
                        .header(HttpHeaders.ORIGIN, "https://site-qualquer.com")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }
}
