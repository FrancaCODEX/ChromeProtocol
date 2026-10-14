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
        return id == null ? null : repository.findById(id).map(c -> engine.view(decode(c.state))).orElse(null);
    }
    @Transactional
    public Created create(String name, String origin, String specialty) {
        GameState state = engine.create(name, origin, specialty);
        UUID id = UUID.randomUUID(); repository.save(new Career(id, encode(state)));
        return new Created(id, engine.view(state));
    }
    @Transactional
    public GameEngine.View act(UUID id, GameEngine.Action action) {
        Career career = load(id); GameState state = decode(career.state);
        engine.act(state, action); career.state = encode(state);
        return engine.view(state);
    }
    @Transactional
    public GameEngine.View restart(UUID id, int revision, String name, String origin, String specialty) {
        Career career = load(id); GameState old = decode(career.state);
        if (old.revision != revision) throw new IllegalArgumentException("Sua carreira mudou. Atualize antes de reiniciar.");
        GameState state = engine.create(name, origin, specialty); state.revision = old.revision + 1;
        career.state = encode(state); return engine.view(state);
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
        try { return mapper.readValue(state, GameState.class); }
        catch (JsonProcessingException e) { throw new IllegalStateException("Falha ao ler carreira", e); }
    }
}
