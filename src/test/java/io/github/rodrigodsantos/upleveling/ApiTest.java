package io.github.rodrigodsantos.upleveling;

import com.jayway.jsonpath.JsonPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Base dos testes de integração: API inteira + Postgres real (Testcontainers), com atalhos de cadastro e login.
 * <p>
 * {@code @Transactional}: cada teste roda numa transação desfeita no fim, então um teste não suja o outro.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional
public abstract class ApiTest {

    protected static final String PASSWORD = "senha-forte-123";

    @Autowired
    protected MockMvc mockMvc;

    protected ResultActions register(String name, String email, String password) throws Exception {
        return mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "%s", "email": "%s", "password": "%s"}
                        """.formatted(name, email, password)));
    }

    protected ResultActions login(String email, String password) throws Exception {
        return mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email": "%s", "password": "%s"}
                        """.formatted(email, password)));
    }

    /** Cadastra e faz login; devolve o valor pronto para o header Authorization. */
    protected String registerAndLogin(String name, String email) throws Exception {
        register(name, email, PASSWORD);
        return bearer(email, PASSWORD);
    }

    protected String bearer(String email, String password) throws Exception {
        String body = login(email, password).andReturn().getResponse().getContentAsString();
        return "Bearer " + JsonPath.read(body, "$.token");
    }
}
