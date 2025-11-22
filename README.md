# Como Rodar o Projeto com Docker

## Visão Geral da Arquitetura

Este projeto utiliza uma arquitetura baseada em microsserviços, composta por:

- **Backend (Java Spring Boot):** Responsável pela lógica de negócio e API REST.
- **Banco de Dados (PostgreSQL):** Armazena os dados da aplicação.
- **Docker Compose:** Orquestra os containers do backend e do banco de dados, facilitando o desenvolvimento e a execução local.

## Pré-requisitos

- ![Docker](https://www.docker.com/sites/default/files/d8/2019-07/Moby-logo.png) [Docker](https://www.docker.com/)
- ![Docker Compose](https://seeklogo.com/images/D/docker-compose-logo-6B6C1D8C18-seeklogo.com.png) [Docker Compose](https://docs.docker.com/compose/)

## Como Executar o Projeto

1. **Clone o repositório:**
   ```sh
   git clone <URL_DO_REPOSITORIO>
   cd <nome_da_pasta>
   ```

2. **Construa e suba os containers:**
   ```sh
   docker compose up --build
   ```

   Isso irá:
   - Construir a imagem do backend (Java Spring Boot) usando o Maven.
   - Baixar a imagem do PostgreSQL.
   - Subir ambos os containers e garantir que o backend consiga se conectar ao banco de dados.

3. **Acessando a aplicação:**
   - O backend estará disponível em: `http://localhost:8080`
   - O banco de dados estará acessível na porta padrão `5432` (caso precise conectar via cliente externo).

## Estrutura dos Arquivos Importantes

- `docker-compose.yml`: Define os serviços (backend e banco de dados), redes e volumes.
- `Dockerfile`: Responsável por construir a imagem do backend.
- `src/`: Código-fonte do backend (Java Spring Boot).
- `docker-entrypoint.sh`: Script de inicialização customizado (se aplicável).

## Variáveis de Ambiente

Você pode configurar variáveis de ambiente no `docker-compose.yml` para customizar usuário, senha e nome do banco de dados PostgreSQL.

Exemplo:
```yaml
environment:
  POSTGRES_USER: usuario
  POSTGRES_PASSWORD: senha
  POSTGRES_DB: nome_do_banco
```

## Parando os Containers

Para parar e remover os containers, execute:
```sh
docker compose down
```

## Observações

- Certifique-se de que as portas `8080` (backend) e `5432` (PostgreSQL) estejam livres.
- O backend irá aguardar o banco de dados estar pronto antes de iniciar.
- Logs dos serviços podem ser acompanhados diretamente pelo terminal.

---

## PostController (Endpoints de Postagem)

O backend expõe endpoints para criar e deletar posts. O controller está mapeado em `/post`.

Resumo:

- Criar post: POST /post
- Deletar post: DELETE /post

Observações gerais:

- Os endpoints exigem autenticação (JWT). Envie o header `Authorization: Bearer <token>` em todas as requisições.
- Upload de imagem deve ser multipart/form-data e o campo do arquivo é `image`.
- A descrição do post deve ser enviada como `description` (parte do multipart request).
- Tipos de imagem aceitos: `image/jpeg`, `image/png`, `image/webp`.

### 1) Criar Post

- Endpoint: `POST /post`
- Autenticação: obrigatória
- Content-Type: `multipart/form-data`
- Partes esperadas:
    - `image` (arquivo) — obrigatória
    - `description` (string) — obrigatória, não vazia

Comportamento:

- O servidor faz upload da imagem para o armazenamento (BlobService) e salva um registro `Post` no banco com `blobName`,
  `description`, `profile` (usuário autenticado) e `createdAt`.
- Em caso de sucesso retorna HTTP 201 Created (corpo vazio).

Erros comuns:

- 400 Bad Request — falta `image` ou `description`, ou validação falhou.
- 401 Unauthorized — requisição sem token válido.
- 500 Internal Server Error — falha no upload da imagem (BlobService) ou erro interno.

Exemplo com curl (upload multipart):

```bash
curl -i -X POST http://localhost:8080/post \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -F "image=@/caminho/para/minha-foto.jpg" \
  -F "description=A bela paisagem"
```

Resposta de sucesso (exemplo):
HTTP/1.1 201 Created
Location: /post

> Observação: atualmente o endpoint retorna 201 com corpo vazio; consulte o código se quiser retornar o ID criado.

### 2) Deletar Post

- Endpoint: `DELETE /post`
- Autenticação: obrigatória
- Parâmetros: `postId` (UUID) como query parameter

Comportamento:

- O endpoint busca o post pelo `postId`. Se não existir, retorna 404 Not Found.
- Se o usuário autenticado não for o dono do post, retorna 401 Unauthorized.
- Se o dono for o usuário, o serviço remove o blob no armazenamento e realiza um soft-delete no banco (por ex. setando
  `deletedAt`). Retorna 204 No Content.

Exemplo com curl:

```bash
curl -i -X DELETE "http://localhost:8080/post?postId=6f7a3b2a-...-abcd" \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

Respostas possíveis:

- 204 No Content — sucesso
- 401 Unauthorized — usuário não é o dono do post
- 404 Not Found — post não encontrado

### 3) Recuperar Detalhes do Post

- Endpoint: `GET /post/{postId}`
- Autenticação: obrigatória
- Parâmetros: `postId` (UUID) na URL

Comportamento:

- Retorna os detalhes completos do post, incluindo URL da imagem, descrição, contagem de likes, autor e lista de quem curtiu.
- Se o post não existir ou estiver deletado, retorna 404 Not Found.

Exemplo de Resposta:

```json
{
  "id": "uuid-do-post",
  "imageUrl": "/images/blob-name.png",
  "description": "Descrição do post",
  "likesCount": 10,
  "createdAt": "2023-10-27T10:00:00Z",
  "author": {
    "id": "uuid-do-autor",
    "name": "Nome do Autor",
    "username": "username_autor",
    "profilePictureUrl": "http://url-da-foto"
  },
  "likedBy": [
    {
      "id": "uuid-usuario-que-curtiu",
      "name": "Nome Usuario",
      "username": "username_usuario",
      "profilePictureUrl": "http://url-da-foto"
    }
  ]
}
```

### 4) Curtir Post

- Endpoint: `POST /post/{postId}/like`
- Autenticação: obrigatória
- Parâmetros: `postId` (UUID) na URL

Comportamento:
- O usuário curte o post identificado por `postId`.
- Se o post não existir, retorna 404 Not Found.
- Se o usuário já curtiu, retorna 409 Conflict.
- Se o post for curtido com sucesso, retorna 200 OK (corpo vazio).

Exemplo com curl:
```bash
curl -i -X POST http://localhost:8080/post/6f7a3b2a-...-abcd/like \
  -H "Authorization: Bearer <JWT_TOKEN>"
```
Respostas possíveis:
- 200 OK — sucesso
- 404 Not Found — post não encontrado
- 409 Conflict — usuário já curtiu o post

### 5) Descurtir Post

- Endpoint: `DELETE /post/{postId}/like`
- Autenticação: obrigatória
- Parâmetros: `postId` (UUID) na URL

Comportamento:
- O usuário remove o like do post identificado por `postId`.
- Se o post não existir, retorna 404 Not Found.
- Se o like não existir, retorna 404 Not Found.
- Se o deslike for realizado com sucesso, retorna 200 OK (corpo vazio).

Exemplo com curl:
```bash
curl -i -X DELETE http://localhost:8080/post/6f7a3b2a-...-abcd/like \
  -H "Authorization: Bearer <JWT_TOKEN>"
```
Respostas possíveis:
- 200 OK — sucesso
- 404 Not Found — post ou like não encontrado

---

## Observações sobre Likes
- O backend previne likes duplicados: se o usuário já curtiu, retorna erro 409 Conflict.
- O contador de likes do post é atualizado automaticamente ao curtir/descurtir.
- Para consultar quem curtiu, utilize o endpoint de detalhes do post (`GET /post/{postId}`), que retorna a lista de usuários que curtiram.

---

## Segurança / JWT

A aplicação espera um segredo JWT na configuração (`api.security.token.secret`) — em produção isso deve vir de variáveis
de ambiente (não comitar secrets no repositório).

Sugestão para desenvolvimento: use um `.env` (não comitado) com as variáveis necessárias e carregue-as no ambiente antes
de executar o app.

Exemplo `.env` (adicionar a `.env` em `.gitignore`):

```ini
JWT_SECRET=algum-segredo-muito-secreto
AZURE_BLOB_CONNECTION_STRING=DefaultEndpointsProtocol=https;AccountName=...;AccountKey=...;EndpointSuffix=core.windows.net
AZURE_BLOB_CONTAINER=musa-container
```

Carregue as variáveis no shell (Linux/macOS):

```bash
set -a
. ./.env
set +a
mvn spring-boot:run
```

---

Se quiser que eu adicione exemplos de request/response em Java (RestTemplate/WebClient) ou em JS (fetch/axios), diga
qual você prefere e eu adiciono.
