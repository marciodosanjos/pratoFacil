# PratoFácil — API REST para Gestão de Pedidos Locais

> 🌐 **Aplicação no ar (deploy em nuvem):** **https://pratofacil.onrender.com**
> *(Render, plano gratuito — o primeiro acesso pode levar ~30s para "acordar".)*

Projeto da disciplina **Sistemas Distribuídos e Mobile** (Avaliação A3). É uma
aplicação **Java + Spring Boot 4** que ajuda pequenos empreendedores do ramo
alimentício a gerenciar o ciclo de vida dos pedidos (cardápio, pedidos e status de
entrega), aplicando na prática conceitos de sistemas distribuídos: arquitetura
cliente-servidor, API REST, protocolo HTTP, middleware, transparência, persistência
de dados, **integração com webservice externo (pagamento)** e serviços em nuvem.
Funciona como um pequeno **marketplace**: cada loja tem seu próprio cardápio e o
cliente escolhe em qual loja deseja pedir. O cardápio é organizado por **categorias**
e o pedido é montado em um **carrinho**, com seletor de **quantidade** por item e
**pagamento** (PIX ou cartão) ao final.

## Problemática

Pequenos empreendedores que produzem e entregam alimentos costumam controlar as
vendas de forma manual (caderno, WhatsApp), o que gera perda de informação e erros
nos pedidos. O PratoFácil centraliza esse controle em um sistema simples e desacoplado.

## Requisitos (escopo)

- **RF01** — listar o cardápio disponível.
- **RF02** — o cliente realiza um pedido via requisição HTTP.
- **RF03** — o empreendedor atualiza o status do pedido (*Em preparo → Saiu para entrega → Entregue*).

## Tecnologias

- **Java 21**
- **Spring Boot 4** (Spring Web MVC, Spring Data JPA, Spring Security, Thymeleaf)
- **Banco de dados:** H2 em **arquivo** (perfil `dev` — os dados persistem entre reinícios) e **PostgreSQL em nuvem / Render** (perfil `prod`); H2 em memória nos testes
- **Pagamentos:** integração com o gateway **Asaas** (sandbox) — cobrança **PIX** (QR Code) + confirmação assíncrona via **webhook**
- **Deploy:** **Docker** (build multi-stage) + **Render** (`Dockerfile` e `render.yaml` incluídos)
- **springdoc-openapi** (Swagger UI / OpenAPI 3)
- **Maven** (wrapper `mvnw` incluído)
- Testes: JUnit 5 + Spring MockMvc

## Arquitetura

Padrão **MVC** com **camada de serviço** isolando as regras de negócio. O mesmo núcleo
é exposto por duas interfaces:

- **API REST** (`/api/...`): troca dados em **JSON**, consumível por qualquer cliente HTTP (ex.: Postman).
- **Interface web** (Thymeleaf): páginas HTML responsivas para uso no navegador.

```
Cliente (Postman / navegador / app)  ->  HTTP  ->  API REST Spring Boot
     ->  Camada de Serviço (regras de negócio)  ->  Spring Data JPA  ->  Banco de Dados
                                              \->  HTTP  ->  Gateway de pagamento (Asaas)
```

## Perfis, papéis e lojas

- **Perfis:** `dev` (H2 em arquivo, padrão — dados persistem) e `prod` (PostgreSQL em nuvem via variáveis de ambiente).
- **Papéis:** `ADMIN` (lojista, dono de uma loja) e `CLIENTE` (cliente final, com conta). Senhas com hash BCrypt.
- **Marketplace:** o cliente entra na **vitrine de lojas** (`/lojas`) e escolhe onde pedir; cada lojista gerencia apenas a própria loja (cardápio, pedidos e dashboard).

Lojas de exemplo criadas na primeira execução (cada uma com seu cardápio):

| Loja | Login (ADMIN) | Senha |
|---|---|---|
| Comida de Vó | `comidadavo@pratofacil.com` | `vovo123` |
| Mãozinha Burger | `maozinhaburger@pratofacil.com` | `burger123` |
| Forno Italiano | `fornoitaliano@pratofacil.com` | `pizza123` |
| Império do Açaí | `imperiodoacai@pratofacil.com` | `acai123` |

> **Cliente:** não há cliente pré-cadastrado. Crie uma conta em `/cadastro`
> (escolha **Cliente**) para testar o fluxo de pedido e pagamento. Não é preciso
> nenhuma chave para rodar localmente — sem o Asaas, o pagamento opera em modo simulação.

## Pagamentos (PIX e cartão)

Ao finalizar o pedido, o cliente é levado a uma **tela de pagamento** com duas opções:

- **PIX** — quando a integração com o **Asaas** está configurada, o app cria a cobrança
  e exibe o **QR Code** e o **código copia-e-cola** reais (consumindo a API do Asaas).
  O **webhook** `/webhooks/asaas` recebe o evento de pagamento e marca o pedido como
  **Pago** automaticamente.
- **Cartão de crédito** — formulário de checkout (ambiente de testes; nenhum dado de
  cartão é armazenado).

Em seguida é exibida a **tela de confirmação** ("Pagamento confirmado!"). A integração
é **opcional e degrada com elegância**: sem a chave do Asaas, o app funciona como uma
**simulação** de pagamento (útil para rodar localmente sem dependências externas).

A chave fica na variável de ambiente `ASAAS_API_KEY` (nunca no repositório). Opcionalmente,
`ASAAS_WEBHOOK_TOKEN` valida o token enviado pelo Asaas no webhook.

## Como executar

Pré-requisito: **Java 21**.

```bash
# perfil dev (H2 em arquivo), na raiz do projeto
./mvnw spring-boot:run
```

A aplicação sobe em `http://localhost:8080`.

| Recurso | URL |
|---|---|
| Vitrine de lojas (cliente) | `http://localhost:8080/lojas` |
| Cadastro / Login | `http://localhost:8080/cadastro` · `/login` |
| Painel do lojista | `http://localhost:8080/admin` |
| Dashboard | `http://localhost:8080/admin/dashboard` |
| Swagger (documentação da API) | `http://localhost:8080/swagger-ui.html` |
| Console H2 (perfil dev) | `http://localhost:8080/h2-console` |

> No console H2, use **JDBC URL** `jdbc:h2:file:./data/pratofacildb`, usuário `sa` e
> senha em branco.

Para testar o **PIX real** localmente, rode com a chave do Asaas (sandbox):

```bash
# Windows PowerShell
$env:ASAAS_API_KEY = "sua_chave_sandbox"
./mvnw spring-boot:run
```

> O webhook só alcança uma URL **pública**; localmente conclua o pagamento pelo botão
> "Confirmar pagamento" (ou exponha o app com um túnel). O fluxo automático completo
> (webhook) é testado no deploy.

### Executar com PostgreSQL em nuvem (perfil `prod`)

Defina as variáveis de ambiente e ative o perfil `prod`:

```bash
export SPRING_PROFILES_ACTIVE=prod
export SPRING_DATASOURCE_URL="jdbc:postgresql://SEU_HOST/SEU_BANCO?sslmode=require"
export SPRING_DATASOURCE_USERNAME="usuario"
export SPRING_DATASOURCE_PASSWORD="senha"
./mvnw spring-boot:run
```

> As credenciais **nunca** ficam no repositório — apenas em variáveis de ambiente.

## Deploy (Render + Docker)

A aplicação está **em produção na nuvem**: **https://pratofacil.onrender.com** — o app
(contêiner **Docker**) e o **PostgreSQL** rodam no **Render**, validando na prática os
conceitos de serviços em nuvem e transparência de localização. O deploy usa o
`Dockerfile` (build multi-stage) e o `render.yaml` (Blueprint), e o passo a passo está
em [`docs/DEPLOY.md`](docs/DEPLOY.md): Web Service runtime Docker, com
`SPRING_PROFILES_ACTIVE=prod`, as variáveis `SPRING_DATASOURCE_*` (Postgres) e, para
pagamentos, `ASAAS_API_KEY`.

## Endpoints da API REST

Base: `http://localhost:8080/api`

| Método | Rota | Descrição | Respostas | Acesso |
|--------|------|-----------|-----------|--------|
| GET    | `/api/pratos`              | Lista o cardápio (RF01)             | 200             | Público |
| GET    | `/api/pratos/{id}`         | Busca um prato                      | 200 / 404       | Público |
| POST   | `/api/pratos`              | Cadastra um prato                   | 201 / 400 / 401 | ADMIN |
| PUT    | `/api/pratos/{id}`         | Atualiza um prato                   | 200 / 400 / 404 | ADMIN |
| DELETE | `/api/pratos/{id}`         | Remove um prato                     | 204 / 404 / 409 | ADMIN |
| GET    | `/api/pedidos`             | Lista os pedidos                    | 200             | Autenticado |
| GET    | `/api/pedidos/{id}`        | Busca um pedido                     | 200 / 404       | Autenticado |
| POST   | `/api/pedidos`             | Cria um pedido (RF02)               | 201 / 400 / 401 | CLIENTE |
| PUT    | `/api/pedidos/{id}/status` | Atualiza o status do pedido (RF03)  | 200 / 404       | ADMIN |
| POST   | `/webhooks/asaas`          | Recebe eventos de pagamento (Asaas) | 200             | Público (token opcional) |

O valor total do pedido é **calculado no servidor** a partir dos pratos enviados.
Status possíveis: `EM_PREPARO`, `SAIU_PARA_ENTREGA`, `ENTREGUE`.

### Exemplos (cURL)

```bash
# Cadastrar um prato na loja do lojista autenticado (HTTP Basic)
curl -u comidadavo@pratofacil.com:vovo123 -X POST http://localhost:8080/api/pratos \
  -H "Content-Type: application/json" \
  -d '{"nome":"Feijoada","descricao":"Completa","preco":39.90}'

# Listar o cardápio (público)
curl http://localhost:8080/api/pratos

# Atualizar o status do pedido 1 (ADMIN)
curl -u comidadavo@pratofacil.com:vovo123 \
  -X PUT "http://localhost:8080/api/pedidos/1/status?status=SAIU_PARA_ENTREGA"
```

## Tratamento de erros

As respostas de erro da API vêm em JSON padronizado (sem stacktrace):

```json
{ "status": 404, "error": "Not Found", "message": "Prato não encontrado: 999" }
```

- **400 Bad Request** — dados inválidos (ex.: prato sem nome ou preço).
- **401 Unauthorized** — operação que exige autenticação.
- **404 Not Found** — recurso inexistente.
- **409 Conflict** — tentativa de remover um prato vinculado a um pedido.

## Testes

```bash
./mvnw test
```

Inclui testes de integração (MockMvc) cobrindo cardápio público, segurança (401),
papéis (ADMIN), erros (400/404) e criação de pedido com valor total calculado. Os
testes rodam em um banco H2 **em memória** isolado (perfil `test`), sem afetar os
dados de desenvolvimento.

## Estrutura do projeto

```
src/main/java/com/example/vendeFacil
├── controller   # Controllers REST (/api), Web (Thymeleaf), Auth, Perfil, Dashboard, webhook e erros
├── service      # Regras de negócio (Cardapio, Pedido, Usuario, Pagamento, Asaas, MetodoPagamento)
├── repository   # Repositórios Spring Data JPA
├── model        # Entidades JPA (Loja, Cardapio, Pedido, ItemPedido, Usuario, MetodoPagamento)
│                #   e enums (Status, Role, Categoria, StatusPagamento, TipoPagamento)
├── dto          # Objetos de transferência (PedidoRequest)
├── util         # Utilitários (processamento da logo da loja)
├── exception    # Exceções de domínio (RecursoNaoEncontrado, RegraNegocio, SessaoInvalida)
└── config       # Segurança, OpenAPI/Swagger e carga inicial (lojas + cardápios)
src/main/resources/templates   # Páginas HTML (Thymeleaf)
Dockerfile · render.yaml       # Deploy (Docker + Render)
docs/                          # Artigo, guia de deploy e roteiro de apresentação
```

## Documentação

- [`docs/PRE-PROJETO.pdf`](docs/PRE-PROJETO.pdf) — pré-projeto (fase de descoberta) que originou o trabalho.
- [`docs/ARTIGO.pdf`](docs/ARTIGO.pdf) — artigo (relatório final) da disciplina.
- [`docs/DEPLOY.md`](docs/DEPLOY.md) — guia de deploy no Render.
- [`docs/APRESENTACAO.md`](docs/APRESENTACAO.md) — roteiro da apresentação.
- [`docs/Avaliacao-A3-Sistemas-Distribuidos-e-Mobile.pdf`](docs/Avaliacao-A3-Sistemas-Distribuidos-e-Mobile.pdf) — enunciado oficial da avaliação.

## Integrantes

- *(preencher com os nomes do grupo)*
