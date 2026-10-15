# Clutch Career

**O próximo round é seu.** Modo carreira web de e-sports, começando por Valorant. Você cria um jogador de 15 anos e toma decisões dentro e fora do servidor até os 35 anos: nacionalidade, função, peneiras, contratos, partidas e legado.

Este projeto substitui o antigo RPG cyberpunk do repositório ChromeProtocol. O nome do repositório e os caminhos internos Java foram mantidos para facilitar a atualização local. O jogo ativo agora é **Clutch Career 1.4.0**.

## O que dá para jogar

- Até 21 temporadas: dos 15 aos 35 anos, de 2026 a 2046.
- Nove nacionalidades, quatro funções, IGL independente e todos os 29 agentes do catálogo de referência como favoritos.
- Personagem com rosto, pele, cabelo, roupa e acessórios de jogador; prévia em tempo real.
- Três trajetórias iniciais: revelação da ranked, equipe da escola e criador de conteúdo.
- Treze fases por ano: preparação, peneiras, mercado, Kickoff, Masters, Stage 1, eventos, Stage 2, playoffs, Champions, prêmios e balanço.
- Séries MD3/MD5 completas, simulação de rodadas comuns e intervenção em três rounds-chave no mapa que pode decidir a série.
- Ritmos detalhado e Run, com a mesma progressão: blocos de até quatro séries, dilemas entre os jogos e escolhas de contrato/cartas sempre manuais.
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
| Kickoff / Challengers Etapa 1 / Premier Split 1 | Janeiro–Fevereiro | Séries do circuito |
| Masters Santiago | Março | Elite classificada no Kickoff |
| Stage 1 / Challengers Etapa 2 / Premier Split 2 | Abril–Maio | Cinco rodadas VCT + playoffs / oito séries de base |
| Masters London | Junho | Elite classificada no Stage 1 |
| Acontecimento inesperado | Junho–Julho | Vida, equipe, mídia, patrocínio e oportunidades |
| Stage 2 / Challengers Playoffs / Premier Split 3 | Julho–Agosto | Disputa da vaga nos playoffs |
| Playoffs regionais / Série de Acesso | Agosto–Setembro | Rota para Champions conforme equipe |
| Champions Shanghai | Setembro–Outubro | Equipes classificadas |
| Prêmios individuais | Novembro | Reconhecimentos calculados pelos números |
| Balanço e carta permanente | Dezembro | Escolher evolução e depois 1 de 3 cartas |

Cada escolha abre o resultado imediatamente. Recarregar mantém o resultado e não refaz o sorteio. Vencer pelo menos dois dos três rounds-chave vence o mapa. Reservas disputam scrims com números separados, sem vitórias oficiais, classificação ou troféus. Nesta versão, só equipes de elite acessam os playoffs do VCT; promoção/Ascension a partir de Challengers e Academy ainda não é simulada. Sem vaga, você recebe um evento contextual em vez de disputar o campeonato.

A elite tem Kickoff com três vitórias ou três derrotas, grupos regionais de seis equipes com cinco séries, classificação dos quatro primeiros, trajetória em chave superior/inferior e finais MD5. Masters inclui suíço (duas vitórias/derrotas) com entrada direta do campeão regional; Champions inclui grupos e playoffs. Tabelas do grupo do jogador são determinísticas e só mostram resultados das rodadas já concluídas. Chaves adversárias, desempates e vagas são simplificados: não há pontuação de circuito, cross-region seeding ou todos os critérios oficiais. O calendário da base/Premier é próprio do jogo: 26 séries (8 + 8 + 10), com campanha campeã ao ganhar pelo menos 75% da etapa e a última série. Esse número não é um formato oficial do VCB. A elite não tem total anual fixo: a campanha invicta no modelo soma 33 séries; suíço e chaves inferiores acrescentam confrontos, e eliminações reduzem o total. A estimativa de 34–42 não é tratada como regra universal. O calendário regional usa Americas, EMEA, Pacific e CN. VCB identifica Gamers Club Challengers Brazil; outras ligas são simplificadas por região.

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

Próximas expansões possíveis: colegas e treinadores persistentes, pontos de circuito, promoções do Challengers e chave mundial completa com todos os confrontos entre outras organizações.

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

## Menu e rotina pessoal (1.2)

A entrada mostra um menu com **Continuar carreira**, **Nova carreira**, guia e dossiê do jogador salvo. Resultados e cartas pendentes continuam abrindo direto para a decisão correspondente. A criação mantém a personalização e acrescenta radar/OVR inicial ao vivo; a prévia acompanha função, trajetória, IGL e modo de evolução.

**Minha rotina** oferece quatro opções estáveis por período, entre nove atividades: VOD, fundamentos, transmissão, família, duo com colega, estudos, idioma, mentoria e recuperação. Apenas uma atividade pode ser feita em cada fase do ano; não avança o calendário. Não pode ser usada entre rounds-chave ou enquanto uma recompensa anual estiver pendente. Os efeitos e ganhos são exibidos antes da escolha, e o resultado/histórico ficam salvos. A quota é verificada no servidor e não reinicia ao atualizar a página.

Escolha **um objetivo anual** até a janela de contratos: manter contrato ativo, vencer três mapas oficiais, completar três mapas com ACS médio 230, ganhar oito pontos de comunicação ou conquistar 1.500 novos seguidores. As metas relativas começam no momento da escolha. Avaliação e recompensa em dinheiro/atributos são automáticas no balanço anual, antes da carta; cada meta só paga uma vez. Uma meta não cumprida não reduz habilidades. A próxima temporada abre nova escolha. Ganhos respeitam o limite 99 e o modo Talento da geração.

O usuário controla somente o jogador. Não há compra de outros atletas, escalação do quinteto, decisão de demitir colegas ou gestão financeira da organização. A central reúne a agenda pessoal, o objetivo anual e informações do vínculo atual. O histórico registra OVR ao encerrar novas temporadas (antes da carta); temporadas antigas sem esse número mostram um traço.

Referências analisadas em 15/10/2026: [ValorantCareer](https://valorantcareer.com/) para hierarquia visual da criação e radar; [Rift Career](https://riftscore.net/valorant/) para progressão, comparação de propostas e histórico anual. O código, textos, avatar e implementação são próprios. O modo de jogador do ValorantSimulator foi indicado como direção pelo usuário, mas não foi inspecionado: o navegador recusou o domínio por uma preferência de permissão salva, mesmo após nova autorização em mensagem. Não copiamos conteúdo nem atribuímos funções não verificadas ao site.

Continua sendo uma simulação própria: séries e grupos do jogador são registrados, mas chaveamentos das demais equipes, pontos oficiais e vagas de cada liga não são reproduzidos integralmente. Estatísticas são geradas pelo jogo e não refletem partidas reais.

## Séries, Run e troféus (1.3)

Antes de uma série, escolha **Assumir o momento decisivo**, **Simular esta série** ou **Avançar até uma decisão**. O avanço detalhado para antes de estreia, rodada final, classificação, eliminação ou título. Em **Run**, o terceiro botão simula até quatro séries, inclusive decisivas, e para no próximo dilema ou ao encerrar o período. Ele não escolhe contratos, cartas ou respostas de eventos. Trocar de ritmo mantém chances, atributos e todo o progresso. No início de cada ano a Run combina treino e teste em uma ação; as propostas continuam sendo escolhidas pelo jogador.

A intervenção simula a abertura da série, pausa quando alguém chega a um mapa da vitória e oferece três rounds-chave. O resultado do mapa influencia em cinco pontos a chance dos mapas restantes. Todos os mapas, inclusive simulados, ficam salvos com identificação da série, modo, kills, mortes, assistências, KD e ACS. Resultados em bloco podem ser expandidos mapa a mapa. Séries e mapas têm contadores separados. Nenhum mapa de um save antigo vira uma série inventada; um mapa antigo em andamento é concluído uma vez no formato original. Os contadores de séries começam com as séries realmente disputadas após a atualização.

A prática a cada quatro séries dá +1 no atributo principal e +1 em disciplina, mesmo perdendo. Até os 23 anos, concluir uma etapa melhora cada atributo em +1. Ganhos respeitam o limite 99 e o bônus de evolução de Talento da geração. Vitórias em mapas pagam 35 × nível; vitórias em séries, 150 × nível, além do salário e premiações de campeonato. Não existe título por vencer apenas um mapa. A simulação usa atributos, condição, adversário, liderança e bônus permanentes: sorte pode mudar uma campanha, mas derrotas não removem a evolução.

Dilemas usam quinze modelos e evitam repetir um modelo já visto no mesmo ano enquanto houver outro disponível. A rotina opcional, investimentos e metas continuam acessíveis entre ocasiões. Troféus de Champions, Masters e regionais usam três ícones SVG próprios, locais, visíveis na sala e ao lado da temporada vencedora. São interpretações gráficas do jogo, sem reproduzir fotografias oficiais. O histórico mostra OVR, séries V/D, resultados por etapa e renda por ano.

Fontes de formato consultadas em 15/10/2026: [Stage 1 EMEA](https://valorantesports.com/en-GB/news/2026-vct-emea-stage-1-format-dates-and-tickets), [Masters Santiago](https://valorantesports.com/en-GB/news/masters-santiago-everything-you-need-to-know), [Champions Shanghai](https://valorantesports.com/pt-BR/news/champions-shanghai-everything-you-need-to-know). São referências de desenho, não sincronização com competições ao vivo.

## Temporada automática e personalização (1.4)

**Temporada automática** é o ritmo padrão do formulário. Você decide aproximadamente quatro vezes por ano: foco de desenvolvimento, time/contrato, um acontecimento importante no meio do ano e carta de evolução. A escolha do contrato simula automaticamente todas as séries até a pausa do meio do ano; responder ao acontecimento simula o restante, registra premiações, salário/despesas, objetivo e balanço, e oferece as cartas. Não há botões para avançar cada série, rounds obrigatórios, pausas por falta de vaga, tela de prêmios ou confirmação extra por resultado. O resultado fica visível no painel; cada série e mapa continua disponível em Estatísticas/Calendário. Compras e rotina são opcionais nas pausas. A partir dos 23 anos, também é possível encerrar a carreira na pré-temporada.

O modo detalhado continua disponível. O ritmo antigo `run`, em blocos de quatro, continua compatível com saves existentes, mas o botão de ritmo e o formulário oferecem a nova temporada automática. Pode mudar o ritmo entre decisões, preservando contratos, atributos, mapas, dinheiro e conquistas. Recarregar não refaz sorteios nem repete a simulação: resultado, revisão e todos os mapas são gravados na mesma transação. O navegador confirma automaticamente o resultado salvo e mostra as consequências ao lado da próxima decisão; se a conexão interromper a confirmação, o resultado permanece no servidor.

O painel automático mostra escolhas à esquerda e histórico anual à direita, com idade/ano, organização e logo, OVR registrado, ACS, K/D, kills e conquistas. Troféus também aparecem agrupados. O OVR histórico continua sendo o valor de encerramento do ano, antes da carta; não é recalculado a partir dos atributos atuais.

O editor agora tem abas **Rosto**, **Cabelo** e **Estilo**: três rostos, seis peles, nove cabelos, oito cores de cabelo, cinco barbas, cinco cores de olhos, quatro estilos de óculos, três acessórios de cabeça, três roupas, sete cores de uniforme e três estampas de jersey. Óculos e headset podem ser combinados. Cabelos, barbas, acessórios e roupas têm miniaturas. Editar durante a carreira é cosmético e mantém atributos/temporada/contratos. Os sete campos originais continuam aceitos e o servidor traduz óculos/headset antigos para os novos campos; opções inválidas são rejeitadas. As ilustrações SVG são próprias e locais.

As capturas enviadas pelo proprietário e [Rift Career](https://riftscore.net/valorant/) orientaram o fluxo compacto de escolhas e histórico. A captura de ValorantCareer também foi usada como referência visual; o site não retornou conteúdo na ferramenta de consulta nesta revisão. Não foi copiado código de nenhum dos sites.
