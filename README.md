# PratoFácil — API REST para Gestão de Pedidos Locais

Projeto da disciplina **Sistemas Distribuídos e Mobile** (Avaliação A3). É uma
aplicação em **Java + Spring Boot** que ajuda pequenos empreendedores do ramo
alimentício a gerenciar o ciclo de vida dos pedidos (cardápio, pedidos e status
de entrega), aplicando na prática conceitos de sistemas distribuídos: arquitetura
cliente-servidor, API REST, protocolo HTTP, middleware e persistência de dados.

## Problemática

Pequenos empreendedores que produzem e entregam alimentos costumam controlar as
vendas de forma manual (caderno, WhatsApp), o que gera perda de informação e erros
nos pedidos. O PratoFácil centraliza esse controle em um sistema simples e
desacoplado.

## Tecnologias

- Java 21
- Spring Boot (Spring Web / MVC, Spring Data JPA, Thymeleaf)
- Banco de dados H2 (em memória) — ver seção *Banco de dados*
- Maven (wrapper `mvnw` incluído)

## Arquitetura

A aplicação segue o padrão **MVC** e expõe duas formas de acesso ao mesmo núcleo
de negócio (controllers → repositories → banco):

- **API REST** (`/api/...`): troca dados em **JSON**, podendo ser consumida por
  qualquer cliente HTTP (ex.: Postman). É a interface que demonstra os conceitos
  de sistemas distribuídos.
- **Interface web** (Thymeleaf): páginas HTML simples para uso direto no navegador.

```
Cliente (Postman / navegador)  ->  HTTP  ->  API REST Spring Boot  ->  Banco de dados
```

## Como executar

Pré-requisito: **Java 21** instalado.

```bash
# na raiz do projeto
./mvnw spring-boot:run
```

A aplicação sobe em `http://localhost:8080`.

- Interface web (admin): `http://localhost:8080/admin`
- Cardápio (cliente): `http://localhost:8080/pratos`
- Console do banco H2: `http://localhost:8080/h2-console`
  (JDBC URL `jdbc:h2:mem:cardapiofacildb`, usuário `sa`, senha em branco)

## Endpoints da API REST

Base: `http://localhost:8080/api`

### Cardápio (`/api/pratos`)

| Método | Rota | Descrição | Respostas |
|--------|------|-----------|-----------|
| GET    | `/api/pratos`      | Lista o cardápio (RF01)        | 200 |
| GET    | `/api/pratos/{id}` | Busca um prato                 | 200 / 404 |
| POST   | `/api/pratos`      | Cadastra um prato              | 201 / 400 |
| PUT    | `/api/pratos/{id}` | Atualiza um prato              | 200 / 400 / 404 |
| DELETE | `/api/pratos/{id}` | Remove um prato                | 204 / 404 / 409 |

### Pedidos (`/api/pedidos`)

| Método | Rota | Descrição | Respostas |
|--------|------|-----------|-----------|
| GET    | `/api/pedidos`             | Lista os pedidos                  | 200 |
| GET    | `/api/pedidos/{id}`        | Busca um pedido                   | 200 / 404 |
| POST   | `/api/pedidos`             | Cria um pedido (RF02)             | 201 / 400 |
| PUT    | `/api/pedidos/{id}/status` | Atualiza o status do pedido (RF03)| 200 / 404 |

O valor total do pedido é calculado no servidor a partir dos pratos enviados.
Status possíveis: `EM_PREPARO`, `SAIU_PARA_ENTREGA`, `ENTREGUE`.

### Exemplos

```bash
# Cadastrar um prato
curl -X POST http://localhost:8080/api/pratos \
  -H "Content-Type: application/json" \
  -d '{"nome":"Feijoada","descricao":"Completa","preco":39.90}'

# Criar um pedido com os pratos de id 1 e 2
curl -X POST http://localhost:8080/api/pedidos \
  -H "Content-Type: application/json" \
  -d '{"nome":"Maria","cardapios":[{"id":1},{"id":2}]}'

# Atualizar o status do pedido 1
curl -X PUT "http://localhost:8080/api/pedidos/1/status?status=SAIU_PARA_ENTREGA"
```

## Tratamento de erros

As respostas de erro da API vêm em JSON padronizado (sem stacktrace):

```json
{ "status": 404, "error": "Not Found", "message": "Prato não encontrado: 999" }
```

- **400 Bad Request** — dados inválidos (ex.: prato sem nome ou preço).
- **404 Not Found** — recurso inexistente.
- **409 Conflict** — tentativa de remover um prato que ainda está vinculado a um pedido.

## Banco de dados

Atualmente o projeto usa **H2 em memória**, ou seja, os dados são reiniciados a cada
execução. Como evolução (em andamento pela equipe), está prevista a migração para um
**banco relacional hospedado em nuvem** (ex.: PostgreSQL gerenciado), reforçando os
conceitos de banco distribuído e transparência de localização.

## Estrutura do projeto

```
src/main/java/com/example/vendeFacil
├── controller   # Controllers web (Thymeleaf) e REST (/api), + tratamento de erros
├── model        # Entidades JPA (Cardapio, Pedido) e enum Status
└── repository   # Repositórios Spring Data JPA
src/main/resources/templates   # Páginas HTML (Thymeleaf)
```

## Integrantes

- *(preencher com os nomes do grupo)*
