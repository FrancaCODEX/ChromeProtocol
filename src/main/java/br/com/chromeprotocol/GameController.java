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
    public record NewCareer(String name, String origin, String specialty, Integer revision) {}
    @GetMapping("/session")
    public Map<String, String> csrf(CsrfToken token) { return Map.of("token", token.getToken(), "header", token.getHeaderName()); }
    @GetMapping("/game")
    public Map<String, Object> get(@CookieValue(name = "CP_CAREER", required = false) String cookie) {
        GameEngine.View game = service.read(id(cookie));
        return game == null ? Map.of("started", false) : Map.of("started", true, "game", game);
    }
    @PostMapping("/game")
    public ResponseEntity<GameEngine.View> create(@RequestBody NewCareer body,
             @CookieValue(name = "CP_CAREER", required = false) String cookie, HttpServletRequest request) {
        if (service.read(id(cookie)) != null) throw new IllegalArgumentException("Uma carreira já existe. Use a opção de nova carreira.");
        CareerService.Created created = service.create(body.name(), body.origin(), body.specialty());
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
        return service.restart(id(cookie), body.revision(), body.name(), body.origin(), body.specialty());
    }
    private UUID id(String value) {
        try { return value == null ? null : UUID.fromString(value); }
        catch (IllegalArgumentException e) { return null; }
    }
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> invalid(IllegalArgumentException error) { return Map.of("message", error.getMessage()); }
}
