package br.com.chromeprotocol;

import java.util.*;
import java.util.random.RandomGenerator;

/** Pure domain engine. No database, HTTP, or framework dependency. */
public final class GameEngine {
    private final RandomGenerator random;
    public GameEngine(RandomGenerator random) { this.random = Objects.requireNonNull(random); }
    public record Action(String type, String target, int revision) {}
    public record ChoiceView(String id, String title, String description, String attribute,
                             int chance, int reward, int price, int energy, boolean available, String blockedReason) {}
    public record MissionView(String id, String title, String district, String contact, String tag,
                              String description, String memory, List<ChoiceView> choices) {}
    public record View(GameState character, MissionView mission, List<Catalog.Implant> shop,
                       Map<String, String> attributes, Map<String, String> factions, int totalMissions) {}

    public GameState create(String name, String origin, String specialty) {
        return create(name, origin, specialty, Appearance.defaults());
    }
    public GameState create(String name, String origin, String specialty, Appearance appearance) {
        require(name != null && !name.isBlank() && name.strip().length() <= 24, "Use um nome de 1 a 24 caracteres.");
        require(name.strip().codePoints().noneMatch(Character::isISOControl), "O nome contém caracteres inválidos.");
        require(Catalog.ORIGINS.containsKey(origin), "Escolha uma origem válida.");
        require(Catalog.SPECIALTIES.containsKey(specialty), "Escolha uma especialidade válida.");
        GameState s = new GameState();
        s.name = name.strip(); s.origin = origin; s.specialty = specialty;
        s.appearance = appearance == null ? Appearance.defaults() : appearance;
        Catalog.ATTRIBUTES.keySet().forEach(key -> s.attributes.put(key, 3));
        Catalog.FACTIONS.keySet().forEach(key -> s.factions.put(key, 0));
        s.money = switch (origin) { case "corpo" -> 800; case "nomad" -> 450; default -> 500; };
        String originStat = switch (origin) { case "corpo" -> "intelligence"; case "nomad" -> "tech"; default -> "cool"; };
        s.attributes.merge(originStat, 1, Integer::sum);
        s.factions.put(switch (origin) { case "corpo" -> "helix"; case "nomad" -> "ghosts"; default -> "district"; }, 5);
        s.attributes.merge(switch (specialty) { case "netrunner" -> "intelligence"; case "solo" -> "body"; default -> "cool"; }, 2, Integer::sum);
        log(s, "Bem-vindo a Nova Aurora", "Mara tem um primeiro contrato para você. O aluguel custa 120 créditos a cada sete dias. A cidade não espera.", "info");
        return s;
    }
    public void customize(GameState s, Appearance appearance, int revision) {
        require(revision == s.revision, "Sua carreira mudou em outra aba. Atualize antes de continuar.");
        require(appearance != null, "Escolha uma aparência válida.");
        s.appearance = appearance;
        s.revision++;
    }
    public View view(GameState s) {
        if (s.appearance == null) s.appearance = Appearance.defaults();
        MissionView mission = null;
        if (!s.ended) {
            Catalog.Mission m = Catalog.MISSIONS.get(s.missionIndex);
            mission = new MissionView(m.id(), m.title(), m.district(), m.contact(), m.tag(), m.description(),
                s.flags.contains(m.memoryFlag()) ? m.memoryText() : "",
                m.choices().stream().map(c -> {
                    String reason = blocked(s, c);
                    return new ChoiceView(c.id(), c.title(), c.description(), Catalog.ATTRIBUTES.getOrDefault(c.attribute(), "Sem teste"),
                        chance(s, m, c), c.success().money(), c.price(), c.energy(), reason.isEmpty(), reason);
                }).toList());
        }
        return new View(s, mission, Catalog.IMPLANTS, Catalog.ATTRIBUTES, Catalog.FACTIONS, Catalog.MISSIONS.size());
    }
    public void act(GameState s, Action action) {
        require(action != null, "Ação inválida.");
        require(action.revision() == s.revision, "Sua carreira mudou em outra aba. Atualize antes de continuar.");
        require(!s.ended, "Esta carreira terminou. Comece uma nova história.");
        require(action.type() != null, "Escolha uma ação.");
        switch (action.type()) {
            case "choice" -> choose(s, action.target());
            case "rest" -> {
                s.energy = Math.min(100, s.energy + 45); s.health = Math.min(100, s.health + 25);
                s.heat = Math.max(0, s.heat - 8);
                log(s, "Uma noite de silêncio", "Você recuperou até 45 de energia e 25 de vida. A vigilância esfriou.", "good");
                advance(s);
            }
            case "work" -> {
                require(s.energy >= 15, "Você precisa de 15 de energia. Descanse primeiro.");
                s.energy -= 15; s.money += 180; s.heat = Math.max(0, s.heat - 3); gainXp(s, 15);
                log(s, "Trabalho fora do radar", "Manutenção e entregas renderam 180 créditos. Nem todo dia precisa virar uma lenda.", "good");
                advance(s);
            }
            case "train" -> {
                require(Catalog.ATTRIBUTES.containsKey(action.target()), "Atributo inválido.");
                require(s.attributes.get(action.target()) < 10, "Este atributo já chegou ao limite.");
                require(s.energy >= 20 && s.money >= 150, "Treinar exige 20 de energia e 150 créditos.");
                s.energy -= 20; s.money -= 150; s.attributes.merge(action.target(), 1, Integer::sum);
                log(s, "Treino: " + Catalog.ATTRIBUTES.get(action.target()), "Atributo base +1. Os implantes adicionam seus bônus separadamente.", "good");
                advance(s);
            }
            case "upgrade" -> {
                require(Catalog.ATTRIBUTES.containsKey(action.target()), "Atributo inválido.");
                require(s.skillPoints > 0 && s.attributes.get(action.target()) < 10, "Sem pontos disponíveis ou atributo no limite.");
                s.skillPoints--; s.attributes.merge(action.target(), 1, Integer::sum);
                log(s, "Nova habilidade", Catalog.ATTRIBUTES.get(action.target()) + " +1. Ponto de nível utilizado.", "good");
            }
            case "buy" -> {
                Catalog.Implant implant = Catalog.IMPLANTS.stream().filter(i -> i.id().equals(action.target())).findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Implante inválido."));
                require(!s.implants.contains(implant.id()), "Você já instalou este implante.");
                require(s.implants.size() < 3, "Você tem três slots de implantes. Escolha com cuidado.");
                require(s.money >= implant.price(), "Créditos insuficientes.");
                s.money -= implant.price(); s.implants.add(implant.id());
                log(s, "Implante instalado", implant.name() + ": +" + implant.bonus() + " em " + Catalog.ATTRIBUTES.get(implant.attribute()) + ".", "good");
            }
            case "debt" -> {
                require(s.debt > 0 && s.money >= s.debt, "Você precisa do valor total para quitar a dívida.");
                int payment = s.debt; s.money -= payment; s.debt = 0;
                log(s, "Contas em dia", "Você quitou " + payment + " créditos em dívidas.", "good");
            }
            default -> throw new IllegalArgumentException("Ação desconhecida.");
        }
        s.revision++;
    }
    private void choose(GameState s, String id) {
        Catalog.Mission m = Catalog.MISSIONS.get(s.missionIndex);
        Catalog.Choice c = m.choices().stream().filter(choice -> choice.id().equals(id)).findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Esta escolha não pertence à missão atual."));
        String reason = blocked(s, c); require(reason.isEmpty(), reason);
        int odds = chance(s, m, c);
        boolean success = c.attribute().isEmpty() || random.nextInt(100) < odds;
        s.money -= c.price(); s.energy -= c.energy();
        Catalog.Effect effect = success ? c.success() : c.failure();
        s.money = Math.max(0, s.money + effect.money());
        s.health = Math.max(0, Math.min(100, s.health + effect.health()));
        s.reputation += effect.reputation(); s.heat = clamp(s.heat + effect.heat(), 0, 100);
        if (!effect.faction().isEmpty()) s.factions.compute(effect.faction(), (key, old) -> clamp(old + effect.affinity(), -100, 100));
        if (!effect.flag().isEmpty()) s.flags.add(effect.flag());
        if (effect.flag().equals("sold_chip")) s.factions.merge("district", -10, Integer::sum);
        if (effect.flag().equals("blackout")) s.factions.merge("district", -15, Integer::sum);
        gainXp(s, success ? 55 : 30);
        String text = (success ? c.successText() : c.failureText()) + " Créditos: +" + effect.money()
            + (c.price() > 0 ? " (custo: " + c.price() + ")" : "") + ".";
        log(s, m.title() + " • " + c.title(), text, success ? "good" : "bad");
        s.missionIndex++;
        advance(s);
        if (s.missionIndex >= Catalog.MISSIONS.size()) finish(s);
    }
    private String blocked(GameState s, Catalog.Choice c) {
        if (!c.flagRequired().isEmpty() && !s.flags.contains(c.flagRequired())) return "Depende de uma decisão anterior: " + c.description();
        if (!c.implant().isEmpty() && !s.implants.contains(c.implant())) {
            return "Requer " + Catalog.IMPLANTS.stream().filter(i -> i.id().equals(c.implant())).findFirst().orElseThrow().name() + ".";
        }
        if (s.money < c.price()) return "Você precisa de " + c.price() + " créditos.";
        if (s.energy < c.energy()) return "Você precisa de " + c.energy() + " de energia. Descanse no distrito.";
        return "";
    }
    public int effective(GameState s, String attribute) {
        return s.attributes.getOrDefault(attribute, 0) + Catalog.IMPLANTS.stream().filter(i -> s.implants.contains(i.id()) && i.attribute().equals(attribute))
            .mapToInt(Catalog.Implant::bonus).sum();
    }
    private int chance(GameState s, Catalog.Mission m, Catalog.Choice c) {
        if (c.attribute().isEmpty()) return 100;
        int memoryBonus = !m.memoryFlag().isEmpty() && s.flags.contains(m.memoryFlag()) ? 8 : 0;
        return clamp(55 + effective(s, c.attribute()) * 7 - c.difficulty() + memoryBonus - s.heat / 10, 25, 95);
    }
    private void gainXp(GameState s, int amount) {
        s.xp += amount;
        while (s.xp >= 100) {
            s.xp -= 100; s.level++; s.skillPoints++;
            log(s, "Nível " + s.level, "Você ganhou um ponto para distribuir nos atributos do dossiê.", "good");
        }
    }
    private void advance(GameState s) {
        if (s.health == 0) {
            int paid = Math.min(s.money, 200); s.money -= paid; s.debt += 200 - paid; s.health = 40;
            log(s, "Resgate de emergência", "Kira estabilizou você. Custo de 200 créditos; o que faltou virou dívida. A carreira continua.", "bad");
        }
        s.day++;
        if ((s.day - 1) % 7 == 0) {
            int paid = Math.min(s.money, 120); s.money -= paid; s.debt += 120 - paid;
            log(s, "Aluguel semanal", paid == 120 ? "120 créditos pagos. Seu abrigo continua garantido." : "Parte do aluguel virou dívida. Você pode quitá-la no distrito.", paid == 120 ? "info" : "bad");
        }
        if (s.heat >= 70) {
            s.energy = Math.max(0, s.energy - 5);
            log(s, "Vigilância elevada", "Patrulhas obrigam você a mudar a rota: -5 de energia. Descansar reduz o nível de procurado.", "bad");
        }
    }
    private void finish(GameState s) {
        s.ended = true;
        if (s.flags.contains("ending_free")) {
            s.ending = "A cidade sem coleira";
            log(s, s.ending, "Você libertou a rede de implantes." + (s.flags.contains("power") ? " A Baixada iluminada transmite seu nome." : " A resistência encontra um novo símbolo.")
                + (s.flags.contains("lia") ? " Lia permanece ao seu lado para reconstruir o distrito." : " Algumas pessoas que você deixou para trás não estarão aqui para comemorar."), "good");
        } else if (s.flags.contains("ending_power")) {
            s.ending = s.flags.contains("pledge_helix") ? "Um lugar no conselho" : "O novo intermediário";
            if (s.flags.contains("pledge_helix")) s.money += 1500;
            log(s, s.ending, s.flags.contains("pledge_helix") ? "Sua lealdade rendeu um cargo e um bônus de 1.500 créditos. A Helix mantém o protocolo. Você agora assina as ordens." : "Você recebe um contrato, mas o conselho mantém distância. A Helix controla a cidade; você negocia o seu lugar nela.", "info");
        } else {
            s.ending = s.flags.contains("convoy") && s.flags.contains("pledge_ghosts") ? "Além da última luz" : "Uma estrada sem nome";
            log(s, s.ending, (s.flags.contains("convoy") ? "Rook lembra do comboio que você salvou e garante a passagem." : "A saída é solitária, sem garantias.")
                + (s.flags.contains("lia") ? " Lia envia uma última mensagem: 'Você me deu um futuro. Espero que encontre o seu.'" : " O rádio fica em silêncio.")
                + (s.debt > 0 ? " Suas dívidas ficaram na cidade, mas alguém ainda pode cobrá-las." : " Você deixa as contas em dia."), "good");
        }
    }
    private static int clamp(int value, int min, int max) { return Math.max(min, Math.min(max, value)); }
    private static void require(boolean condition, String message) { if (!condition) throw new IllegalArgumentException(message); }
    private static void log(GameState s, String title, String text, String tone) {
        s.journal.add(0, new GameState.LogEntry(s.day, title, text, tone));
        if (s.journal.size() > 100) s.journal.remove(s.journal.size() - 1);
    }
}
