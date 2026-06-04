# PratoFácil — API REST para Gestão de Pedidos Locais

Projeto da disciplina **Sistemas Distribuídos e Mobile** (Avaliação A3). É uma
aplicação **Java + Spring Boot 4** que ajuda pequenos empreendedores do ramo
alimentício a gerenciar o ciclo de vida dos pedidos (cardápio, pedidos e status de
entrega), aplicando na prática conceitos de sistemas distribuídos: arquitetura
cliente-servidor, API REST, protocolo HTTP, middleware, transparência, persistência
de dados e serviços em nuvem.

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
- **Banco de dados:** H2 em memória (perfil `dev`) e **PostgreSQL em nuvem / Render** (perfil `prod`)
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
```

## Perfis e papéis

- **Perfis:** `dev` (H2 em memória, padrão) e `prod` (PostgreSQL em nuvem via variáveis de ambiente).
- **Papéis:** `ADMIN` (empreendedor) e `CLIENTE` (cliente final, com conta). Senhas com hash BCrypt.
- **Admin padrão** criado na primeira execução: **`admin@pratofacil.com`** / **`admin123`**.

## Como executar

Pré-requisito: **Java 21**.

```bash
# perfil dev (H2), na raiz do projeto
./mvnw spring-boot:run
```

A aplicação sobe em `http://localhost:8080`.

| Recurso | URL |
|---|---|
| Cardápio (cliente) | `http://localhost:8080/pratos` |
| Cadastro / Login | `http://localhost:8080/cadastro` · `/login` |
| Painel do empreendedor | `http://localhost:8080/admin` |
| Dashboard | `http://localhost:8080/admin/dashboard` |
| Swagger (documentação da API) | `http://localhost:8080/swagger-ui.html` |
| Console H2 (perfil dev) | `http://localhost:8080/h2-console` |

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

O valor total do pedido é **calculado no servidor** a partir dos pratos enviados.
Status possíveis: `EM_PREPARO`, `SAIU_PARA_ENTREGA`, `ENTREGUE`.

### Exemplos (cURL)

```bash
# Cadastrar um prato (autenticado como ADMIN via HTTP Basic)
curl -u admin@pratofacil.com:admin123 -X POST http://localhost:8080/api/pratos \
  -H "Content-Type: application/json" \
  -d '{"nome":"Feijoada","descricao":"Completa","preco":39.90}'

# Listar o cardápio (público)
curl http://localhost:8080/api/pratos

# Atualizar o status do pedido 1 (ADMIN)
curl -u admin@pratofacil.com:admin123 \
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
papéis (ADMIN), erros (400/404) e criação de pedido com valor total calculado.

## Estrutura do projeto

```
src/main/java/com/example/vendeFacil
├── controller   # Controllers REST (/api) e Web (Thymeleaf), Auth, Dashboard e erros
├── service      # Regras de negócio (Cardapio, Pedido, Usuario)
├── repository   # Repositórios Spring Data JPA
├── model        # Entidades JPA (Cardapio, Pedido, Usuario) e enums (Status, Role)
├── exception    # Exceções de domínio (RecursoNaoEncontrado, RegraNegocio)
└── config       # Segurança, OpenAPI/Swagger e carga inicial (admin)
src/main/resources/templates   # Páginas HTML (Thymeleaf)
docs/ARTIGO.md                 # Artigo (relatório final) da disciplina
```

## Documentação

O relatório/artigo da disciplina está em [`docs/ARTIGO.md`](docs/ARTIGO.md).

## Integrantes

- *(preencher com os nomes do grupo)*
