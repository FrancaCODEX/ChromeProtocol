package br.com.chromeprotocol;

import com.fasterxml.jackson.databind.*;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.*;
import org.springframework.mock.web.MockHttpSession;
import java.util.UUID;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:career-api;DB_CLOSE_DELAY=-1","spring.jpa.hibernate.ddl-auto=create-drop"})
@AutoConfigureMockMvc
class GameApiTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired CareerRepository repository;
    static final String NEW="{\"name\":\"Nova\",\"nationality\":\"BR\",\"role\":\"duelist\",\"background\":\"ranked\",\"agent\":\"Jett\"}";
    private Cookie create() throws Exception {
        var response=mvc.perform(post("/api/game").with(csrf()).contentType("application/json").content(NEW))
            .andExpect(status().isOk()).andExpect(jsonPath("$.character.age").value(15)).andReturn().getResponse();
        var cookie=response.getCookie("CP_CAREER");assertNotNull(cookie);assertTrue(cookie.isHttpOnly());return cookie;
    }
    @Test void newBrowserAndCatalog() throws Exception {
        mvc.perform(get("/api/game")).andExpect(jsonPath("$.started").value(false));
        mvc.perform(get("/api/catalog")).andExpect(status().isOk()).andExpect(jsonPath("$.countries[0].id").value("BR"));
        mvc.perform(get("/")).andExpect(status().isOk()).andExpect(header().exists("Content-Security-Policy"));
    }
    @Test void csrfRequiredAndTokenUsable() throws Exception {
        mvc.perform(post("/api/game").contentType("application/json").content(NEW)).andExpect(status().isForbidden());
        var result=mvc.perform(get("/api/session")).andReturn();var token=mapper.readTree(result.getResponse().getContentAsString());
        mvc.perform(post("/api/game").session((MockHttpSession)result.getRequest().getSession(false)).header(token.get("header").asText(),token.get("token").asText()).contentType("application/json").content(NEW)).andExpect(status().isOk());
    }
    @Test void resultPersistsAndMustBeAcknowledged() throws Exception {
        Cookie cookie=create();
        String action="{\"type\":\"choice\",\"target\":\"prep-aim\",\"revision\":0,\"money\":999999}";
        mvc.perform(post("/api/game/action").cookie(cookie).with(csrf()).contentType("application/json").content(action))
            .andExpect(status().isOk()).andExpect(jsonPath("$.character.phase").value(1)).andExpect(jsonPath("$.character.money").value(500)).andExpect(jsonPath("$.character.outcome.title").exists());
        mvc.perform(get("/api/game").cookie(cookie)).andExpect(jsonPath("$.game.character.outcome.changes[0]").exists());
        mvc.perform(post("/api/game/action").cookie(cookie).with(csrf()).contentType("application/json").content(action)).andExpect(status().isBadRequest());
        mvc.perform(post("/api/game/action").cookie(cookie).with(csrf()).contentType("application/json").content("{\"type\":\"choice\",\"target\":\"trial-steady\",\"revision\":1}")).andExpect(status().isBadRequest());
        mvc.perform(post("/api/game/action").cookie(cookie).with(csrf()).contentType("application/json").content("{\"type\":\"continue\",\"target\":\"\",\"revision\":1}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.character.outcome").doesNotExist());
        mvc.perform(get("/api/game")).andExpect(jsonPath("$.started").value(false));
    }
    @Test void oldCampaignIsArchivedWithoutOverwritingItsRow() throws Exception {
        UUID id=UUID.randomUUID();String old="{\"name\":\"Old Nova\",\"origin\":\"street\",\"money\":777}";repository.saveAndFlush(new Career(id,old));var cookie=new Cookie("CP_CAREER",id.toString());
        mvc.perform(get("/api/game").cookie(cookie)).andExpect(jsonPath("$.started").value(false)).andExpect(jsonPath("$.legacy").value(true));
        mvc.perform(get("/api/archive").cookie(cookie)).andExpect(status().isOk()).andExpect(jsonPath("$.money").value(777));
        var created=mvc.perform(post("/api/game").cookie(cookie).with(csrf()).contentType("application/json").content(NEW)).andExpect(status().isOk()).andReturn().getResponse().getCookie("CP_CAREER");
        mvc.perform(get("/api/archive").cookie(created)).andExpect(status().isOk()).andExpect(jsonPath("$.money").value(777));
        mvc.perform(get("/api/game").cookie(created)).andExpect(jsonPath("$.archiveAvailable").value(true));
        assertEquals(old,repository.findById(id).orElseThrow().state);
    }
    @Test void cosmeticUpdatesPreserveCareerAndValidateRevision() throws Exception {
        Cookie cookie=create();
        mvc.perform(post("/api/game/appearance").cookie(cookie).with(csrf()).contentType("application/json")
            .content("{\"revision\":0,\"appearance\":{\"hair\":\"braids\",\"eyes\":\"visor\",\"outfit\":\"nomad\"}}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.character.appearance.hair").value("braids")).andExpect(jsonPath("$.character.age").value(15)).andExpect(jsonPath("$.character.money").value(500));
        mvc.perform(post("/api/game/appearance").cookie(cookie).with(csrf()).contentType("application/json").content("{\"revision\":0,\"appearance\":{}}")).andExpect(status().isBadRequest());
        mvc.perform(post("/api/game/appearance").cookie(cookie).contentType("application/json").content("{\"revision\":1,\"appearance\":{}}")).andExpect(status().isForbidden());
    }
    @Test void forgedOrInvalidChoicesDoNotChangeCareer() throws Exception {
        Cookie cookie=create();
        mvc.perform(post("/api/game/action").cookie(cookie).with(csrf()).contentType("application/json").content("{\"type\":\"choice\",\"target\":\"offer-2\",\"revision\":0}")).andExpect(status().isBadRequest());
        mvc.perform(post("/api/game").cookie(cookie).with(csrf()).contentType("application/json").content(NEW)).andExpect(status().isBadRequest());
        mvc.perform(get("/api/game").cookie(cookie)).andExpect(jsonPath("$.game.character.revision").value(0));
    }
    @Test void newCareerHasExplicitRevisionAndReset() throws Exception {
        Cookie cookie=create();String restart=NEW.substring(0,NEW.length()-1)+",\"revision\":0}";
        mvc.perform(post("/api/game/restart").cookie(cookie).with(csrf()).contentType("application/json").content(restart)).andExpect(status().isOk()).andExpect(jsonPath("$.character.revision").value(1));
        mvc.perform(post("/api/game/restart").cookie(cookie).with(csrf()).contentType("application/json").content(restart)).andExpect(status().isBadRequest());
    }
    @Test void identityAndAppearanceAreValidated() throws Exception {
        mvc.perform(post("/api/game").with(csrf()).contentType("application/json").content(NEW.replace("Jett","Unknown"))).andExpect(status().isBadRequest());
        mvc.perform(post("/api/game").with(csrf()).contentType("application/json").content(NEW.substring(0,NEW.length()-1)+",\"appearance\":{\"hair\":\"bad\"}}" )).andExpect(status().isBadRequest());
        mvc.perform(get("/api/game").cookie(new Cookie("CP_CAREER","invalid"))).andExpect(jsonPath("$.started").value(false));
    }
    @Test void boostFavoritesLeadershipAndPermanentInvestmentPersist() throws Exception {
        String body=NEW.substring(0,NEW.length()-1)+",\"developmentMode\":\"prodigy\",\"igl\":true,\"favoriteAgents\":[\"Miks\",\"Veto\",\"Waylay\"]}";
        var cookie=mvc.perform(post("/api/game").with(csrf()).contentType("application/json").content(body)).andExpect(status().isOk()).andExpect(jsonPath("$.character.igl").value(true)).andExpect(jsonPath("$.character.developmentMode").value("prodigy")).andExpect(jsonPath("$.character.favoriteAgents.length()").value(4)).andReturn().getResponse().getCookie("CP_CAREER");
        var row=repository.findById(UUID.fromString(cookie.getValue())).orElseThrow();var state=mapper.readValue(row.state,GameState.class);state.money=10000;row.state=mapper.writeValueAsString(state);repository.saveAndFlush(row);
        mvc.perform(post("/api/game/action").cookie(cookie).with(csrf()).contentType("application/json").content("{\"type\":\"invest\",\"target\":\"coach\",\"revision\":0}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.character.phase").value(0)).andExpect(jsonPath("$.character.money").value(8500)).andExpect(jsonPath("$.character.investments.coach").value(1)).andExpect(jsonPath("$.character.permanentBonuses.tactics").value(5));
        mvc.perform(get("/api/game").cookie(cookie)).andExpect(jsonPath("$.game.character.outcome").exists()).andExpect(jsonPath("$.game.character.permanentBonuses.tactics").value(5));
    }
    @Test void firstVersionCareerMigratesWithoutLosingProgressOrPendingMap() throws Exception {
        UUID id=UUID.randomUUID();GameState s=new GameEngine(new java.util.Random(1)).create("Veteran","BR","duelist","ranked","Jett",null);s.phase=6;s.money=777;s.revision=14;s.age=20;s.year=2031;s.contract=new GameState.Contract("furia","FURIA",3,5000,2032,"Titular",12);s.match=new GameState.Match();s.match.opponent="Sentinels";s.match.map="Ascent";s.match.stage=1;s.match.score=5;s.match.opponentScore=4;
        var json=(com.fasterxml.jackson.databind.node.ObjectNode)mapper.valueToTree(s);for(String field:java.util.List.of("rulesVersion","favoriteAgents","developmentMode","maps","cards","investments","permanentBonuses","awards"))json.remove(field);String old=mapper.writeValueAsString(json);repository.saveAndFlush(new Career(id,old));var cookie=new Cookie("CP_CAREER",id.toString());
        mvc.perform(get("/api/game").cookie(cookie)).andExpect(status().isOk()).andExpect(jsonPath("$.game.character.phase").value(9)).andExpect(jsonPath("$.game.character.money").value(777)).andExpect(jsonPath("$.game.character.revision").value(14)).andExpect(jsonPath("$.game.activeMatch.stage").value(1)).andExpect(jsonPath("$.game.character.maps.length()").value(0));
        assertEquals(old,repository.findById(id).orElseThrow().state,"Read must not overwrite save");
    }

}
