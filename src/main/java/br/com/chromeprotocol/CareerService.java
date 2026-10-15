package br.com.chromeprotocol;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;
import java.util.UUID;

@Service
public class CareerService {
    private final CareerRepository repository;
    private final ObjectMapper mapper;
    private final GameEngine engine = new GameEngine(new SecureRandom());
    public CareerService(CareerRepository repository, ObjectMapper mapper) { this.repository = repository; this.mapper = mapper; }
    public record Created(UUID id, GameEngine.View game) {}
    @Transactional(readOnly = true)
    public GameEngine.View read(UUID id) {
        return id == null ? null : repository.findById(id).filter(c -> isCurrent(c.state)).map(c -> engine.view(decode(c.state))).orElse(null);
    }
    @Transactional
    public Created create(String name, String nationality, String role, String background, String agent, Appearance appearance, String mode, boolean igl, java.util.List<String> favorites, String pace, UUID previousId) {
        GameState state = engine.create(name, nationality, role, background, agent, appearance, mode, igl, favorites);
        setPace(state,pace);
        UUID id = UUID.randomUUID(); Career career = new Career(id, encode(state));
        if (legacy(previousId)) career.archivedCareerId = previousId;
        repository.save(career);
        return new Created(id, engine.view(state));
    }
    @Transactional
    public GameEngine.View act(UUID id, GameEngine.Action action) {
        Career career = load(id); GameState state = decode(career.state);
        engine.act(state, action); career.state = encode(state);
        return engine.view(state);
    }
    @Transactional
    public GameEngine.View restart(UUID id, int revision, String name, String nationality, String role, String background, String agent, Appearance appearance, String mode, boolean igl, java.util.List<String> favorites, String pace) {
        Career career = load(id); GameState old = decode(career.state);
        if (old.revision != revision) throw new IllegalArgumentException("Sua carreira mudou. Atualize antes de reiniciar.");
        GameState state = engine.create(name, nationality, role, background, agent, appearance, mode, igl, favorites); setPace(state,pace); state.revision = old.revision + 1;
        career.state = encode(state); return engine.view(state);
    }
    @Transactional
    public GameEngine.View customize(UUID id, int revision, Appearance appearance) {
        Career career = load(id); GameState state = decode(career.state);
        engine.customize(state, appearance, revision); career.state = encode(state);
        return engine.view(state);
    }
    @Transactional(readOnly = true)
    public boolean legacy(UUID id) { return id != null && repository.findById(id).map(c -> !isCurrent(c.state)).orElse(false); }
    @Transactional(readOnly = true)
    public String archive(UUID id) {
        if (id == null) throw new IllegalArgumentException("Campanha não encontrada.");
        Career own = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Campanha não encontrada."));
        if (!isCurrent(own.state)) return own.state;
        if (own.archivedCareerId == null) throw new IllegalArgumentException("Nenhuma campanha antiga neste navegador.");
        return repository.findById(own.archivedCareerId).map(c -> c.state).orElseThrow(() -> new IllegalArgumentException("Arquivo não encontrado."));
    }
    @Transactional(readOnly = true)
    public boolean hasArchive(UUID id) {
        return id != null && repository.findById(id).map(c -> !isCurrent(c.state) || c.archivedCareerId != null).orElse(false);
    }
    private void setPace(GameState state,String pace){if(pace!=null&&!java.util.Set.of("detailed","run","season").contains(pace))throw new IllegalArgumentException("Ritmo inválido.");state.pace=pace==null?"detailed":pace;}
    private boolean isCurrent(String state) {
        try { return "valorant-career-v1".equals(mapper.readTree(state).path("gameMode").asText()); }
        catch (JsonProcessingException e) { throw new IllegalStateException("Falha ao identificar salvamento", e); }
    }
    private Career load(UUID id) {
        if (id == null) throw new IllegalArgumentException("Crie um personagem primeiro.");
        return repository.locked(id).orElseThrow(() -> new IllegalArgumentException("Carreira não encontrada neste navegador."));
    }
    private String encode(GameState state) {
        try { return mapper.writeValueAsString(state); }
        catch (JsonProcessingException e) { throw new IllegalStateException("Falha ao salvar carreira", e); }
    }
    private GameState decode(String state) {
        try {
            if (!isCurrent(state)) throw new IllegalArgumentException("Crie uma carreira de Valorant para continuar.");
            GameState game = mapper.readValue(state, GameState.class);
            if (mapper.readTree(state).path("rulesVersion").asInt(1) < 2) {
                int[] phases = {0,1,2,5,7,8,9,12};
                game.phase = phases[Math.min(7, Math.max(0,game.phase))];
                game.rulesVersion = 2;
                if(game.phase==9 && game.match!=null && game.contract!=null && game.contract.tier()>=2) game.qualifiedPlayoffs=true;
                if(game.match != null) {
                    game.match.competition = Calendar.competition(game) + " — mapa em andamento";
                    game.match.finalEligible = Calendar.trophy(game);
                }
            }
            if(mapper.readTree(state).path("rulesVersion").asInt(1)<3){game.rulesVersion=3;game.legacyMap=game.match!=null;}
            if(mapper.readTree(state).path("rulesVersion").asInt(1)<4){
                // Existing independent Stage 2 campaigns retain their earned regional qualification.
                var stage=game.campaigns.get(8);
                if(Calendar.tier(game)<3&&game.contract!=null&&!Calendar.academy(game)&&!Calendar.reserve(game))
                    game.qualifiedAscension=stage!=null&&stage.done&&stage.wins>=6||game.phase==9&&game.qualifiedPlayoffs;
                game.rulesVersion=4;
            }
            if(game.favoriteAgents == null || game.favoriteAgents.isEmpty()) game.favoriteAgents = new java.util.ArrayList<>(java.util.List.of(game.agent));
            if(game.developmentMode == null) game.developmentMode = "standard";
            Catalog.refreshContractTier(game);
            Catalog.ensureStarter(game);
            return game;
        }
        catch (JsonProcessingException e) { throw new IllegalStateException("Falha ao ler carreira", e); }
    }
}
