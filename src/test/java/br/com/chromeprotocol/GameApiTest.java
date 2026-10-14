package br.com.chromeprotocol;

import com.fasterxml.jackson.databind.*;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.*;
import org.springframework.mock.web.MockHttpSession;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:api-tests;DB_CLOSE_DELAY=-1", "spring.jpa.hibernate.ddl-auto=create-drop"})
@AutoConfigureMockMvc
class GameApiTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    static final String NEW="{\"name\":\"Nova\",\"origin\":\"street\",\"specialty\":\"netrunner\"}";
    private Cookie create() throws Exception {
        MvcResult result=mvc.perform(post("/api/game").with(csrf()).contentType("application/json").content(NEW))
            .andExpect(status().isOk()).andExpect(jsonPath("$.character.money").value(500)).andReturn();
        Cookie cookie=result.getResponse().getCookie("CP_CAREER");
        assertNotNull(cookie); assertTrue(cookie.isHttpOnly()); return cookie;
    }
    @Test void newBrowserHasNoCareerAndAssetsLoad() throws Exception {
        mvc.perform(get("/api/game")).andExpect(status().isOk()).andExpect(jsonPath("$.started").value(false));
        mvc.perform(get("/")).andExpect(status().isOk()).andExpect(header().exists("Content-Security-Policy"));
    }
    @Test void csrfRequiredForMutations() throws Exception {
        mvc.perform(post("/api/game").contentType("application/json").content(NEW)).andExpect(status().isForbidden());
    }
    @Test void browserCanObtainRealCsrfToken() throws Exception {
        MvcResult tokenResult=mvc.perform(get("/api/session")).andExpect(status().isOk()).andReturn();
        JsonNode token=mapper.readTree(tokenResult.getResponse().getContentAsString());
        MockHttpSession session=(MockHttpSession)tokenResult.getRequest().getSession(false);
        assertNotNull(session);
        mvc.perform(post("/api/game").session(session).header(token.get("header").asText(), token.get("token").asText())
            .contentType("application/json").content(NEW)).andExpect(status().isOk());
    }
    @Test void statePersistsAndDuplicateRevisionIsRejected() throws Exception {
        Cookie cookie=create();
        String action="{\"type\":\"work\",\"target\":\"\",\"revision\":0,\"money\":999999}";
        mvc.perform(post("/api/game/action").cookie(cookie).with(csrf()).contentType("application/json").content(action))
            .andExpect(status().isOk()).andExpect(jsonPath("$.character.money").value(680));
        mvc.perform(post("/api/game/action").cookie(cookie).with(csrf()).contentType("application/json").content(action))
            .andExpect(status().isBadRequest());
        mvc.perform(get("/api/game").cookie(cookie)).andExpect(jsonPath("$.game.character.money").value(680))
            .andExpect(jsonPath("$.game.character.revision").value(1));
        mvc.perform(get("/api/game")).andExpect(jsonPath("$.started").value(false));
    }
    @Test void lockedRoutesCannotBeForgedAndCreateCannotOverwrite() throws Exception {
        Cookie cookie=create();
        mvc.perform(post("/api/game/action").cookie(cookie).with(csrf()).contentType("application/json")
            .content("{\"type\":\"choice\",\"target\":\"free\",\"revision\":0}"))
            .andExpect(status().isBadRequest());
        mvc.perform(post("/api/game").cookie(cookie).with(csrf()).contentType("application/json").content(NEW))
            .andExpect(status().isBadRequest());
        mvc.perform(get("/api/game").cookie(cookie)).andExpect(jsonPath("$.game.character.revision").value(0));
    }
    @Test void restartValidatesRevisionAndKeepsBrowserIdentity() throws Exception {
        Cookie cookie=create();
        mvc.perform(post("/api/game/restart").cookie(cookie).with(csrf()).contentType("application/json")
            .content("{\"name\":\"Rook\",\"origin\":\"corpo\",\"specialty\":\"solo\",\"revision\":7}"))
            .andExpect(status().isBadRequest());
        mvc.perform(post("/api/game/restart").cookie(cookie).with(csrf()).contentType("application/json")
            .content("{\"name\":\"Rook\",\"origin\":\"corpo\",\"specialty\":\"solo\",\"revision\":0}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.character.money").value(800));
        mvc.perform(get("/api/game").cookie(cookie)).andExpect(jsonPath("$.game.character.name").value("Rook"))
            .andExpect(jsonPath("$.game.character.revision").value(1));
    }
    @Test void invalidCookieAndInvalidIdentityHandled() throws Exception {
        mvc.perform(get("/api/game").cookie(new Cookie("CP_CAREER","invalid"))).andExpect(jsonPath("$.started").value(false));
        mvc.perform(post("/api/game").with(csrf()).contentType("application/json")
            .content("{\"name\":\"\",\"origin\":\"street\",\"specialty\":\"solo\"}"))
            .andExpect(status().isBadRequest());
    }
}
