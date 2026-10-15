# Changelog

Todas as alterações do ChromeProtocol são registradas aqui.

## [1.1.0] — 2026-10-15

### Adicionado
- Modo opcional Talento da geração, IGL independente e pool de todos os 29 agentes oficiais de referência.
- Três cartas anuais estáveis para escolher um bônus permanente; coleção e garantia de carta lendária no modo talento.
- Dez investimentos permanentes em treinos, coach, nutrição, psicologia, idiomas, preparo físico e equipamentos, com níveis, preços e ganhos visíveis.
- 48 organizações oficiais do VCT com logos PNG locais e equipes brasileiras de desenvolvimento; script e workflow de atualização de referências.
- Agenda de 13 fases: Kickoff, Masters Santiago/London, Stage 1/2, playoffs, Champions Shanghai, VCB/Challengers e Premier, conforme elegibilidade da equipe.
- Eventos de temporada e rounds variados, com situações inesperadas, liderança, mídia, patrocínio e vida pessoal.
- Estatísticas persistentes por mapa: kills, mortes, assistências, KD, rounds, combatScore e ACS ponderado; números da carreira e temporada separados de scrims.
- Prêmios individuais calculados por desempenho, sala de prêmios e critérios públicos.
- Abas Evolução, Calendário e Estatísticas e botão Nova carreira no topo, com confirmação, exportação e cancelamento sem perda de progresso.
- Migração dos saves 1.0.0 preservando carreira e partidas em andamento, sem fabricar estatísticas antigas.
- Testes de temporadas completas, progressão monotônica, cartas únicas, compras, elegibilidade, estatísticas, migração, reset e navegador desktop/mobile.

### Alterado
- OVR e habilidades nunca diminuem por escolhas, falhas ou idade. Energia, forma, estresse, relações e recursos permanecem dinâmicos.
- Interface mostra ganhos permanentes antes de compras/cartas, logos reais, agenda e estatísticas no resultado imediato do mapa.
- Calendário ancorado em 2026 e adaptado nos anos seguintes; regulamento, propostas e partidas continuam simplificações da simulação.
- Versão, pacote, Docker, workflow e documentação atualizados para 1.1.0.

## [1.0.0] — 2026-10-15

### Alterado
- Substituído o RPG cyberpunk por Clutch Career, modo carreira de e-sports com Valorant.
- Nova interface esportiva com arena ilustrada, player card, agenda, placares e histórico.
- Personalização adaptada para jersey, jaqueta, moletom, óculos e headset, sem implantes ou estética cyberpunk.
- Pacote, Docker, workflow e documentação atualizados para 1.0.0.

### Adicionado
- Carreira de 21 temporadas, dos 15 aos 35 anos, com oito nacionalidades e quatro funções.
- Preparação, peneiras, contratos, duas etapas, vida pessoal, finais/repescagem e balanço anual.
- Organizações, propostas, salários mensais, contratos de uma ou duas temporadas, titular/reserva, renegociação e rescisão.
- Partidas com três rounds-chave; decisões e desempenho acumulado definem placares e resultados.
- Resultado imediato obrigatório após cada escolha, com narrativa e consequências antes/depois, persistindo após reload.
- Evolução de atributos, forma, energia, estresse, química, reputação e seguidores.
- Premiação, despesas por mês, títulos, estatísticas e históricos anual e de decisões.
- Aposentadoria opcional a partir dos 23 e encerramento aos 35 anos.
- Arquivo das campanhas anteriores, com download preservado após criar o jogador.
- Testes de carreira completa nos caminhos de sucesso/falha, contratos, resultados pendentes, arquivo e navegador.
- Autorização do proprietário para merge das próximas alterações após validação, com rollback via revert se necessário.

## [0.2.0] — 2026-10-15

### Adicionado
- Personalização antes de iniciar: rosto, pele, corte e cor de cabelo, implante facial, vestuário e assinatura neon.
- Retrato vetorial em camadas com prévia ao vivo, codinome e opção de gerar outro visual.
- Criação em duas etapas: identidade e história; origem e especialidade mantêm os efeitos de jogo.
- Aparência persistida no servidor e retrato personalizado no perfil da carreira.
- Editor de visual para carreiras existentes, com salvamento por revisão e sem alterar história, atributos, créditos ou calendário.
- Compatibilidade com saves antigos que não possuem aparência, usando valores padrão.
- Testes de personalização, persistência, cancelamento, regras intactas e criação responsiva.

### Alterado
- Identidade visual cyberpunk com paleta lima/ciano, detalhes magenta, painéis de HUD e botões de alto contraste.
- Tela inicial, ilustração noturna de Nova Aurora, favicon, cartões e navegação.
- Layout da criação, retrato e editor adaptados a celular e teclado, com movimento reduzido respeitado.
- Documentação, pacote e imagens Docker atualizados para 0.2.0.

## [0.1.0] — 2026-10-14

### Adicionado
- Projeto Java 17 / Spring Boot com execução via Maven Wrapper e Docker Compose.
- Universo original Nova Aurora, três origens, três especialidades e três facções.
- Campanha Protocolo Chrome com dez contratos, trinta abordagens e três destinos finais com variações.
- Escolhas com probabilidades visíveis, requisitos de implantes, custos e consequências persistentes.
- Rotas desbloqueadas por decisões anteriores e bônus de memória nas chances de sucesso.
- Vida, energia, dinheiro, reputação, procurado, experiência, níveis e pontos de atributo.
- Cinco implantes e limite de três slots permanentes.
- Descanso, trabalho informal, treino, aluguel, dívidas e resgate de emergência.
- Persistência H2, cookie de carreira, proteção CSRF e controle de revisão concorrente.
- Interface PT-BR responsiva: criação, contrato, distrito, clínica e dossiê.
- Resultado do último contrato, linha do tempo e exportação de dossiê para consulta.
- Identidade visual verde/âmbar, ilustração SVG de cidade e favicon próprios.
- Suíte de regressão do motor, testes de API e fluxo de navegador em GitHub Actions.
- Guia de execução, regras, arquitetura, limitações e conservação de saves.
