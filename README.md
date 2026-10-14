# ChromeProtocol

**Seu futuro tem um preço.** RPG web narrativo de vida e carreira em Nova Aurora, uma cidade cyberpunk original. Jogue por escolhas: aceite contratos, ganhe créditos, instale implantes e decida com quem ficar.

## Primeira versão — 0.1.0

- Criação de personagem: três origens e três especialidades.
- Cinco atributos, experiência, níveis e pontos de habilidade.
- Dez contratos autorais com três escolhas cada, testes de atributo e consequências persistentes.
- Relações com Helix Systems, Fantasmas e Distrito Livre.
- Cinco implantes; três slots permanentes e rotas exclusivas.
- Vida, energia, dinheiro, procurado, aluguel semanal e dívidas.
- Descanso, trabalhos informais, treinamento e resgate de emergência.
- Três destinos finais, com variações de acordo com a carreira.
- Salvamento automático no servidor; retomada pelo mesmo navegador.
- Dossiê com linha do tempo e exportação JSON para consulta (não é um backup importável).
- Interface PT-BR responsiva, SVGs locais e nenhuma dependência de APIs de IA.

Universo original inspirado no gênero cyberpunk. Não é uma adaptação oficial de Cyberpunk 2077. As mecânicas foram simplificadas para um jogo de navegador.

## Rodar com Docker (mais simples)

Precisa de Docker Desktop com o engine em execução.

```bash
docker compose up --build
```

Abra **http://localhost:8080**. O volume `chromeprotocol-data` guarda as carreiras. `docker compose down` preserva os dados; `docker compose down -v` apaga o banco.

## Rodar com Java

Precisa de **JDK 17** (ou compatível). O Maven Wrapper baixa o Maven e as dependências na primeira execução.

Windows / PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

Linux / WSL:

```bash
./mvnw spring-boot:run
```

Abra **http://localhost:8080**. Para trocar a porta no PowerShell:

```powershell
$env:PORT = "8081"
.\mvnw.cmd spring-boot:run
```

## Como jogar

1. Crie um codinome, escolha a origem e a especialidade.
2. Leia o contrato e confira as chances, custos e requisitos das escolhas.
3. Confirme a decisão: o contrato avança e o resultado é salvo.
4. Use o distrito para recuperar vida/energia, ganhar dinheiro e treinar.
5. Instale implantes na clínica para aumentar atributos e abrir rotas.
6. Consulte o dossiê para distribuir pontos de nível e acompanhar consequências.

Ações de missão, descanso, trabalho e treino avançam um dia. Compra de implante, distribuição de pontos e pagamento de dívida não avançam o relógio. Aluguel de 120 créditos nos dias 8, 15, 22 etc. Valores não pagos viram dívida. Vida zero gera resgate de 200 créditos e recuperação para 40 de vida, sem encerrar a história.

Chance de sucesso: `55 + atributo efetivo × 7 − dificuldade + bônus de memória − procurado / 10`, limitada a 25–95%. Ações sem teste têm 100%. Decisões prévias podem adicionar 8 pontos percentuais. A interface mostra a mesma chance usada pelo motor.

## Salvamento

O banco H2 fica em `./data` por padrão. Um cookie `CP_CAREER` aleatório, HttpOnly e SameSite Strict vincula o navegador à carreira. Atualizar a página ou reiniciar o servidor mantém o progresso se o cookie e o banco forem preservados. Outro navegador inicia uma carreira independente.

Sem login ou recuperação de carreira nesta versão. Limpar cookies perde o vínculo com a carreira. “Nova carreira” substitui o salvamento após a confirmação do formulário; voltar sem enviar preserva a história anterior.

O backend aceita somente IDs de ações e a revisão atual: dinheiro e resultados são calculados no Java. Bloqueio de registro e controle de revisão impedem gastar recursos duas vezes em requisições concorrentes. Mutações exigem token CSRF da sessão.

## Tecnologias e organização

Java 17 · Spring Boot 3.5.16 · Spring Web · Spring Security · Spring Data JPA · H2 · HTML/CSS/JavaScript sem bundler.

| Arquivo | Responsabilidade |
|---|---|
| `Catalog.java` | Missões, escolhas, efeitos, origens e implantes |
| `GameEngine.java` | Regras, probabilidades, economia e finais |
| `GameState.java` | Estado serializável da carreira |
| `CareerService.java` | Persistência transacional e bloqueio por carreira |
| `GameController.java` | API e cookie de identidade |
| `WebSecurity.java` | CSRF e política de conteúdo |
| `static/` | Interface, identidade visual e SVGs |

Adicionar conteúdo não exige mudar a interface: novas missões e escolhas entram em `Catalog`. Alterações no catálogo que reordenem missões exigem considerar a migração dos saves existentes, pois a campanha utiliza índice de missão nesta versão.

## Testes

```bash
./mvnw verify
```

Motor sem dependências externas, com JDK 17:

```bash
./scripts/test-domain.sh
```

Verificação de navegador com a aplicação em execução:

```bash
npm install
npx playwright install chromium
npm run test:browser
```

A workflow `Verify ChromeProtocol` executa testes de domínio/API, empacotamento e fluxos reais em Chromium; publica relatórios e capturas como artefato. Consulte o resultado em Actions antes de integrar a branch.

## Escopo atual

Campanha single-player finita, dez contratos, escolhas por cards. Ainda sem combate em turnos separado, mapa interativo, romances, apartamentos compráveis, multiplayer ou contas de usuário. O combate dos contratos é resolvido por testes de atributo.

Próximos passos sugeridos: expandir os contratos em etapas, introduzir inventário e equipamentos, adicionar eventos de vida e melhorar a recuperação dos saves.

Para hospedagem pública, preparar HTTPS, backup do banco, limites de requisição e identidade recuperável. O Docker Compose entregue é para execução local; nenhum serviço de hospedagem foi configurado.

Toda alteração deve constar em [CHANGELOG.md](CHANGELOG.md).
