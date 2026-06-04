# Deploy do PratoFácil (Render)

> **Por que Render e não Vercel?** A Vercel hospeda front-end/serverless (Node, etc.)
> e **não roda aplicações Java/Spring Boot**. O PratoFácil precisa de um servidor com
> JVM rodando continuamente — o **Render** atende isso e, como o **PostgreSQL do
> projeto já está no Render**, app e banco ficam na mesma infraestrutura.

O projeto já está pronto para deploy: tem um **`Dockerfile`** (Render constrói a
imagem) e escuta na porta da variável `PORT` (que o Render injeta).

## Passo a passo

### 1. Pegue os dados do banco (PostgreSQL no Render)
No painel do Render, abra o seu banco PostgreSQL → aba **Info** (ou **Connections**) e
anote os campos REAIS (têm cara de `dpg-...`):

- **Hostname (Internal):** ex. `dpg-abc123def456-a`
- **Port:** `5432`
- **Database:** ex. `pratofacil_db`
- **Username** e **Password**

> Use o host **interno** (`dpg-...-a`, sem domínio) porque o app roda no mesmo Render
> (mais rápido, sem SSL). Se app e banco estiverem em **regiões diferentes**, use o host
> **externo** (`dpg-...-a.oregon-postgres.render.com`) e acrescente `?sslmode=require`.

### 2. Crie o Web Service
1. Render → **New +** → **Web Service**.
2. Conecte o repositório do GitHub e escolha a branch (`main` após o merge do PR, ou
   `feat/refatoracao-backend`).
3. O Render detecta o **`Dockerfile`** automaticamente (Runtime: *Docker*).
4. **Instance Type:** Free serve para testes do grupo.

### 3. Configure as variáveis de ambiente
Em **Environment**, adicione. ⚠️ **Troque os valores de exemplo pelos SEUS dados do
passo 1** — NÃO deixe `HOST_INTERNO`/`BANCO` literalmente (isso causa o erro
`UnknownHostException`):

| Chave | Valor (exemplo — use os SEUS dados reais) |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://dpg-abc123def456-a/pratofacil_db` |
| `SPRING_DATASOURCE_USERNAME` | `pratofacil_user` |
| `SPRING_DATASOURCE_PASSWORD` | (a senha do banco) |
| `ASAAS_API_KEY` | (sua chave sandbox — opcional, para o pagamento real: PIX e cartão) |

> A *Internal Database URL* do Render vem como `postgresql://user:senha@dpg-xxxx-a/banco`.
> Converta para JDBC: vire `jdbc:postgresql://dpg-xxxx-a/banco` e ponha usuário/senha nos
> campos separados (o driver JDBC não aceita `user:senha@` dentro da URL).

### 4. Deploy
Clique em **Create Web Service**. O Render constrói a imagem e sobe a aplicação.
Ao final, você recebe um link público (ex.: `https://pratofacil.onrender.com`) para
compartilhar com o grupo.

## Observações
- **Primeiro acesso:** na primeira execução, o sistema cria automaticamente as 4 lojas
  de exemplo (com cardápios e logos). Os logins dos lojistas estão no `README.md`.
- **Plano Free:** o serviço "hiberna" após ~15 min sem uso; o primeiro acesso depois
  disso demora ~30s para acordar (cold start). É normal no plano gratuito.
- **Erro `UnknownHostException` / `The connection attempt failed` no log:** a
  `SPRING_DATASOURCE_URL` está com um host placeholder (ex.: `HOST_INTERNO`) em vez do
  host real do Postgres. Corrija a variável com o `dpg-...` real (passo 1) e faça um
  **Manual Deploy**. O aviso *"No open ports detected"* é só consequência: o app crasha
  na conexão com o banco antes de abrir a porta; resolvido o banco, a porta sobe.
- **Build local da imagem (opcional):** `docker build -t pratofacil .` e
  `docker run -p 8080:8080 -e SPRING_PROFILES_ACTIVE=prod -e SPRING_DATASOURCE_URL=... pratofacil`.
