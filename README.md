# Inti API & Operations Guide

> Aplicação Spring Boot responsável pelo backend da rede social **Inti**. Este documento reúne as instruções para executar o projeto e a documentação das rotas expostas (método, payloads esperados, respostas e observações de autenticação).

---

## 📦 Como rodar o projeto com Docker

### Arquitetura em alto nível

- **Backend**: Java 17 + Spring Boot (REST API, autenticação, feed, posts, eventos etc.).
- **PostgreSQL**: banco relacional para perfis, posts e eventos.
- **Docker Compose**: orquestra os containers e garante rede interna compartilhada.

### Pré-requisitos

- [Docker](https://www.docker.com/)
- [Docker Compose](https://docs.docker.com/compose/)

### Passo a passo

```bash
git clone <URL_DO_REPOSITORIO>
cd 2025.2-Inti
docker compose up --build
```

- API disponível em `http://localhost:8080`
- PostgreSQL disponível em `localhost:5432`

Para desligar os containers:

```bash
docker compose down
```

### Arquivos importantes

| Arquivo                | Função                                                |
| ---------------------- | ----------------------------------------------------- |
| `docker-compose.yml`   | Define serviços, volumes e variáveis de ambiente.     |
| `Dockerfile`           | Build da imagem do backend (mvn clean package + JAR). |
| `docker-entrypoint.sh` | Script de inicialização customizado.                  |
| `src/`                 | Código-fonte (controllers, services, DTOs etc.).      |

### Variáveis de ambiente úteis

```ini
JWT_SECRET=algum-segredo-muito-secreto
AZURE_BLOB_CONNECTION_STRING=DefaultEndpointsProtocol=...;AccountKey=...
AZURE_BLOB_CONTAINER=musa-container
```

Carregue-as antes de rodar localmente para que o Spring reconheça:

```bash
set -a
. ./.env
set +a
mvn spring-boot:run
```

---

## 📘 Visão geral da API

| Item                 | Valor                                               |
| -------------------- | --------------------------------------------------- |
| **Base URL (local)** | `http://localhost:8080`                             |
| **Formatos aceitos** | JSON (default) e `multipart/form-data` para uploads |
| **Autenticação**     | JWT (`Authorization: Bearer <token>`)               |
| **Versionamento**    | Não há prefixo de versão; utilize a raiz `/`        |

### Convenções

- Se o endpoint exige autenticação e o header não for enviado ou for inválido, retorna **401 Unauthorized**.
- Recursos inexistentes retornam **404 Not Found**.
- Toda data/hora é enviada em ISO-8601 (`2025-11-25T14:30:00Z`).
- Uploads de imagem aceitam `image/jpeg`, `image/png`, `image/gif` ou `image/webp`.

### Códigos de status recorrentes

| Código                    | Significado                                                           |
| ------------------------- | --------------------------------------------------------------------- |
| 200 OK                    | Operação concluída com sucesso.                                       |
| 201 Created               | Recurso criado (normalmente sem corpo).                               |
| 204 No Content            | Operação bem-sucedida sem payload.                                    |
| 400 Bad Request           | Payload inválido ou campos faltando.                                  |
| 401 Unauthorized          | Token ausente ou inválido.                                            |
| 403 Forbidden             | Perfil autenticado sem permissão (ex.: eventos só para organizações). |
| 404 Not Found             | Recurso inexistente.                                                  |
| 409 Conflict              | Violação de regra de negócio (ex.: like duplicado).                   |
| 500 Internal Server Error | Erro inesperado no servidor.                                          |

### Sumário rápido de endpoints

| Domínio      | Método | Caminho                        | Resumo                                         |
| ------------ | ------ | ------------------------------ | ---------------------------------------------- |
| Autenticação | POST   | `/auth/register`               | Cria usuário e retorna JWT + dados do perfil.  |
| Autenticação | POST   | `/auth/login`                  | Valida credenciais e retorna JWT.              |
| Autenticação | GET    | `/auth`                        | Endpoint simples para testes (retorna string). |
| Perfil       | GET    | `/profile/me`                  | Perfil do usuário autenticado (paginado).      |
| Perfil       | GET    | `/profile/{username}`          | Perfil público com posts paginados.            |
| Perfil       | POST   | `/profile/upload-me`           | Atualiza foto de perfil (multipart).           |
| Perfil       | PATCH  | `/profile/update`              | Atualiza dados cadastrais (multipart).         |
| Perfil       | POST   | `/profile/{username}/follow`   | Segue usuário.                                 |
| Perfil       | DELETE | `/profile/{username}/unfollow` | Deixa de seguir usuário.                       |
| Post         | POST   | `/post`                        | Cria post com imagem.                          |
| Post         | DELETE | `/post/{postId}`               | Remove post (owner).                           |
| Post         | GET    | `/post/{postId}`               | Detalhes completos do post.                    |
| Post         | POST   | `/post/{postId}/like`          | Curte post.                                    |
| Post         | DELETE | `/post/{postId}/like`          | Remove like.                                   |
| Feed         | GET    | `/feed`                        | Feed personalizado paginado.                   |
| Feed         | GET    | `/feed/organization`           | Mensagem de boas-vindas para organizações.     |
| Imagens      | GET    | `/images/{blobName}`           | Baixa imagem direto do Blob Storage.           |
| Eventos      | POST   | `/event`                       | Cria evento (apenas organizações).             |
| Eventos      | GET    | `/event/lists`                 | Lista eventos publicados.                      |

---

## 🔐 Autenticação (`/auth`)

### POST `/auth/register`

- **Corpo (JSON)**

```json
{
  "name": "Lucas Moretti",
  "username": "morettipdr",
  "email": "lucas@example.com",
  "password": "senhaSuperSecreta",
  "type": "user" // ou "organization"
}
```

- **Resposta 201** (`ProfileCreationResponse`)

```json
{
  "id": "394a77ba-9e56-47e7-a3d4-715dba81eaf9",
  "username": "morettipdr",
  "name": "Lucas Moretti",
  "email": "lucas@example.com",
  "jwt": "<TOKEN_JWT>",
  "type": "user",
  "createdAt": "2025-11-25T17:08:15.123Z"
}
```

### POST `/auth/login`

- **Corpo (JSON)**

```json
{
  "email": "lucas@example.com",
  "password": "senhaSuperSecreta"
}
```

- **Resposta 200**: string contendo o JWT.

### GET `/auth`

- Sem corpo; útil apenas para testar se o controller responde (retorna string "userid: ").

---

## 👤 Perfis & Social (`/profile`)

Todos os endpoints abaixo **exigem JWT**.

### GET `/profile/me`

- **Query params obrigatórios**: `page`, `size` (inteiros).
- **Resposta 200** (`ProfileResponse`):

```json
{
  "name": "Lucas Moretti",
  "username": "morettipdr",
  "publicEmail": "lucas_public@example.com",
  "phone": "+55 61 99999-0000",
  "profile_picture_url": "/images/avatar.png",
  "bio": "Engenheiro da UnB",
  "followersCount": 150,
  "followingCount": 88,
  "posts": [
    {
      "id": "3d68bfe8-9613-4e1d-b8ef-d69e662ebdea",
      "imgLink": "/images/blob.png",
      "description": "Post 2 de Maria",
      "likesCount": 12,
      "createdAt": "2025-11-21T17:52:44.788Z"
    }
  ]
}
```

### GET `/profile/{username}`

- Mesmo payload acima, porém para o usuário solicitado.

### POST `/profile/upload-me`

- **Content-Type**: `multipart/form-data`
- **Campo obrigatório**: `myImage` (arquivo).
- **Resposta**: `201 Created` sem corpo.

### PATCH `/profile/update`

- **Content-Type**: `multipart/form-data`
- **Campos aceitos** (`UpdateUserRequest`): `name`, `username`, `phone`, `publicemail`, `userBio`, `profilePicture`.
- **Resposta**: `201 Created` sem corpo.

### POST `/profile/{username}/follow`

- Segue o usuário indicado.
- **Resposta 200** (`FollowResponse`):

```json
{ "message": "Perfil seguido com sucesso." }
```

### DELETE `/profile/{username}/unfollow`

- Cancela o follow.
- Resposta igual ao follow (mensagem).

### GET `/profile/string/teste/organization`

- Protegido com `@PreAuthorize("hasRole('ORGANIZATION')")`.
- Retorna apenas `"teste"` (endpoint de diagnóstico).

---

## 🖼️ Imagens (`/images`)

### GET `/images/{blobName}`

- Não exige autenticação.
- Detecta o `Content-Type` pelo sufixo do arquivo (`.png`, `.gif`, `.webp`, `.jpg`).
- **Resposta 200**: bytes da imagem.
- **Resposta 404**: quando o blob não existe.

---

## 📝 Posts & Likes (`/post`)

Todos os endpoints exigem JWT.

### POST `/post`

- **Content-Type**: `multipart/form-data`.
- **Campos obrigatórios**:
  - `image`: arquivo.
  - `description`: texto.
- **Resposta 201**: sem corpo.

### DELETE `/post/{postId}`

- Remove post criado pelo usuário autenticado.
- **Resposta 204**: sucesso.
- **Erros**: 401 (não é dono), 404 (post inexistente).

### GET `/post/{postId}`

- **Resposta 200** (`PostDetailResponse`):

```json
{
  "id": "953f575e-ca17-428a-8d4c-095a312315d5",
  "imageUrl": "/images/ac262053-...jpeg",
  "description": "aodkaweoksopdwaopk!",
  "likesCount": 0,
  "createdAt": "2025-11-21T17:52:43.127Z",
  "author": {
    "id": "ac262053-0516-4095-8895-856a000a62fe",
    "name": "Pedro Moretti",
    "username": "morettipdr",
    "profilePictureUrl": "/images/pic.png"
  },
  "likedBy": []
}
```

### POST `/post/{postId}/like`

- Cria like associado ao usuário autenticado.
- **Resposta 200**: sem corpo.
- **Erros**: 404 (post), 409 (like duplicado).

### DELETE `/post/{postId}/like`

- Remove like.
- **Resposta 200**: sem corpo.
- **Erros**: 404 (like inexistente ou post inexistente).

---

## 📰 Feed (`/feed`)

### GET `/feed`

- **Query params**: `page` (default 0), `size` (default 20).
- **Resposta 200**: lista de itens do feed, cada um contendo metadados de classificação calculados no serviço.

```json
[
  {
    "id": "22c453d6-0f7c-4421-ba1e-fcd1cba603b4",
    "imageProfileUrl": "/images/2d77e841-aa54-4a9d-b297-8d2f4a1feb4b_...png",
    "username": "natan8643",
    "description": "ICC norte do Darcy é maioral, UnB do gama melhore",
    "imageUrl": "/images/2d77e841-aa54-4a9d-b297-8d2f4a1feb4b_...jpeg",
    "likes": 2,
    "type": "ORGANIZATION", // FOLLOWED | SECOND_DEGREE | POPULAR | RANDOM
    "reason": "Post de organização"
  }
]
```

- Classificações possíveis (`PostType`): `ORGANIZATION`, `FOLLOWED`, `SECOND_DEGREE`, `POPULAR`, `RANDOM`.

### GET `/feed/organization`

- Retorna texto fixo: `"Bem-vindo à área exclusiva de organizações!"`.

---

## 🎟️ Eventos (`/event`)

### POST `/event`

- **Requer:** usuário autenticado com `ProfileType.organization`.
- **Content-Type:** `multipart/form-data` usando `EventRequestDTO`.
- **Campos principais**: `title`, `eventTime` (ISO-8601), `description`, `image`, `streetAddress`, `administrativeRegion`, `city`, `state`, `referencePoint`, `latitude`, `longitude`.
- **Resposta 201**:

```json
{
  "id": "a0c33f9f-0f9e-4d9d-b111-2b13997f6a63",
  "message": "Evento criado com sucesso"
}
```

### GET `/event/lists`

- Lista resumida de eventos.

```json
[
  {
    "title": "Feira da Engenharia",
    "imageUrl": "/images/evento.png",
    "data": "2025-12-01T18:00:00",
    "id": "b3e8f6b5-3c18-4874-86be-16a6d2d58b35"
  }
]
```

---

## 🧾 Erros comuns

| Situação                                         | Resposta                                             |
| ------------------------------------------------ | ---------------------------------------------------- |
| Upload sem imagem                                | `400 Bad Request` com mensagem do Spring Validation. |
| JWT ausente                                      | `401 Unauthorized`.                                  |
| Usuário (ProfileType user) tentando criar evento | `403 Forbidden`.                                     |
| ID inexistente                                   | `404 Not Found`.                                     |
| Like duplicado                                   | `409 Conflict`.                                      |

---

## 🧪 Testar mais rápido (cURL)

```bash
# Registrar e obter token
curl -X POST http://localhost:8080/auth/register \
	-H "Content-Type: application/json" \
	-d '{"name":"Org", "username":"org", "email":"org@example.com", "password":"123456", "type":"organization"}'

# Login (retorna JWT)
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
	-H "Content-Type: application/json" \
	-d '{"email":"org@example.com","password":"123456"}')

# Criar post
curl -X POST http://localhost:8080/post \
	-H "Authorization: Bearer ${TOKEN}" \
	-F "image=@/tmp/pic.jpg" \
	-F "description=Primeiro post"
```

---

## ✅ Check-list rápido antes de integrar

- [x] Adicionar header `Authorization` em rotas protegidas.
- [x] Enviar `Content-Type` correto (`application/json` ou `multipart/form-data`).
- [x] Converter datas para ISO-8601 (UTC) ao chamar a API.
- [x] Usar IDs UUID válidos nos paths.
