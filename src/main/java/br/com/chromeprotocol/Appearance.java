package br.com.chromeprotocol;

import java.util.Set;

/** Cosmetic choices only. Tokens are validated before they can be persisted. */
public record Appearance(String face, String skin, String hair, String hairColor,
                         String eyes, String outfit, String accent,String beard,String eyeColor,String eyewear,String headwear,String jerseyPattern,String portraitStyle) {
    public Appearance {
        portraitStyle=valid(portraitStyle,"classic",Set.of("classic","studio"));
        face = valid(face, "angular", Set.of("angular", "soft", "broad"));
        skin = valid(skin, "warm", Set.of("porcelain", "sand", "warm", "copper", "umber", "ebony"));
        hair = valid(hair, "undercut", Set.of("undercut", "mohawk", "bob", "braids", "shaved","curly","long","ponytail","afro"));
        hairColor = valid(hairColor, "black", Set.of("black", "silver", "cyan", "violet", "red", "gold","brown","auburn"));
        eyes = valid(eyes, "natural", Set.of("natural", "optic", "visor"));
        outfit = valid(outfit, "runner", Set.of("runner", "armor", "nomad"));
        accent = valid(accent, "cyan", Set.of("cyan", "acid", "magenta", "amber","red","blue","white"));
        beard=valid(beard,"none",Set.of("none","stubble","full","goatee","mustache"));
        eyeColor=valid(eyeColor,"brown",Set.of("brown","hazel","blue","green","gray"));
        eyewear=valid(eyewear,eyes.equals("optic")?"rectangular":"none",Set.of("none","rectangular","round","sun"));
        headwear=valid(headwear,eyes.equals("visor")?"headset":"none",Set.of("none","headset","cap"));
        jerseyPattern=valid(jerseyPattern,"diagonal",Set.of("diagonal","minimal","stripes"));
    }
    public Appearance(String face,String skin,String hair,String hairColor,String eyes,String outfit,String accent){this(face,skin,hair,hairColor,eyes,outfit,accent,null,null,null,null,null,null);}
    public Appearance(String face,String skin,String hair,String hairColor,String eyes,String outfit,String accent,String beard,String eyeColor,String eyewear,String headwear,String jerseyPattern){this(face,skin,hair,hairColor,eyes,outfit,accent,beard,eyeColor,eyewear,headwear,jerseyPattern,null);}
    public static Appearance defaults() {
        return new Appearance(null, null, null, null, null, null, null);
    }
    private static String valid(String value, String fallback, Set<String> allowed) {
        if (value == null) return fallback;
        if (!allowed.contains(value)) throw new IllegalArgumentException("Opção de aparência inválida.");
        return value;
    }
}
