package br.com.chromeprotocol;

import java.util.*;

/** Authored content, separated from the game engine and persistence. */
public final class Catalog {
    private Catalog() {}
    public static final Map<String, String> ATTRIBUTES = labels(
        "body", "Corpo", "reflex", "Reflexos", "tech", "Técnica", "intelligence", "Inteligência", "cool", "Sangue-frio");
    public static final Map<String, String> ORIGINS = labels(
        "street", "Cria das ruas", "corpo", "Ex-corporativo", "nomad", "Nômade");
    public static final Map<String, String> SPECIALTIES = labels(
        "netrunner", "Netrunner", "solo", "Combatente", "ghost", "Infiltrador");
    public static final Map<String, String> FACTIONS = labels(
        "helix", "Helix Systems", "ghosts", "Fantasmas", "district", "Distrito Livre");

    public record Effect(int money, int health, int reputation, int heat, String faction, int affinity, String flag) {}
    public record Choice(String id, String title, String description, String attribute, int difficulty,
                         String implant, String flagRequired, int price, int energy,
                         Effect success, Effect failure, String successText, String failureText) {}
    public record Mission(String id, String title, String district, String contact, String tag,
                          String description, String memoryFlag, String memoryText, List<Choice> choices) {}
    public record Implant(String id, String name, String attribute, int bonus, int price, String description) {}
    public static final List<Implant> IMPLANTS = List.of(
        new Implant("deck", "Cyberdeck Íris", "intelligence", 2, 550, "Invada redes e abra rotas digitais exclusivas."),
        new Implant("arms", "Braços Atlas", "body", 2, 650, "Potência hidráulica para abrir portas e encarar confrontos."),
        new Implant("legs", "Tendões Sprint", "reflex", 2, 500, "Reflexos assistidos e rotas de fuga pelos telhados."),
        new Implant("skin", "Pele Espectral", "cool", 2, 600, "Camuflagem ativa para desaparecer da vigilância."),
        new Implant("link", "Interface de Campo", "tech", 2, 450, "Conecte-se a máquinas e sabote sistemas de segurança.")
    );

    static Map<String, String> labels(String... values) {
        Map<String, String> result = new LinkedHashMap<>();
        for (int i = 0; i < values.length; i += 2) result.put(values[i], values[i + 1]);
        return Collections.unmodifiableMap(result);
    }
    static Effect e(int cash, int hp, int rep, int heat, String faction, int affinity, String flag) {
        return new Effect(cash, hp, rep, heat, faction, affinity, flag);
    }
    static Choice c(String id, String title, String description, String attr, int difficulty, String implant,
                    String required, int price, int energy, Effect success, Effect failure, String ok, String fail) {
        return new Choice(id, title, description, attr, difficulty, implant, required, price, energy, success, failure, ok, fail);
    }
    static Mission m(String id, String title, String district, String contact, String tag, String text,
                     String flag, String memory, Choice... choices) {
        return new Mission(id, title, district, contact, tag, text, flag, memory, List.of(choices));
    }

    public static final List<Mission> MISSIONS = List.of(
        m("delivery", "Um nome no asfalto", "Baixada Neon", "Mara • intermediária", "ENTREGA",
          "Mara deixa um pacote lacrado no balcão do bar Frequência. O destino é uma clínica clandestina. Um drone da Helix bloqueia a rua. Seu primeiro contrato parece simples demais.", "", "",
          c("alley", "Passar pelas vielas", "Uma rota discreta entre vendedores e câmeras quebradas.", "cool", 10, "", "", 0, 15,
            e(280, 0, 3, 3, "district", 4, "delivery"), e(120, -12, 1, 8, "district", 1, ""),
            "O pacote chega intacto. Mara salva seu contato: agora você existe na cidade.", "O drone identifica seu rosto. Você entrega com atraso e recebe só parte do valor."),
          c("bribe", "Pagar pela passagem", "O fiscal aceita 80 créditos para olhar para o outro lado. Sem teste.", "", 0, "", "", 80, 10,
            e(280, 0, 2, 0, "helix", 2, "delivery"), e(0, 0, 0, 0, "", 0, ""), "A cancela sobe. Na cidade, até a indiferença tem preço.", ""),
          c("roof", "Correr pelos telhados", "O caminho é rápido. A queda também.", "reflex", 14, "", "", 0, 20,
            e(340, 0, 4, 4, "ghosts", 3, "delivery"), e(100, -20, 0, 6, "", 0, ""), "Você chega antes do previsto. Alguém nos telhados observa sua habilidade.", "Um salto curto demais. A clínica trata sua queda e desconta parte do pagamento.")),
        m("chip", "O chip que sangra", "Baixada Neon", "Mara • intermediária", "DILEMA",
          "Você encontra Lia, a ladra do chip que deveria recuperar. Ela mostra registros de experiências da Helix em moradores. O contratante quer os dados. Lia quer continuar viva.", "delivery", "Mara lembra da entrega bem-sucedida e confia esta missão a você.",
          c("return", "Cumprir o contrato", "Entregue o chip. A Helix paga bem pelo silêncio.", "", 0, "", "", 0, 15,
            e(600, 0, 4, -3, "helix", 15, "sold_chip"), e(0, 0, 0, 0, "", 0, ""), "Lia desaparece na multidão. A transferência chega antes que você saia do distrito.", ""),
          c("protect", "Proteger Lia", "Ajude a ladra a fugir com as provas. O pagamento será menor.", "", 0, "", "", 0, 20,
            e(220, 0, 6, 10, "district", 15, "lia"), e(0, 0, 0, 0, "", 0, ""), "Lia deixa um canal seguro. Você perdeu um cliente e ganhou uma aliada.", ""),
          c("leak", "Vazar as provas", "Entre na rede e publique o arquivo sem revelar sua posição.", "intelligence", 15, "", "", 0, 25,
            e(350, 0, 10, 15, "ghosts", 15, "leak"), e(100, -10, 0, 20, "helix", -10, "exposed"), "Os telões exibem as provas. Esta noite, a cidade não consegue desviar o olhar.", "O firewall rastreia sua conexão. As provas somem, mas seu rosto fica no sistema.")),
        m("clinic", "Carne de reposição", "Mercado Ferrugem", "Dra. Kira • cirurgiã", "RESGATE",
          "Um carregamento de implantes foi tomado por saqueadores. Kira precisa dele para manter a clínica aberta. Você encontra o caminhão num ferro-velho cercado.", "lia", "Lia envia o horário da troca de guarda. A proteção que você ofereceu começa a voltar.",
          c("negotiate", "Negociar a carga", "Ofereça um acordo e convença o líder a liberar os implantes.", "cool", 15, "", "", 100, 15,
            e(600, 0, 5, 0, "district", 8, "clinic"), e(180, -8, 0, 5, "", 0, ""), "Kira recebe a carga. A clínica passa a reconhecer você como alguém confiável.", "A negociação termina em ameaça. Você recupera apenas uma caixa."),
          c("force", "Abrir caminho à força", "Enfrente os saqueadores na entrada.", "body", 18, "", "", 0, 25,
            e(650, -10, 7, 10, "ghosts", 5, "clinic"), e(150, -30, 1, 15, "", 0, ""), "Você recupera o caminhão, com marcas do confronto no corpo.", "A resistência é maior do que parecia. Você foge com o que consegue carregar."),
          c("lia_route", "Usar o contato de Lia", "Ela conhece a entrada de manutenção. Requer ter protegido Lia.", "", 0, "", "lia", 0, 10,
            e(600, 0, 6, -3, "district", 12, "clinic"), e(0, 0, 0, 0, "", 0, ""), "Lia guia você por baixo do ferro-velho. A carga sai sem um disparo.", "")),
        m("tower", "Um andar acima", "Coroa de Vidro", "Soren • executivo", "INFILTRAÇÃO",
          "Soren quer um registro de auditoria antes que seu próprio empregador o destrua. O arquivo está numa torre Helix. Você precisa atravessar uma recepção onde todos têm um preço.", "sold_chip", "Seu nome está na lista de prestadores da Helix desde a entrega do chip.",
          c("cover", "Vestir uma identidade falsa", "Entre como consultor e sustente a história.", "cool", 22, "", "", 80, 20,
            e(850, 0, 6, 5, "helix", 6, "audit"), e(200, -15, 0, 15, "helix", -5, ""), "O elevador fecha antes que a recepcionista note a inconsistência.", "Seu crachá falha. A fuga deixa uma nova imagem sua na vigilância."),
          c("network", "Entrar pelo sistema", "O Cyberdeck Íris permite acessar o arquivo do lado de fora.", "intelligence", 18, "deck", "", 0, 20,
            e(950, 0, 8, 5, "ghosts", 10, "audit"), e(200, -5, 1, 20, "helix", -8, ""), "O arquivo sai da torre sem que você atravesse uma porta.", "Um programa de defesa queima sua conexão. Você salva apenas uma parte do arquivo."),
          c("insider", "Acionar sua credencial Helix", "Use a confiança adquirida ao entregar o chip. Sem teste.", "", 0, "", "sold_chip", 0, 15,
            e(750, 0, 4, 0, "helix", 8, "audit"), e(0, 0, 0, 0, "", 0, ""), "A corporação abre a porta para quem já demonstrou lealdade. Você copia a auditoria.", "")),
        m("blackout", "A noite sem neon", "Baixada Neon", "Davi • Distrito Livre", "SABOTAGEM",
          "A Helix ameaça cortar a energia da Baixada. Davi pede que você assuma a subestação. Um representante corporativo oferece dinheiro para deixar tudo como está.", "leak", "Os moradores reconhecem quem expôs os experimentos. Davi recebe você com confiança.",
          c("repair", "Reconectar a Baixada", "Reconfigure os distribuidores antes que a segurança chegue.", "tech", 22, "", "", 0, 25,
            e(550, 0, 10, 12, "district", 15, "power"), e(150, -20, 2, 12, "district", 3, ""), "As janelas acendem uma a uma. Seu nome circula pelas ruas.", "A rede suporta só parte da carga. Você evita o pior, mas a região continua instável."),
          c("deal", "Aceitar a proposta Helix", "Entregue a subestação e receba pelo serviço.", "", 0, "", "", 0, 10,
            e(1000, 0, 2, -10, "helix", 18, "blackout"), e(0, 0, 0, 0, "", 0, ""), "O dinheiro chega. Atrás de você, a Baixada escurece.", ""),
          c("hardware", "Sabotar o bloqueio", "Use sua Interface de Campo para cortar o controle remoto.", "tech", 15, "link", "", 0, 20,
            e(650, 0, 12, 8, "district", 20, "power"), e(150, -12, 1, 10, "", 0, ""), "O sistema perde o controle da subestação. A energia permanece com o distrito.", "A defesa elétrica detecta a interface. Você recua antes de danificar o implante.")),
        m("convoy", "Ferro em movimento", "Mercado Ferrugem", "Rook • Fantasmas", "ESCOLTA",
          "Um comboio leva testemunhas para fora da cidade. Rook precisa de alguém na retaguarda. A rota atravessa um bloqueio de drones.", "lia", "Lia está entre as testemunhas. Ela reconhece você antes de entrar no veículo.",
          c("drive", "Conduzir pelo canal seco", "Velocidade e precisão para escapar dos drones.", "reflex", 22, "", "", 0, 25,
            e(800, -8, 8, 10, "ghosts", 15, "convoy"), e(200, -25, 1, 15, "", 0, ""), "O último veículo cruza o portão. Rook passa a tratar você como parceiro.", "Um drone acerta o motor. Parte do grupo precisa abandonar a rota."),
          c("shield", "Segurar o bloqueio", "Braços Atlas permitem afastar as barreiras reforçadas.", "body", 18, "arms", "", 0, 30,
            e(900, -15, 10, 15, "ghosts", 18, "convoy"), e(200, -30, 2, 20, "", 0, ""), "Você abre espaço entre as barreiras. O comboio passa enquanto o aço ainda range.", "A blindagem não cede a tempo. Você sai ferido, com poucos veículos a salvo."),
          c("toll", "Comprar uma rota segura", "Pague 250 créditos aos contrabandistas. Sem teste.", "", 0, "", "", 250, 15,
            e(700, 0, 5, -5, "ghosts", 8, "convoy"), e(0, 0, 0, 0, "", 0, ""), "O comboio atravessa um túnel fora dos mapas. Todos chegam ao outro lado.", "")),
        m("ghost", "O preço de um fantasma", "Coroa de Vidro", "Mara • intermediária", "CAÇADA",
          "Um caçador vende sua localização num leilão clandestino. Mara encontrou o servidor que hospeda as ofertas. Você pode apagá-las, assumir outra identidade ou enfrentar o vendedor.", "exposed", "A identificação deixada no vazamento fracassado está entre os arquivos à venda.",
          c("erase", "Apagar seu registro", "Invada o servidor e destrua os backups.", "intelligence", 24, "", "", 0, 25,
            e(500, 0, 8, -25, "ghosts", 8, "erased"), e(100, -15, 1, 15, "", 0, ""), "Seu rosto desaparece dos anúncios. Por algumas horas, você volta a ser ninguém.", "O servidor cria uma cópia antes de cair. Seu preço acaba de aumentar."),
          c("cloak", "Trocar de identidade", "Pele Espectral engana a vigilância. Sem teste.", "", 0, "skin", "", 100, 10,
            e(450, 0, 5, -30, "ghosts", 5, "erased"), e(0, 0, 0, 0, "", 0, ""), "Uma silhueta diferente passa pelas câmeras. O caçador perde seu rastro.", ""),
          c("hunt", "Confrontar o caçador", "Dê ao vendedor uma razão para encerrar o leilão.", "body", 24, "", "", 0, 30,
            e(800, -15, 10, 10, "ghosts", 10, "hunter"), e(150, -35, 0, 20, "", 0, ""), "O leilão termina. Na próxima vez, ele vai pensar antes de usar seu nome.", "Ele estava esperando você. A saída custa sangue e mais uma dívida com Mara.")),
        m("archive", "Protocolo Chrome", "Coroa de Vidro", "Dra. Kira • cirurgiã", "REVELAÇÃO",
          "A auditoria aponta para o Protocolo Chrome: a Helix pode desligar implantes à distância. Kira localizou a chave de controle. O futuro de milhares de pessoas cabe num único arquivo.", "audit", "A auditoria recuperada por você revela uma rota de acesso ao laboratório.",
          c("hack", "Extrair a chave pela rede", "Enfrente o sistema central. O Cyberdeck abre esta rota.", "intelligence", 25, "deck", "", 0, 30,
            e(1100, -10, 12, 20, "ghosts", 12, "key"), e(250, -25, 2, 20, "helix", -10, ""), "A chave está no seu dispositivo. Pela primeira vez, a Helix não controla todas as peças.", "Você extrai registros, mas a chave é movida antes do fim da conexão."),
          c("break", "Entrar pelo laboratório", "Cruze a segurança física e retire o módulo de controle.", "reflex", 25, "", "", 0, 30,
            e(1000, -15, 12, 25, "district", 12, "key"), e(250, -30, 2, 20, "", 0, ""), "Você sai com o módulo de controle enquanto o alarme cobre a torre.", "O laboratório é selado. Você escapa com provas, mas sem o módulo."),
          c("kira", "Pedir ajuda à clínica", "Kira abre uma rota usando dispositivos dos pacientes. Requer ter recuperado a carga.", "", 0, "", "clinic", 250, 20,
            e(900, 0, 10, 15, "district", 15, "key"), e(0, 0, 0, 0, "", 0, ""), "A rede de Kira atravessa o bloqueio. A chave chega através de centenas de sinais anônimos.", "")),
        m("offer", "A cidade faz uma oferta", "Mercado Ferrugem", "Soren • executivo", "LEALDADE",
          "Soren oferece proteção e um contrato permanente. Davi quer mobilizar as ruas. Rook pode tirar você da cidade. Todos precisam de uma resposta antes do amanhecer.", "power", "A Baixada mantém as luzes acesas graças a você. Davi não esqueceu.",
          c("helix", "Assinar com a Helix", "Poder e estabilidade, com seu nome num contrato corporativo.", "", 0, "", "", 0, 10,
            e(1400, 0, 5, -20, "helix", 25, "pledge_helix"), e(0, 0, 0, 0, "", 0, ""), "Soren entrega uma credencial definitiva. Você já não é apenas um prestador.", ""),
          c("district", "Ficar com o Distrito Livre", "Prepare a resistência e abra mão do contrato.", "", 0, "", "", 0, 20,
            e(400, 0, 12, 15, "district", 25, "pledge_district"), e(0, 0, 0, 0, "", 0, ""), "Davi distribui seus planos pela Baixada. Agora a responsabilidade também é sua.", ""),
          c("ghosts", "Preparar sua saída", "Rook reserva espaço no próximo comboio.", "", 0, "", "", 150, 10,
            e(600, 0, 6, -10, "ghosts", 20, "pledge_ghosts"), e(0, 0, 0, 0, "", 0, ""), "Um veículo espera além do mercado. Pela primeira vez, sair parece possível.", "")),
        m("finale", "Quem fica depois do neon", "Nova Aurora", "Você • última decisão", "FINAL",
          "O sinal de controle será ativado ao amanhecer. A cidade aguarda sua decisão. Seus contatos, suas dívidas e seus contratos chegam juntos a esta última noite.", "key", "Você tem a chave do Protocolo Chrome. Desta vez, o destino da rede está nas suas mãos.",
          c("free", "Libertar os implantes", "Destrua o controle remoto. Requer a chave do Protocolo Chrome.", "", 0, "", "key", 0, 20,
            e(0, 0, 20, 20, "district", 20, "ending_free"), e(0, 0, 0, 0, "", 0, ""), "A rede cai. Milhares de implantes continuam funcionando, agora sem um dono remoto.", ""),
          c("power", "Entregar o protocolo à Helix", "Negocie um lugar no topo. Suas escolhas anteriores mudam o acordo.", "", 0, "", "", 0, 10,
            e(2500, 0, 8, -30, "helix", 20, "ending_power"), e(0, 0, 0, 0, "", 0, ""), "A torre abre suas portas. A cidade continua lá embaixo, com as mesmas luzes e novos donos.", ""),
          c("leave", "Desaparecer na estrada", "Saia com o que conseguiu guardar. Rook e Lia lembram de suas decisões.", "", 0, "", "", 0, 10,
            e(0, 0, 3, -40, "ghosts", 10, "ending_leave"), e(0, 0, 0, 0, "", 0, ""), "Nova Aurora vira uma faixa de luz no retrovisor. Pela primeira vez, o próximo dia não tem um contrato." , ""))
    );
}
