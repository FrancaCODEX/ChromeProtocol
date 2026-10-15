# Changelog

## 1.6.0 — 2026-10-15

- Interface redesenhada, com navegação arredondada, mercado com identidade e salário em destaque e layouts responsivos.
- Retrato Arena vetorial local, opção de personagem feminina na criação e edição; migração do antigo Estúdio e manutenção do Clássico.
- Propostas vinculadas ao ID estável da organização, corrigindo duplicações e assinatura com clube diferente do exibido.
- Mercado internacional nas quatro regiões, sem bloqueio de idade para a elite na simulação.
- Renovação única com aumento mínimo de 10% e valorização por desempenho; convites Academy → principal com aumento mínimo de 20%, sem multa.
- Ascension regional para equipes independentes classificadas, com promoção ao VCT da região na temporada seguinte.
- Regressões de domínio, API e navegador para identidades das ofertas, contratos, acesso regional e aparência feminina persistida.


## 1.5.0 — 2026-10-15

- Retrato Estúdio com DiceBear/Avataaars local, editor com escolha de estilo e compatibilidade dos retratos clássicos.
- Nove imagens de troféus fornecidas pelo usuário, seleção por competição, galeria de referência e ampliação acessível.
- Renovação garantida após expiração, com um ano adicional, salário mínimo igual ao anterior e escolha ao lado de novas ofertas.
- Titularidade garantida a partir de 80 de OVR: promoção do contrato atual e propostas apenas de titular, preservando séries iniciadas e separando os resultados de scrim da nova campanha oficial.
- Regressões de motor/API para renovação, promoção, persistência e compatibilidade; navegador para troca de estilos, galeria, renovação e layouts desktop/mobile.

Todas as alterações do ChromeProtocol são registradas aqui.

## [1.4.0] — 2026-10-15

### Adicionado
- Ritmo Temporada automática, padrão no formulário: escolher foco, contrato/time, um acontecimento importante e carta anual. Todas as séries são simuladas automaticamente entre essas pausas.
- Balanço, premiações, salários/despesas e avaliação de objetivo automáticos, com gravação atômica e todos os mapas preservados; sem botões de avançar série ou confirmação de resultado na interface automática.
- Painel de escolhas ao lado do histórico anual, com logos, OVR, ACS, K/D, kills, campanha e troféus agrupados; consequências ficam no painel sem interromper a próxima escolha.
- Personalização em abas Rosto/Cabelo/Estilo, com miniaturas: nove cabelos, oito cores de cabelo, cinco barbas, cinco cores de olhos, óculos e acessórios de cabeça independentes, sete cores de uniforme e três estampas de jersey.
- Compatibilidade com os campos cosméticos antigos, tradução de óculos/headset e validação de todos os novos tokens no servidor.
- Testes de carreira automática completa com vitórias e derrotas, limite de quatro escolhas por ano, pagamentos únicos, pausas em contratos/cartas, persistência e edição cosmética sem mudar atributos.

### Alterado
- Ritmo automático substitui a antiga opção Run no formulário e no seletor; saves `run` em blocos continuam funcionando.
- OVR incluído nas consequências e mostrado no painel compacto. Modos detalhado e Talento da geração preservados.
- Pacote, Docker, CI, README e exportação atualizados para 1.4.0.

## [1.3.0] — 2026-10-15

### Adicionado
- Séries MD3 e MD5 com todos os mapas persistidos, identificação da série, modo de resolução e estatísticas completas; contadores separados de séries e mapas.
- Trajetória de elite com Kickoff, grupos regionais de seis equipes, cinco rodadas, classificação, chaves superior/inferior, Masters suíço e Champions com grupos e playoffs; semifinais inferiores e finais MD5.
- Três controles: assumir três rounds-chave no mapa que pode fechar a série, simular o confronto ou avançar rodadas comuns até uma decisão. Todos registram resultados imediatamente.
- Modo Run selecionável na criação ou durante a carreira: simular até quatro séries por bloco, parar em dilemas/fim do período e combinar preparação/teste no início do ano. Contratos, eventos e cartas continuam sob controle do jogador.
- Dilemas entre blocos, seleção estável e sem repetir modelos já vistos no mesmo ano enquanto houver alternativas.
- Treino permanente pela prática a cada quatro séries, inclusive derrotas; desenvolvimento por etapa concluída até os 23 anos, com limite 99 e mesmo equilíbrio nos dois ritmos.
- Ícones SVG próprios e locais para troféus de Champions, Masters e regionais; conquistas ao lado do histórico anual, resultados por etapa e detalhes expansíveis de cada série/mapa.
- Testes de calendário completo de 26 séries na base, classificações da elite, fases MD5, avanço seletivo, intervenção, persistência de blocos, migração e amostra de equilíbrio de dezesseis carreiras sem boost.

### Alterado
- Calendário de base/Premier usa 26 séries próprias (8 + 8 + 10). A elite varia conforme a trajetória; o caminho invicto do modelo tem 33 séries. Não se assume um total oficial universal de 26 ou 34–42.
- Troféus dependem da campanha inteira; receitas por mapa ajustadas ao novo volume e prêmio por série. Salários/despesas continuam por mês, sem cobrança extra por série.
- Saves antigos preservam mapas, números e resultados pendentes; mapa antigo em andamento conclui uma vez, sem fabricar uma MD3 ou estatísticas retroativas.
- Documentação explicita simplificações: sem pontos oficiais, Ascension/promoções, tabela mundial ou simulação integral dos confrontos de outras equipes. Referências Riot e Rift Career, implementação própria.
- Pacote, Docker e CI atualizados para 1.3.0.

## [1.2.0] — 2026-10-15

### Adicionado
- Novo menu de carreira com Continuar, Nova carreira, guia e dossiê do jogador salvo; visual escuro, painéis angulares e destaque vermelho.
- Radar e OVR inicial em tempo real na criação, acompanhando função, origem, IGL e modo Talento da geração; avatar personalizável preservado.
- Aba Minha rotina: quatro atividades estáveis por período escolhidas entre nove tipos de treino, estudo, idioma, comunidade, família, recuperação e convivência.
- Uma atividade opcional por fase, sem avançar o calendário; bloqueio durante mapas em andamento, resultados pendentes e escolha de carta, com validação no servidor e persistência contra repetição por reload.
- Cinco objetivos anuais de carreira, contrato, desempenho, comunicação e comunidade, escolhidos na pré-temporada; avaliação/recompensa automática e única no balanço anual, sem penalidade de habilidade por falha.
- Central do jogador com agenda pessoal, objetivo e relatório de contrato; histórico de objetivos e OVR/receitas por temporada.
- Cinco acontecimentos fora do servidor, totalizando quinze modelos, com conversa com coach, redes sociais, projeto social, contato internacional e equilíbrio de agenda.
- Testes de rotina, metas, recompensas, migração do histórico, radar e fluxo de menu no desktop/celular.

### Alterado
- Escopo centrado no jogador: evolução, rotina e contratos pessoais; elenco e gestão financeira do clube continuam fora do controle do usuário.
- Entrada em carreira existente passa pelo menu de retomada; resultados e cartas pendentes continuam abrindo diretamente para preservar a continuidade.
- Saves 1.0/1.1 continuam compatíveis; OVR de temporadas antigas sem registro aparece como indisponível, sem dados inventados.
- Referências estudadas: criação/painel visual do ValorantCareer e progressão/mercado/histórico do Rift Career. Conteúdo e código próprios; ValorantSimulator não pôde ser inspecionado por bloqueio de permissão salvo no navegador.
- Documentação, pacote, Docker e CI atualizados para 1.2.0.

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
