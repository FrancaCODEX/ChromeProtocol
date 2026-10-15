# Clutch Career

**O próximo round é seu.** Modo carreira web de e-sports, começando por Valorant. Você cria um jogador de 15 anos e toma decisões dentro e fora do servidor até os 35 anos: nacionalidade, função, peneiras, contratos, partidas e legado.

Este projeto substitui o antigo RPG cyberpunk do repositório ChromeProtocol. O nome do repositório e os caminhos internos Java foram mantidos para facilitar a atualização local. O jogo ativo agora é **Clutch Career 1.0.0**.

## O que dá para jogar

- Até 21 temporadas: dos 15 aos 35 anos, de 2026 a 2046.
- Oito nacionalidades, quatro funções e agentes favoritos por função.
- Personagem com rosto, pele, cabelo, roupa e acessórios de jogador; prévia em tempo real.
- Três trajetórias iniciais: revelação da ranked, equipe da escola e criador de conteúdo.
- Preparação, peneiras, mercado, duas etapas competitivas, vida pessoal, decisão da temporada e balanço anual.
- Três mapas por temporada, cada um com três rounds-chave escolhidos pelo jogador.
- Resultado imediato persistente após cada decisão, com narrativa, placar e valores antes/depois. O próximo evento só é liberado ao continuar.
- Mercado com propostas distintas de salário, duração e vaga de titular/reserva.
- Academias e equipes fictícias; LOUD, FURIA, MIBR e Sentinels no nível elite da simulação.
- Contratos de uma ou duas temporadas, renegociação e liberação por dois salários.
- Mira, leitura de jogo, comunicação, disciplina, controle emocional, forma, energia, química, estresse, reputação e seguidores.
- Sala de troféus, histórico anual e diário de decisões.
- Aposentadoria opcional a partir dos 23 anos e encerramento obrigatório aos 35.
- Salvamento automático, exportação de dossiê e interface mobile.

É uma simulação de fã não oficial, sem afiliação com Riot Games ou as organizações. Calendários, elencos, valores, critérios de idade e elegibilidade são fictícios; não reproduzem o regulamento real do VCT. Os nomes e funções de agentes são referências ao jogo Valorant. Os salários usam reais em todas as regiões para simplificar a comparação.

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
| Preparação | Janeiro | Mecânica, estudo ou coletivo |
| Peneiras | Fevereiro | Destaque, jogo em equipe ou consistência |
| Contratos | Março | Comparar propostas, cumprir vínculo, renegociar ou rescindir |
| Etapa 1 | Abril–Junho | Três rounds-chave de um mapa |
| Fora do servidor | Julho | Imagem, grupo ou vida pessoal |
| Etapa 2 | Agosto–Outubro | Três rounds-chave de outro mapa |
| Decisão da temporada | Novembro | Final se venceu as duas etapas; repescagem caso contrário |
| Balanço anual | Dezembro | Desenvolvimento, marca, descanso ou aposentadoria |

Cada escolha abre o resultado imediatamente. A janela fica aberta após recarregar a página; confirmar “Continuar carreira” não refaz o sorteio. Nos mapas, vencer pelo menos dois dos três rounds-chave vence o mapa. Reservas disputam scrims de avaliação, sem troféu do circuito. Vencer a repescagem também não concede um título.

Os salários são creditados pelos meses concluídos, junto das despesas: R$ 50/mês antes dos 18 anos e R$ 300/mês depois, limitadas ao saldo. O calendário avança entre fases; os três rounds de um mapa pertencem à mesma fase. Escolhas sem teste são garantidas. As demais mostram a chance exata usada no servidor, calculada pelos atributos, condição do jogador, química e progresso do mapa.

As oportunidades de elite exigem 18 anos, reputação 55 e overall 48. Challengers exige 17 anos e reputação 18. Esses limites são regras próprias desta simulação. Nacionalidade prioriza a região do mercado; a rotação anual também oferece mudanças de região.

## Salvamento e campanhas antigas

O banco continua em `./data/chromeprotocol`, com cookie HttpOnly `CP_CAREER`. Mantenha cookie e banco para retomar o progresso; não há login/recuperação de conta nesta versão. Resultados, salários e ofertas são calculados no Java, com CSRF e revisão/bloqueio de registro para impedir ações duplicadas.

Campanhas cyberpunk não são reinterpretadas como jogadores. A interface identifica o save antigo e oferece iniciar uma carreira de Valorant. O registro antigo é preservado no banco e pode ser baixado antes de começar ou, depois, no Histórico → Campanha anterior. O dossiê JSON serve para consulta; não há importação automática.

“Nova carreira” substitui a carreira de Valorant somente após confirmar o formulário. Editar jogador muda apenas o visual.

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

Próximas expansões possíveis: campeonatos com grupos, colegas e treinadores persistentes, estatísticas de abates/ACS e novas modalidades.

Toda alteração entra em [CHANGELOG.md](CHANGELOG.md). Mudanças validadas podem ser integradas à main conforme autorização do proprietário.

## Retorno à versão anterior

O último commit da main antes desta transformação é `72c64ae67bd442b5c256727dc77aee8d0d3c10f5` (versão cyberpunk 0.2.0). O PR da mudança fica no histórico para reversão por revert, sem reescrever a branch. Ao voltar versões, use o salvamento compatível; o banco preserva as campanhas antigas.
