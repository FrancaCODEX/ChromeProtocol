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

    @Test void optionalRoutinePersistsWithoutAdvancingOrRepeating() throws Exception {
        Cookie cookie=create();var initial=mvc.perform(get("/api/game").cookie(cookie)).andReturn();var data=mapper.readTree(initial.getResponse().getContentAsString());String activity=data.path("game").path("activities").get(0).path("item").path("id").asText();String body=mapper.writeValueAsString(new GameEngine.Action("activity",activity,0));
        mvc.perform(post("/api/game/action").cookie(cookie).with(csrf()).contentType("application/json").content(body)).andExpect(status().isOk()).andExpect(jsonPath("$.character.phase").value(0)).andExpect(jsonPath("$.character.activitySlots.length()").value(1)).andExpect(jsonPath("$.character.outcome").exists());
        mvc.perform(get("/api/game").cookie(cookie)).andExpect(jsonPath("$.game.character.activitySlots[0]").value("2026:0"));
        mvc.perform(post("/api/game/action").cookie(cookie).with(csrf()).contentType("application/json").content(mapper.writeValueAsString(new GameEngine.Action("continue","",1)))).andExpect(status().isOk());
        mvc.perform(post("/api/game/action").cookie(cookie).with(csrf()).contentType("application/json").content(mapper.writeValueAsString(new GameEngine.Action("activity",activity,2)))).andExpect(status().isBadRequest());
        mvc.perform(get("/api/game").cookie(cookie)).andExpect(jsonPath("$.game.character.revision").value(2)).andExpect(jsonPath("$.game.character.phase").value(0));
    }
    @Test void selectedGoalAndOldSeasonWithoutOverallRemainCompatible() throws Exception {
        Cookie cookie=create();mvc.perform(post("/api/game/action").cookie(cookie).with(csrf()).contentType("application/json").content(mapper.writeValueAsString(new GameEngine.Action("goal","community",0))))
            .andExpect(status().isOk()).andExpect(jsonPath("$.character.seasonGoal").value("community")).andExpect(jsonPath("$.character.goalStart").value(100)).andExpect(jsonPath("$.character.phase").value(0));
        mvc.perform(get("/api/game").cookie(cookie)).andExpect(jsonPath("$.game.character.seasonGoal").value("community"));
        var old=mapper.readValue("{\"year\":2026,\"age\":15,\"organization\":\"Mix\",\"wins\":1,\"losses\":2,\"titles\":0,\"earnings\":500}",GameState.Season.class);assertNull(old.overall(),"Do not invent old OVR");assertEquals(500,old.earnings());
    }

    private JsonNode action(Cookie cookie,String type,String target,int revision) throws Exception {
        if(target.startsWith("offer-")){var state=mapper.readValue(repository.findById(UUID.fromString(cookie.getValue())).orElseThrow().state,GameState.class);target=Catalog.offers(state).stream().filter(o->o.id().startsWith("offer:")).toList().get(Integer.parseInt(target.substring(6))).id();}
        return mapper.readTree(mvc.perform(post("/api/game/action").cookie(cookie).with(csrf()).contentType("application/json").content(mapper.writeValueAsString(new GameEngine.Action(type,target,revision)))).andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
    }
    @Test void runBatchesPersistEverySeriesAndMapWithoutRerollOrDuplicate() throws Exception {
        Cookie cookie=create();var row=repository.findById(UUID.fromString(cookie.getValue())).orElseThrow();var state=mapper.readValue(row.state,GameState.class);state.phase=3;state.pace="run";row.state=mapper.writeValueAsString(state);repository.saveAndFlush(row);
        var result=action(cookie,"choice","series-run",0);assertEquals(4,result.path("character").path("series").size());assertTrue(result.path("character").path("tourEvent").asBoolean());assertEquals(4,result.path("character").path("outcome").path("seriesResults").size());int maps=result.path("character").path("maps").size();assertTrue(maps>=8&&maps<=12);
        var read=mapper.readTree(mvc.perform(get("/api/game").cookie(cookie)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).path("game");assertEquals(result.path("character"),read.path("character"));
        mvc.perform(post("/api/game/action").cookie(cookie).with(csrf()).contentType("application/json").content(mapper.writeValueAsString(new GameEngine.Action("choice","series-run",0)))).andExpect(status().isBadRequest());
        action(cookie,"continue","",1);var unchanged=mapper.readTree(mvc.perform(get("/api/game").cookie(cookie)).andReturn().getResponse().getContentAsString()).path("game");assertEquals(maps,unchanged.path("character").path("maps").size());String decision=unchanged.path("scene").path("choices").get(0).path("id").asText();action(cookie,"choice",decision,2);var saved=mapper.readValue(repository.findById(UUID.fromString(cookie.getValue())).orElseThrow().state,GameState.class);assertFalse(saved.tourEvent);assertEquals(4,saved.series.size());assertEquals(3,saved.phase);
    }
    @Test void runCanBeSelectedAtCreationAndPaceDoesNotChangeAbilities() throws Exception {
        String body=NEW.substring(0,NEW.length()-1)+",\"pace\":\"run\"}";var response=mvc.perform(post("/api/game").with(csrf()).contentType("application/json").content(body)).andExpect(status().isOk()).andExpect(jsonPath("$.character.pace").value("run")).andExpect(jsonPath("$.scene.choices[0].id").value("run-prepare")).andReturn().getResponse();var cookie=response.getCookie("CP_CAREER");var attrs=mapper.readTree(response.getContentAsString()).path("character").path("attributes");var switched=action(cookie,"pace","detailed",0);assertEquals(attrs,switched.path("character").path("attributes"));assertEquals(0,switched.path("character").path("phase").asInt());
        mvc.perform(post("/api/game").with(csrf()).contentType("application/json").content(body.replace("run","cheat"))).andExpect(status().isBadRequest());
        for(String icon:java.util.List.of("champions","masters","regional"))mvc.perform(get("/trophies/"+icon+".svg")).andExpect(status().isOk());
    }
    @Test void previousIndependentQualificationMigratesWithoutPromotingAcademies() throws Exception {
        for(String club:java.util.List.of("red-canids","mibr-academy")){
            Cookie cookie=create();var row=repository.findById(UUID.fromString(cookie.getValue())).orElseThrow();
            var state=mapper.readValue(row.state,GameState.class);state.rulesVersion=3;state.phase=9;
            var org=Catalog.ORGS.stream().filter(o->o.id().equals(club)).findFirst().orElseThrow();
            state.contract=new GameState.Contract(club,org.name(),org.tier(),3000,2026,"Titular",24);
            var stage=new GameState.Campaign();stage.done=true;stage.wins=6;stage.played=10;state.campaigns.put(8,stage);
            row.state=mapper.writeValueAsString(state);repository.saveAndFlush(row);
            var view=mapper.readTree(mvc.perform(get("/api/game").cookie(cookie)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).path("game");
            assertEquals(4,view.path("character").path("rulesVersion").asInt());
            assertEquals(!org.academy(),view.path("character").path("qualifiedAscension").asBoolean());
            assertEquals(0,view.path("character").path("vctPromotions").size());
        }
    }

    @Test void unfinishedLegacyMapIsNotInventedAsAFullSeries() throws Exception {
        Cookie cookie=create();var row=repository.findById(UUID.fromString(cookie.getValue())).orElseThrow();var state=mapper.readValue(row.state,GameState.class);state.rulesVersion=2;state.phase=3;state.wins=7;state.match=new GameState.Match();state.match.opponent="Mix antigo";state.match.map="Haven";state.match.competition="Premier antigo";state.match.stage=2;state.match.roundsWon=2;state.match.score=9;state.match.opponentScore=7;row.state=mapper.writeValueAsString(state);repository.saveAndFlush(row);
        var initial=mapper.readTree(mvc.perform(get("/api/game").cookie(cookie)).andExpect(status().isOk()).andExpect(jsonPath("$.game.activeMatch.stage").value(2)).andReturn().getResponse().getContentAsString()).path("game");assertEquals(0,initial.path("character").path("series").size());String choice=initial.path("scene").path("choices").get(0).path("id").asText();var completed=action(cookie,"choice",choice,0).path("character");assertEquals(8,completed.path("wins").asInt());assertEquals(0,completed.path("series").size());assertTrue(completed.path("maps").get(0).path("seriesId").isNull());assertEquals(4,completed.path("phase").asInt());
    }

    @Test void seasonModeSimulatesBetweenMeaningfulDecisionsAndPersists() throws Exception {
        String body=NEW.substring(0,NEW.length()-1)+",\"pace\":\"season\",\"appearance\":{\"hair\":\"curly\",\"beard\":\"goatee\",\"eyeColor\":\"green\",\"eyewear\":\"round\",\"headwear\":\"headset\",\"jerseyPattern\":\"stripes\"}}";
        var cookie=mvc.perform(post("/api/game").with(csrf()).contentType("application/json").content(body)).andExpect(status().isOk()).andExpect(jsonPath("$.scene.choices[0].id").value("season-focus:aim")).andExpect(jsonPath("$.character.appearance.headwear").value("headset")).andReturn().getResponse().getCookie("CP_CAREER");
        action(cookie,"choice","season-focus:aim",0);action(cookie,"continue","",1);var first=action(cookie,"choice","offer-0",2);assertEquals(7,first.path("character").path("phase").asInt());assertEquals(16,first.path("character").path("series").size());assertEquals(16,first.path("character").path("outcome").path("seriesResults").size());
        var saved=mapper.readTree(mvc.perform(get("/api/game").cookie(cookie)).andReturn().getResponse().getContentAsString()).path("game");assertEquals(first.path("character"),saved.path("character"));var event=action(cookie,"continue","",3);String id=event.path("scene").path("choices").get(0).path("id").asText();var end=action(cookie,"choice",id,4);assertTrue(end.path("character").path("pendingCards").asBoolean());assertEquals(26,end.path("character").path("series").size());assertEquals(1,end.path("character").path("seasons").size());assertEquals(2026,end.path("character").path("year").asInt());
        mvc.perform(post("/api/game/action").cookie(cookie).with(csrf()).contentType("application/json").content(mapper.writeValueAsString(new GameEngine.Action("choice",id,4)))).andExpect(status().isBadRequest());
        mvc.perform(get("/api/game").cookie(cookie)).andExpect(jsonPath("$.game.character.seasons.length()").value(1)).andExpect(jsonPath("$.game.character.appearance.eyewear").value("round"));
    }

    @Test void renewalAndStarterPromotionPersistWithoutReplayingTheWindow() throws Exception {
        Cookie cookie=create();var row=repository.findById(UUID.fromString(cookie.getValue())).orElseThrow();var state=mapper.readValue(row.state,GameState.class);
        state.year=2027;state.age=16;state.phase=2;state.attributes.replaceAll((k,v)->80);
        state.expiredContract=new GameState.Contract("furia","FURIA",3,500,2026,"Reserva",12);
        row.state=mapper.writeValueAsString(state);repository.saveAndFlush(row);
        mvc.perform(get("/api/game").cookie(cookie)).andExpect(jsonPath("$.game.offers[0].id").value("renew")).andExpect(jsonPath("$.game.offers[0].position").value("Titular"));
        action(cookie,"choice","renew",0);
        mvc.perform(get("/api/game").cookie(cookie)).andExpect(jsonPath("$.game.character.contract.orgId").value("furia")).andExpect(jsonPath("$.game.character.contract.endYear").value(2027)).andExpect(jsonPath("$.game.character.contract.position").value("Titular")).andExpect(jsonPath("$.game.character.expiredContract").doesNotExist());
        mvc.perform(post("/api/game/action").cookie(cookie).with(csrf()).contentType("application/json").content(mapper.writeValueAsString(new GameEngine.Action("choice","renew",0)))).andExpect(status().isBadRequest());
        var saved=mapper.readValue(repository.findById(row.id).orElseThrow().state,GameState.class);assertEquals("Titular",saved.contract.position());assertNull(saved.expiredContract);
    }
    @Test void studioAppearanceAndOldReserveSaveRemainCompatible() throws Exception {
        Cookie cookie=create();var row=repository.findById(UUID.fromString(cookie.getValue())).orElseThrow();var state=mapper.readValue(row.state,GameState.class);
        state.attributes.replaceAll((k,v)->80);state.contract=new GameState.Contract("furia","FURIA",3,500,2028,"Reserva",12);row.state=mapper.writeValueAsString(state);repository.saveAndFlush(row);
        mvc.perform(get("/api/game").cookie(cookie)).andExpect(jsonPath("$.game.character.contract.position").value("Titular")).andExpect(jsonPath("$.game.character.appearance.portraitStyle").value("classic"));
        mvc.perform(post("/api/game/appearance").cookie(cookie).with(csrf()).contentType("application/json").content("{\"revision\":0,\"appearance\":{\"portraitStyle\":\"studio\",\"hair\":\"long\"}}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.character.appearance.portraitStyle").value("arena"));
        mvc.perform(get("/api/game").cookie(cookie)).andExpect(jsonPath("$.game.character.appearance.portraitStyle").value("arena")).andExpect(jsonPath("$.game.character.appearance.hair").value("long"));
        for(String id:java.util.List.of("champions","masters","americas","emea","pacific","china","brazil","korea","spain"))mvc.perform(get("/trophies/"+id+".png")).andExpect(status().isOk());
        mvc.perform(get("/vendor/avatar-studio.js")).andExpect(status().isOk());
    }
    @Test void displayedMarketIdsAlwaysSignTheSameClubIncludingRenewalAndInternationalOffers() throws Exception {
        for(String wanted:java.util.List.of("renew","offer:sentinels","offer:fnatic","offer:drx","promote:mibr")){
            Cookie cookie=create();var row=repository.findById(UUID.fromString(cookie.getValue())).orElseThrow();var state=mapper.readValue(row.state,GameState.class);state.phase=2;state.age=15;state.reputation=100;state.attributes.replaceAll((k,v)->85);state.expiredContract=new GameState.Contract(wanted.startsWith("promote")?"mibr-academy":"nrg",wanted.startsWith("promote")?"MIBR Academy":"NRG",wanted.startsWith("promote")?1:3,18000,2025,"Titular",36);
            // Find an annual seed offering the requested club; identity remains stable for that seed.
            for(int seed=0;seed<1000;seed++){state.careerSeed=seed;if(Catalog.offers(state).stream().anyMatch(o->o.id().equals(wanted)))break;}
            var proposal=Catalog.offers(state).stream().filter(o->o.id().equals(wanted)).findFirst().orElseThrow();row.state=mapper.writeValueAsString(state);repository.saveAndFlush(row);
            var view=mapper.readTree(mvc.perform(get("/api/game").cookie(cookie)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).path("game");
            var names=new java.util.HashSet<String>();for(var offer:view.path("offers"))assertTrue(names.add(offer.path("orgId").asText()),"Distinct organizations");
            var signed=action(cookie,"choice",wanted,0).path("character").path("contract");assertEquals(proposal.orgId(),signed.path("orgId").asText());assertEquals(proposal.name(),signed.path("name").asText());assertEquals(proposal.salary(),signed.path("salary").asInt());
            mvc.perform(get("/api/game").cookie(cookie)).andExpect(jsonPath("$.game.character.contract.orgId").value(proposal.orgId()));
        }
    }
    @Test void femaleArenaAppearancePersistsWithoutChangingCareerAndLegacyStudioUpgrades() throws Exception {
        Cookie cookie=create();var row=repository.findById(UUID.fromString(cookie.getValue())).orElseThrow();var state=mapper.readValue(row.state,GameState.class);var attrs=new java.util.LinkedHashMap<>(state.attributes);int money=state.money;
        mvc.perform(post("/api/game/appearance").cookie(cookie).with(csrf()).contentType("application/json").content("{\"revision\":0,\"appearance\":{\"portraitStyle\":\"arena\",\"presentation\":\"feminine\",\"hair\":\"ponytail\",\"outfit\":\"runner\"}}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.character.appearance.presentation").value("feminine"));
        var saved=mapper.readValue(repository.findById(row.id).orElseThrow().state,GameState.class);assertEquals(attrs,saved.attributes);assertEquals(money,saved.money);assertEquals("feminine",saved.appearance.presentation());
        mvc.perform(get("/api/game").cookie(cookie)).andExpect(jsonPath("$.game.character.appearance.presentation").value("feminine"));
        mvc.perform(post("/api/game/appearance").cookie(cookie).with(csrf()).contentType("application/json").content("{\"revision\":1,\"appearance\":{\"presentation\":\"invalid\"}}" )).andExpect(status().isBadRequest());
    }
    @Test void regionalAccessSurvivesReloadAndRevaluesExpiredContractAtNewSeason() throws Exception {
        Cookie cookie=create();var row=repository.findById(UUID.fromString(cookie.getValue())).orElseThrow();var state=mapper.readValue(row.state,GameState.class);state.year=2027;state.age=16;state.phase=2;state.contract=new GameState.Contract("red-canids","RED Canids Kalunga",2,3000,2028,"Titular",24);state.vctPromotions.put("red-canids",2027);row.state=mapper.writeValueAsString(state);repository.saveAndFlush(row);
        mvc.perform(get("/api/game").cookie(cookie)).andExpect(jsonPath("$.game.character.contract.tier").value(3)).andExpect(jsonPath("$.game.circuit").value("VCT Americas"));
        var result=action(cookie,"choice","honor",0).path("character");assertEquals(2027,result.path("vctPromotions").path("red-canids").asInt());assertTrue(result.path("contract").path("salary").asInt()>3000);
    }

}
