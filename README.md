# Clutch Career

**O próximo round é seu.** Modo carreira web de e-sports, começando por Valorant. Você cria um jogador de 15 anos e toma decisões dentro e fora do servidor até os 35 anos: nacionalidade, função, peneiras, contratos, partidas e legado.

Este projeto substitui o antigo RPG cyberpunk do repositório ChromeProtocol. O nome do repositório e os caminhos internos Java foram mantidos para facilitar a atualização local. O jogo ativo agora é **Clutch Career 1.1.0**.

## O que dá para jogar

- Até 21 temporadas: dos 15 aos 35 anos, de 2026 a 2046.
- Nove nacionalidades, quatro funções, IGL independente e todos os 29 agentes do catálogo de referência como favoritos.
- Personagem com rosto, pele, cabelo, roupa e acessórios de jogador; prévia em tempo real.
- Três trajetórias iniciais: revelação da ranked, equipe da escola e criador de conteúdo.
- Treze fases por ano: preparação, peneiras, mercado, Kickoff, Masters, Stage 1, eventos, Stage 2, playoffs, Champions, prêmios e balanço.
- Mapas representativos das competições elegíveis, cada um com três rounds-chave escolhidos pelo jogador.
- Resultado imediato persistente após cada decisão, com narrativa, placar e valores antes/depois. O próximo evento só é liberado ao continuar.
- Mercado com propostas distintas de salário, duração e vaga de titular/reserva.
- 48 organizações reais do VCT, com logos locais da fonte oficial, mais equipes brasileiras de desenvolvimento.
- Contratos de uma ou duas temporadas, renegociação e liberação por dois salários.
- Mira, leitura de jogo, comunicação, disciplina, controle emocional, forma, energia, química, estresse, reputação e seguidores.
- Sala de troféus, prêmios individuais, kills/mortes/assistências por mapa, KD, ACS, histórico anual e diário de decisões.
- Modo Talento da geração: +20 atributos iniciais, +8 pontos de chance, +20 reputação, 1.000 seguidores e evolução positiva 50% maior (arredondada para cima).
- Uma de três cartas anuais com grandes bônus permanentes e +1 ponto de chance (limite acumulado de 15).
- Dez investimentos com níveis: treino de mira, coach, nutrição, psicologia, mouse, monitor, PC, bootcamp, idiomas e preparo físico. Compras não avançam o tempo.
- Habilidades e OVR nunca diminuem, inclusive depois dos 28 anos. Condição e recursos continuam variando.
- Aposentadoria opcional a partir dos 23 anos e encerramento obrigatório aos 35.
- Salvamento automático, exportação de dossiê e interface mobile.

É uma simulação de fã não oficial, sem afiliação com Riot Games ou as organizações. Os nomes e a estrutura das competições usam 2026 como referência; anos futuros adaptam esse calendário. Elencos, valores, ofertas, resultados e critérios simplificados de idade/elegibilidade pertencem à simulação. Não há calendário ao vivo ou reprodução integral do regulamento do VCT. Os nomes e funções de agentes são referências ao jogo Valorant. Os salários usam reais em todas as regiões para simplificar a comparação.

## Jogar localmente

Com JDK 17 no IntelliJ/PowerShell:

```powershell
git switch main
git pull origin main
.\mvnw.cmd spring-boot:run
```

Linux/WSL:

```bash
./mvnw spring-boot:run
```

Abra **http://localhost:8080**. O Maven Wrapper baixa as dependências na primeira execução.

Ou, com Docker Desktop em execução:

```bash
docker compose up --build
```

O volume `chromeprotocol-data` mantém o banco. `docker compose down` preserva os dados; adicionar `-v` apaga o volume. Nenhuma hospedagem foi configurada.

## O ritmo de uma temporada

| Fase | Período | Decisões |
|---|---|---|
| Preparação, peneiras e contratos | Janeiro | Preparar fundamentos e negociar |
| Kickoff / Challengers Etapa 1 / Premier Split 1 | Janeiro–Fevereiro | Mapa representativo do circuito |
| Masters Santiago | Março | Elite classificada no Kickoff |
| Stage 1 / Challengers Etapa 2 / Premier Split 2 | Abril–Maio | Mapa representativo |
| Masters London | Junho | Elite classificada no Stage 1 |
| Acontecimento inesperado | Junho–Julho | Vida, equipe, mídia, patrocínio e oportunidades |
| Stage 2 / Challengers Playoffs / Premier Split 3 | Julho–Agosto | Disputa da vaga nos playoffs |
| Playoffs regionais / Série de Acesso | Agosto–Setembro | Rota para Champions conforme equipe |
| Champions Shanghai | Setembro–Outubro | Equipes classificadas |
| Prêmios individuais | Novembro | Reconhecimentos calculados pelos números |
| Balanço e carta permanente | Dezembro | Escolher evolução e depois 1 de 3 cartas |

Cada escolha abre o resultado imediatamente. Recarregar mantém o resultado e não refaz o sorteio. Vencer pelo menos dois dos três rounds-chave vence o mapa. Reservas disputam scrims com números separados, sem vitórias oficiais, classificação ou troféus. Equipes Academy não acessam a rota de Challengers para os playoffs do VCT. Sem vaga, você recebe um evento contextual em vez de disputar o campeonato.

A simulação representa cada etapa com um mapa decisivo: não simula grupos, séries MD3/MD5, tabelas, pontos de circuito ou todas as vagas reais. O calendário regional usa Americas, EMEA, Pacific e CN. VCB identifica Gamers Club Challengers Brazil; outras ligas são simplificadas por região.

Os salários são creditados pelos meses concluídos, junto das despesas: R$ 50/mês antes dos 18 anos e R$ 300/mês depois, limitadas ao saldo. O calendário avança entre fases; os três rounds de um mapa pertencem à mesma fase. Escolhas sem teste são garantidas. As demais mostram a chance exata usada no servidor, calculada pelos atributos, condição do jogador, química e progresso do mapa.

As oportunidades de elite exigem 18 anos, reputação 40 e overall 45. Challengers exige 16 anos e reputação 14. Esses limites são regras próprias desta simulação. Nacionalidade prioriza a região do mercado; as propostas locais têm prioridade e rodam entre organizações a cada ano.

## Salvamento e campanhas antigas

O banco continua em `./data/chromeprotocol`, com cookie HttpOnly `CP_CAREER`. Mantenha cookie e banco para retomar o progresso; não há login/recuperação de conta nesta versão. Resultados, salários e ofertas são calculados no Java, com CSRF e revisão/bloqueio de registro para impedir ações duplicadas.

Campanhas cyberpunk não são reinterpretadas como jogadores. A interface identifica o save antigo e oferece iniciar uma carreira de Valorant. O registro antigo é preservado no banco e pode ser baixado antes de começar ou, depois, no Histórico → Campanha anterior. O dossiê JSON serve para consulta; não há importação automática.

Salvamentos anteriores de Clutch Career são migrados preservando jogador, idade, atributos, contrato, histórico, revisão e mapa em andamento. Números de mapas antigos não são inventados; as novas estatísticas começam com os mapas jogados após a atualização.

O botão **Nova carreira** no topo abre uma confirmação com opção de baixar o dossiê. Voltar ou cancelar mantém todo o progresso. Confirmar o novo formulário substitui a carreira de Valorant atual. Editar jogador muda apenas o visual.

## Tecnologia e testes

Java 17 · Spring Boot 3.5.16 · Spring Web/Security/Data JPA · H2 · HTML/CSS/JavaScript e SVG locais.

- `Catalog.java`: nacionalidades, funções, agentes, organizações, ofertas e calendário.
- `GameEngine.java`: decisões, probabilidades, contratos, partidas, resultados e temporadas.
- `GameState.java`: carreira, contrato, mapa, resultados e histórico serializáveis.
- `CareerService.java`: persistência transacional e arquivo das campanhas antigas.
- `static/`: interface, retratos, ilustração da arena e tema de e-sports.

```bash
./mvnw verify
./scripts/test-domain.sh
```

A suíte do motor percorre carreiras completas ganhando e perdendo, além de contratos, rescisão, resultados pendentes e aposentadoria. O script de domínio funciona sem baixar dependências, com um runtime Java contendo o módulo de compilação.

Com a aplicação em execução, os testes do navegador usam:

```bash
npm install
npx playwright install chromium
npm run test:browser
```

GitHub Actions executa motor/API, empacotamento e Chromium, publicando relatórios e capturas em `verification-evidence`.

## Limites atuais e próximos passos

Simulação single-player de decisões. Ainda não há partidas jogáveis em FPS, roster individual de colegas, ligas com tabela completa, transferências negociadas com outros jogadores, multiplayer ou outros e-sports. Mapas representam momentos selecionados; não são uma reprodução round a round de uma partida real.

Próximas expansões possíveis: campeonatos com grupos, colegas e treinadores persistentes, novas modalidades e circuitos com grupos, séries e pontos de classificação.

Toda alteração entra em [CHANGELOG.md](CHANGELOG.md). Mudanças validadas podem ser integradas à main conforme autorização do proprietário.

## Retorno à versão anterior

O último commit da main antes desta transformação é `72c64ae67bd442b5c256727dc77aee8d0d3c10f5` (versão cyberpunk 0.2.0). O PR da mudança fica no histórico para reversão por revert, sem reescrever a branch. Ao voltar versões, use o salvamento compatível; o banco preserva as campanhas antigas.

## Catálogo e fontes de referência

Snapshot de referência consultado em 15/10/2026. Não há conexão com serviços de Valorant durante o jogo; imagens são servidas localmente, sem rastreamento externo.

- [Agentes oficiais](https://playvalorant.com/pt-br/agents/): 29 nomes e funções, incluindo Miks, Veto, Waylay e Tejo.
- [VCT 2026: tudo sobre a temporada](https://valorantesports.com/pt-BR/news/vct-2026-season-start-eyntk): regiões, Kickoff, Masters Santiago/London, Stage 1/2, Champions Shanghai e via de acesso dos Challengers.
- [Challengers Brazil 2026 — Etapa 2](https://valorantesports.com/pt-BR/news/tudo-sobre-a-etapa-2-do-gamers-club-challengers-brazil-2026): nomenclatura e equipes brasileiras de desenvolvimento.
- [Global Power Rankings](https://valorantesports.com/en-US/gpr): nomes, regiões e arquivos PNG originais das 48 organizações do VCT. Proveniência em `reference-teams.json`; downloads em `scripts/sync-reference-assets.py`. Marcas pertencem aos titulares. Equipes sem logo local usam somente texto.
- [Ask VALORANT #7](https://playvalorant.com/en-us/news/dev/ask-valorant-7/) e [Patch 1.11](https://playvalorant.com/en-us/news/game-updates/valorant-patch-notes-1-11/): referência histórica de combate/assistências. O ACS solicitado pelo jogo é uma aproximação simulada, calculada a partir do combatScore por round, sem telemetria real; não equivale ao Performance Score atual do cliente.

Os bônus já estão incluídos no atributo mostrado (limite 99). Não são somados outra vez ao calcular o OVR. A aba Estatísticas agrega apenas mapas oficiais; o filtro mostra também scrims identificadas. O ACS geral soma combatScore e divide pelo total de rounds, enquanto KD usa os totais de kills e mortes. Nenhuma média é obtida somando médias de mapas. Prêmios são conquistas desta simulação; os critérios ficam visíveis na interface.
