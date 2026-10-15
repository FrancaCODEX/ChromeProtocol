package br.com.chromeprotocol;

import jakarta.servlet.http.*;
import org.springframework.http.*;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import java.time.Duration;
import java.util.*;

@RestController
@RequestMapping("/api")
public class GameController {
    private final CareerService service;
    public GameController(CareerService service) { this.service = service; }
    public record NewCareer(String name, String nationality, String role, String background, String agent, Integer revision, Appearance appearance, String developmentMode, Boolean igl, java.util.List<String> favoriteAgents) {}
    @GetMapping("/session")
    public Map<String, String> csrf(CsrfToken token) { return Map.of("token", token.getToken(), "header", token.getHeaderName()); }
    @GetMapping("/game")
    public Map<String, Object> get(@CookieValue(name = "CP_CAREER", required = false) String cookie) {
        GameEngine.View game = service.read(id(cookie));
        return game == null ? Map.of("started", false, "legacy", service.legacy(id(cookie))) : Map.of("started", true, "game", game, "archiveAvailable", service.hasArchive(id(cookie)));
    }
    @GetMapping("/catalog")
    public Map<String,Object> catalog() { return Map.of("countries",Catalog.COUNTRIES,"roles",Catalog.ROLES,"backgrounds",Catalog.BACKGROUNDS,"organizations",Catalog.ORGS,"agents",Catalog.AGENTS,"investmentCatalog",Catalog.INVESTMENTS); }
    @GetMapping(value="/archive", produces="application/json")
    public String archive(@CookieValue(name="CP_CAREER",required=false) String cookie) { return service.archive(id(cookie)); }
    @PostMapping("/game")
    public ResponseEntity<GameEngine.View> create(@RequestBody NewCareer body,
             @CookieValue(name = "CP_CAREER", required = false) String cookie, HttpServletRequest request) {
        if (service.read(id(cookie)) != null) throw new IllegalArgumentException("Uma carreira já existe. Use a opção de nova carreira.");
        CareerService.Created created = service.create(body.name(), body.nationality(), body.role(), body.background(), body.agent(), body.appearance(), body.developmentMode(), Boolean.TRUE.equals(body.igl()), body.favoriteAgents(), id(cookie));
        ResponseCookie careerCookie = ResponseCookie.from("CP_CAREER", created.id().toString()).httpOnly(true)
            .secure(request.isSecure()).sameSite("Strict").path("/").maxAge(Duration.ofDays(365)).build();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, careerCookie.toString()).body(created.game());
    }
    @PostMapping("/game/action")
    public GameEngine.View action(@CookieValue(name = "CP_CAREER", required = false) String cookie, @RequestBody GameEngine.Action action) {
        return service.act(id(cookie), action);
    }
    @PostMapping("/game/restart")
    public GameEngine.View restart(@CookieValue(name = "CP_CAREER", required = false) String cookie, @RequestBody NewCareer body) {
        if (body.revision() == null) throw new IllegalArgumentException("Atualize a carreira antes de reiniciar.");
        return service.restart(id(cookie), body.revision(), body.name(), body.nationality(), body.role(), body.background(), body.agent(), body.appearance(), body.developmentMode(), Boolean.TRUE.equals(body.igl()), body.favoriteAgents());
    }
    public record AppearanceChange(Appearance appearance, Integer revision) {}
    @PostMapping("/game/appearance")
    public GameEngine.View customize(@CookieValue(name = "CP_CAREER", required = false) String cookie, @RequestBody AppearanceChange body) {
        if (body.revision() == null) throw new IllegalArgumentException("Atualize a carreira antes de alterar o visual.");
        return service.customize(id(cookie), body.revision(), body.appearance());
    }
    private UUID id(String value) {
        try { return value == null ? null : UUID.fromString(value); }
        catch (IllegalArgumentException e) { return null; }
    }
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> invalid(IllegalArgumentException error) { return Map.of("message", error.getMessage()); }
}
