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
    @Autowired CareerRepository repository;
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
    @Test void appearancePersistsAndChangesWithoutResettingStory() throws Exception {
        Cookie cookie=create();
        mvc.perform(post("/api/game/action").cookie(cookie).with(csrf()).contentType("application/json")
            .content("{\"type\":\"work\",\"target\":\"\",\"revision\":0}"))
            .andExpect(status().isOk());
        mvc.perform(post("/api/game/appearance").cookie(cookie).with(csrf()).contentType("application/json")
            .content("{\"revision\":1,\"appearance\":{\"face\":\"soft\",\"skin\":\"ebony\",\"hair\":\"braids\",\"hairColor\":\"cyan\",\"eyes\":\"visor\",\"outfit\":\"nomad\",\"accent\":\"magenta\"},\"money\":99999}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.character.appearance.hair").value("braids"))
            .andExpect(jsonPath("$.character.money").value(680)).andExpect(jsonPath("$.character.day").value(2))
            .andExpect(jsonPath("$.character.revision").value(2));
        mvc.perform(get("/api/game").cookie(cookie)).andExpect(jsonPath("$.game.character.appearance.accent").value("magenta"));
        mvc.perform(post("/api/game/appearance").cookie(cookie).with(csrf()).contentType("application/json")
            .content("{\"revision\":1,\"appearance\":{}}"))
            .andExpect(status().isBadRequest());
        mvc.perform(post("/api/game/appearance").cookie(cookie).contentType("application/json")
            .content("{\"revision\":2,\"appearance\":{}}"))
            .andExpect(status().isForbidden());
    }
    @Test void malformedAppearanceRejectedBeforeSaving() throws Exception {
        mvc.perform(post("/api/game").with(csrf()).contentType("application/json")
            .content("{\"name\":\"Nova\",\"origin\":\"street\",\"specialty\":\"solo\",\"appearance\":{\"hair\":\"<script>\"}}"))
            .andExpect(status().isBadRequest());
    }
    @Test void legacyJsonWithoutAppearanceRemainsPlayable() throws Exception {
        java.util.UUID id=java.util.UUID.randomUUID();
        repository.saveAndFlush(new Career(id, "{\"name\":\"Legacy\",\"origin\":\"street\",\"specialty\":\"solo\",\"money\":777,\"attributes\":{\"body\":4,\"reflex\":3,\"tech\":3,\"intelligence\":3,\"cool\":4},\"factions\":{\"helix\":0,\"ghosts\":0,\"district\":5}}"));
        mvc.perform(get("/api/game").cookie(new Cookie("CP_CAREER",id.toString())))
            .andExpect(status().isOk()).andExpect(jsonPath("$.game.character.money").value(777))
            .andExpect(jsonPath("$.game.character.appearance.hair").value("undercut"));
    }

}
