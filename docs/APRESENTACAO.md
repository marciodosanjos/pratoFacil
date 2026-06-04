# PratoFácil — Apresentação (A3 Sistemas Distribuídos e Mobile)

Material de apoio para a apresentação final, nas duas modalidades possíveis:
**ao vivo (até 10 min)** ou **vídeo/pitch (até 5 min)**.

---

## Parte 1 — Roteiro de slides (apresentação ao vivo, ~10 min)

> Sugestão: 1 a 1,5 min por slide. Dividam as falas entre os integrantes.

**Slide 1 — Capa**
- Título: *PratoFácil — API REST para Gestão de Pedidos Locais*
- Disciplina, professores, integrantes.

**Slide 2 — O problema**
- Pequenos empreendedores de comida controlam vendas no caderno/WhatsApp.
- Consequências: perda de informação, erros nos pedidos, difícil acompanhar e escalar.

**Slide 3 — A solução**
- PratoFácil centraliza o ciclo de vida do pedido.
- Foco em 3 requisitos: RF01 listar cardápio · RF02 cliente faz pedido · RF03 empreendedor atualiza status.

**Slide 4 — Arquitetura**
- Diagrama: `Cliente (navegador/Postman/app) → HTTP → API Spring Boot → Serviço → JPA → Banco`.
- Padrão MVC + camada de serviço; duas interfaces (REST JSON e Web Thymeleaf) sobre o mesmo núcleo.

**Slide 5 — Conceitos de Sistemas Distribuídos aplicados**
- **Transparência** (cliente não sabe onde está o servidor/banco).
- **Middleware** (Spring Boot intermedeia requisições e dados).
- **Protocolo HTTP / REST** (GET, POST, PUT, DELETE + códigos de status).
- **Banco em nuvem** (PostgreSQL no Render).

**Slide 6 — Tecnologias**
- Java 21 · Spring Boot 4 (Web MVC, Data JPA, Security, Thymeleaf) · H2/PostgreSQL · springdoc/Swagger · Maven · JUnit/MockMvc · Git/GitHub.

**Slide 7 — Segurança e papéis**
- Dois papéis: ADMIN (empreendedor) e CLIENTE.
- Login por formulário + HTTP Basic (API), senhas com BCrypt, cada cliente vê só os próprios pedidos.

**Slide 8 — Demonstração (ao vivo)**
- Ver roteiro da Parte 3.

**Slide 9 — Testes**
- 7 testes automatizados (MockMvc) verdes + testes manuais (Postman/cURL).
- Códigos HTTP validados: 200, 201, 204, 400, 401, 404, 409.

**Slide 10 — Conclusão e evolução futura**
- Conceitos de SD aplicados de ponta a ponta; banco já hospedado em nuvem.
- Futuro: deploy da aplicação em nuvem, pagamento online, notificações, app mobile.

---

## Parte 2 — Roteiro de pitch (vídeo gravado, ~5 min)

> Texto-base para narração. Ajustem o tom e dividam as falas.

"Olá! Somos o grupo *(nomes)* e apresentamos o **PratoFácil**, projeto da disciplina
de Sistemas Distribuídos e Mobile.

**O problema:** pequenos empreendedores que vendem comida controlam tudo no caderno
ou no WhatsApp. Isso causa perda de pedidos, erros e dificulta o crescimento.

**Nossa solução** é o PratoFácil, uma **API REST** em Java com Spring Boot, com uma
interface web. Ela centraliza o ciclo de vida do pedido: o cliente vê o cardápio e
faz o pedido; o empreendedor acompanha e atualiza o status da entrega.

**Por que isso é um sistema distribuído?** Porque separamos o cliente, o servidor de
aplicação e o banco de dados, que se comunicam por HTTP. O cliente faz uma requisição
e não precisa saber onde está o servidor nem o banco — é a **transparência**. O Spring
Boot funciona como **middleware**, recebendo as requisições, aplicando as regras de
negócio e acessando os dados. Usamos o padrão **REST** com os verbos HTTP e respostas
em JSON, com códigos de status adequados e erros tratados.

Para reforçar o conceito de **nuvem**, a aplicação tem dois perfis: em
desenvolvimento usa um banco local, e em produção conecta-se a um **PostgreSQL
hospedado no Render** — sem mudar uma linha de código, só o perfil.

Também aplicamos **segurança**: há dois papéis, o empreendedor e o cliente, com login
e senha protegida, e cada cliente só acessa os próprios pedidos.

*(mostrar a demonstração — ver roteiro)*

Validamos tudo com testes automatizados e manuais. Como evolução, queremos publicar a
aplicação em nuvem, integrar pagamento e criar um app mobile consumindo a mesma API.
Obrigado!"

---

## Parte 3 — Roteiro de demonstração (o que mostrar na tela)

1. **Vitrine de lojas:** abrir `/lojas` — mostra as várias lojas do marketplace; abrir uma loja exibe o cardápio dela (RF01).
2. **Cadastro/login de cliente:** criar conta em `/cadastro`, logar (cai na vitrine de lojas).
3. **Fazer um pedido (RF02):** escolher uma loja, navegar pelas **categorias**, ajustar a **quantidade (+/−)** dos itens, conferir o **carrinho** (total ao vivo) e finalizar.
4. **Acompanhamento:** mostrar a página de pedido com a **linha do tempo** de status.
5. **Empreendedor (ADMIN):** logar como um lojista, ex.: `maozinhaburger@pratofacil.com` / `burger123`
   (cada loja tem login próprio — ver lista no README). Mostrar o **isolamento**: o lojista só vê o cardápio e os pedidos da sua loja.
6. **Gerir status (RF03):** em `/admin/pedidos`, mudar o status do pedido e mostrar
   que o cliente vê a atualização.
7. **Dashboard:** abrir `/admin/dashboard` (faturamento, pedidos, clientes, status).
8. **API + Swagger:** abrir `/swagger-ui.html`; no Postman, mostrar `GET /api/pratos`
   (público), um `POST` sem login retornando **401**, e com login do admin retornando **201**.
9. **Nuvem:** explicar/mostrar que, no perfil `prod`, os dados são gravados no
   **PostgreSQL do Render** (transparência de localização).

> Dica: deixem o app já rodando e com 1–2 pratos cadastrados antes de gravar/apresentar.
