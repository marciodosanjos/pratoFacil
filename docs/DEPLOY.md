# Deploy do PratoFácil (Render)

> **Por que Render e não Vercel?** A Vercel hospeda front-end/serverless (Node, etc.)
> e **não roda aplicações Java/Spring Boot**. O PratoFácil precisa de um servidor com
> JVM rodando continuamente — o **Render** atende isso e, como o **PostgreSQL do
> projeto já está no Render**, app e banco ficam na mesma infraestrutura.

O projeto já está pronto para deploy: tem um **`Dockerfile`** (Render constrói a
imagem) e escuta na porta da variável `PORT` (que o Render injeta).

## Passo a passo

### 1. Pegue os dados do banco (PostgreSQL no Render)
No painel do Render, abra o seu banco PostgreSQL e copie a **Internal Database URL**
(formato `postgresql://USUARIO:SENHA@HOST_INTERNO/BANCO`). Use a *internal* (mais
rápida, sem SSL) porque o app vai rodar no mesmo Render.

### 2. Crie o Web Service
1. Render → **New +** → **Web Service**.
2. Conecte o repositório do GitHub e escolha a branch (`main` após o merge do PR, ou
   `feat/refatoracao-backend`).
3. O Render detecta o **`Dockerfile`** automaticamente (Runtime: *Docker*).
4. **Instance Type:** Free serve para testes do grupo.

### 3. Configure as variáveis de ambiente
Em **Environment**, adicione:

| Chave | Valor |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://HOST_INTERNO/BANCO` |
| `SPRING_DATASOURCE_USERNAME` | usuário do banco |
| `SPRING_DATASOURCE_PASSWORD` | senha do banco |

> Converta a *Internal Database URL* para JDBC: troque `postgresql://user:senha@host/banco`
> por `jdbc:postgresql://host/banco` e ponha usuário/senha nos campos separados.

### 4. Deploy
Clique em **Create Web Service**. O Render constrói a imagem e sobe a aplicação.
Ao final, você recebe um link público (ex.: `https://pratofacil.onrender.com`) para
compartilhar com o grupo.

## Observações
- **Primeiro acesso:** na primeira execução, o sistema cria automaticamente as 4 lojas
  de exemplo (com cardápios e logos). Os logins dos lojistas estão no `README.md`.
- **Plano Free:** o serviço "hiberna" após ~15 min sem uso; o primeiro acesso depois
  disso demora ~30s para acordar (cold start). É normal no plano gratuito.
- **Build local da imagem (opcional):** `docker build -t pratofacil .` e
  `docker run -p 8080:8080 -e SPRING_PROFILES_ACTIVE=prod -e SPRING_DATASOURCE_URL=... pratofacil`.
