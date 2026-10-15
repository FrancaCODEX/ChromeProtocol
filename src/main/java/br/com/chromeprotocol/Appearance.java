package br.com.chromeprotocol;

import java.util.Set;

/** Cosmetic choices only. Tokens are validated before they can be persisted. */
public record Appearance(String face, String skin, String hair, String hairColor,
                         String eyes, String outfit, String accent) {
    public Appearance {
        face = valid(face, "angular", Set.of("angular", "soft", "broad"));
        skin = valid(skin, "warm", Set.of("porcelain", "sand", "warm", "copper", "umber", "ebony"));
        hair = valid(hair, "undercut", Set.of("undercut", "mohawk", "bob", "braids", "shaved"));
        hairColor = valid(hairColor, "silver", Set.of("black", "silver", "cyan", "violet", "red", "gold"));
        eyes = valid(eyes, "optic", Set.of("natural", "optic", "visor"));
        outfit = valid(outfit, "runner", Set.of("runner", "armor", "nomad"));
        accent = valid(accent, "cyan", Set.of("cyan", "acid", "magenta", "amber"));
    }
    public static Appearance defaults() {
        return new Appearance(null, null, null, null, null, null, null);
    }
    private static String valid(String value, String fallback, Set<String> allowed) {
        if (value == null) return fallback;
        if (!allowed.contains(value)) throw new IllegalArgumentException("Opção de aparência inválida.");
        return value;
    }
}
