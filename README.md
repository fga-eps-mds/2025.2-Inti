# Inti API & Operations Guide

> Aplicação Spring Boot responsável pelo backend da rede social **Inti**. Este documento reúne as instruções para executar o projeto e a documentação das rotas expostas (método, payloads esperados, respostas e observações de autenticação).

---

## 📦 Como rodar o projeto com Docker

### Arquitetura em alto nível

- **Backend**: Java 17 + Spring Boot (REST API, autenticação, feed, posts, eventos etc.).
- **PostgreSQL**: banco relacional para perfis, posts e eventos.
- **Docker Compose**: orquestra os containers e garante rede interna compartilhada.

## 🎟️ Eventos (`/event`)

### POST `/event`

- **Autenticação:** JWT obrigatório; apenas perfis com `ProfileType.organization` podem criar.
- **Content-Type:** `multipart/form-data` em `EventRequestDTO`.
- **Campos principais:**
  - `title` (string)
  - `eventTime` (Instant ISO-8601)
  - `description` (string)
  - `image` (arquivo opcional)
  - `streetAddress`, `administrativeRegion`, `city`, `state`, `referencePoint`
  - `latitude`, `longitude` (BigDecimal)
- **Resposta 201** (`EventResponseDTO`): inclui `id` do evento recém-criado.

```json
{
  "id": "a0c33f9f-0f9e-4d9d-b111-2b13997f6a63",
  "message": "Evento criado com sucesso"
}
```

### GET `/event/{eventid}`

- **Path params:** `eventid` (UUID).
- **Autenticação:** opcional; quando informada, o backend indica se o usuário já está inscrito.
- **Resposta 200** (`EventDetailResponse`): título, descrição, localização, horários e participantes.

### GET `/event/my`

- **Autenticação:** JWT obrigatório.
- **Resposta 200** (`List<MyEvent>`): eventos criados pelo perfil autenticado.

### POST `/event/{eventid}/attendees`

- **Path params:** `eventid` (UUID).
- **Autenticação:** JWT obrigatório.
- **Resposta 200** (`EventParticipantResponse`): confirma a inscrição e devolve identificadores da relação.

### DELETE `/event/{eventid}/attendees`

- **Path params:** `eventid` (UUID).
- **Autenticação:** JWT obrigatório.
- **Resposta 204**: cancela a inscrição do usuário no evento.

### GET `/event/lists`

- **Autenticação:** não requer JWT (público).
- **Resposta 200** (`List<EventListResponse>`): cartões com `title`, `imageUrl`, `data` e `id`.

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

### GET `/event/following`

- **Autenticação:** JWT obrigatório; utiliza o grafo de follows para montar a lista.
- **Resposta 200** (`List<EventFollowingDTO>`): eventos promovidos por perfis que o usuário segue.

---

## 🏢 Organizações (`/org`)

### GET `/org`

- **Autenticação:** JWT obrigatório; o usuário logado precisa representar uma organização.
- **Query params:** `page` e `size` controlam os posts paginados anexados à resposta.
- **Resposta 200** (`ProfileResponse`): dados completos da organização autenticada.

### GET `/org/{username}`

- **Path params:** `username` (identificador público).
- **Query params:** `page` e `size` (inteiros).
- **Resposta 200** (`ProfileResponse`): visão pública de outra organização.

### POST `/org`

- **Autenticação:** JWT obrigatório.
- **Content-Type:** `multipart/form-data` com campo `myImage` (arquivo obrigatório).
- **Resposta 201**: confirma atualização da foto institucional.

### PATCH `/org`

- **Autenticação:** JWT obrigatório.
- **Content-Type:** `multipart/form-data` mapeado para `UpdateUserRequest` (`name`, `username`, `phone`, `publicemail`, `userBio`, `profilePicture`).
- **Resposta 201**: dados atualizados da organização.

### POST `/org/{username}/follow`

- **Path params:** `username` (organização a seguir).
- **Autenticação:** JWT obrigatório.
- **Resposta 200** (`FollowResponse`): confirma follow.

### DELETE `/org/{username}/unfollow`

- **Path params:** `username`.
- **Autenticação:** JWT obrigatório.
- **Resposta 200** (`FollowResponse`): confirma remoção do follow.

---

## 🔎 Busca (`/search`)

### GET `/search/{username}`

- **Path params:** `username` (string completa a ser buscada).
- **Autenticação:** não requer JWT.
- **Resposta 200** (`SearchProfile`): resumo com `id`, `username`, `name`, `profilePictureUrl` e indicador `isOrganization`.

---

## 🌐 Geocoding (`/geo`)

### GET `/geo/reverse`

- **Query params obrigatórios:** `lat` (double), `lng` (double).
- **Query param opcional:** `lang` (locale, ex.: `pt-BR`). Se omitido, o backend usa o valor padrão configurado.
- **Autenticação:** não requer JWT.
- **Resposta 200**: JSON bruto do Nominatim (`format=jsonv2`), incluindo endereço e componentes derivados.

---

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

| Domínio      | Método | Caminho                              | Resumo                                                         |
| ------------ | ------ | ------------------------------------ | -------------------------------------------------------------- |
| Autenticação | POST   | `/auth/register`                     | Cria usuário e retorna `ProfileCreationResponse` + JWT.        |
| Autenticação | POST   | `/auth/login`                        | Valida credenciais e retorna `LoginResponse` com token.        |
| Autenticação | GET    | `/auth`                              | Endpoint simples para verificar se a API está de pé.           |
| Perfil       | GET    | `/profile/me`                        | Perfil do usuário autenticado (query `page`/`size`).           |
| Perfil       | GET    | `/profile/{username}`                | Perfil público incluindo posts (query `page`/`size`).          |
| Perfil       | POST   | `/profile/upload-me`                 | Atualiza foto do perfil via multipart (`myImage`).             |
| Perfil       | PATCH  | `/profile/update`                    | Atualiza dados cadastrais via multipart (`UpdateUserRequest`). |
| Perfil       | POST   | `/profile/{username}/follow`         | Segue o perfil indicado.                                       |
| Perfil       | DELETE | `/profile/{username}/unfollow`       | Remove follow do perfil indicado.                              |
| Perfil       | GET    | `/profile/string/teste/organization` | Endpoint protegido para validar ROLE_ORGANIZATION.             |
| Perfil       | GET    | `/profile/{profileId}/products`      | Lista produtos de um perfil (query `page`/`size`).             |
| Organização  | GET    | `/org`                               | Perfil da organização autenticada (query `page`/`size`).       |
| Organização  | GET    | `/org/{username}`                    | Perfil público de organização (query `page`/`size`).           |
| Organização  | POST   | `/org`                               | Atualiza foto da organização (multipart `myImage`).            |
| Organização  | PATCH  | `/org`                               | Atualiza dados via `UpdateUserRequest`.                        |
| Organização  | POST   | `/org/{username}/follow`             | Segue organização.                                             |
| Organização  | DELETE | `/org/{username}/unfollow`           | Remove follow de organização.                                  |
| Post         | POST   | `/post`                              | Cria post com imagem + descrição.                              |
| Post         | DELETE | `/post/{postId}`                     | Remove post do usuário logado.                                 |
| Post         | GET    | `/post/{postId}`                     | Detalhes completos do post, incluindo curtidas.                |
| Post         | POST   | `/post/{postId}/like`                | Cria like para o post.                                         |
| Post         | DELETE | `/post/{postId}/like`                | Remove like existente.                                         |
| Eventos      | POST   | `/event`                             | Cria evento (somente perfis organization).                     |
| Eventos      | GET    | `/event/{eventid}`                   | Retorna detalhes completos do evento.                          |
| Eventos      | GET    | `/event/my`                          | Lista eventos criados pelo usuário autenticado.                |
| Eventos      | POST   | `/event/{eventid}/attendees`         | Inscreve o usuário em um evento.                               |
| Eventos      | DELETE | `/event/{eventid}/attendees`         | Cancela a inscrição no evento.                                 |
| Eventos      | GET    | `/event/lists`                       | Lista pública de eventos.                                      |
| Eventos      | GET    | `/event/following`                   | Eventos promovidos por perfis que você segue.                  |
| Feed         | GET    | `/feed`                              | Feed personalizado paginado para o usuário autenticado.        |
| Feed         | GET    | `/feed/organization`                 | Mensagem/landing para organizações.                            |
| Produtos     | POST   | `/products`                          | Cria produto (multipart + JWT).                                |
| Produtos     | GET    | `/products`                          | Lista pública paginada de produtos.                            |
| Produtos     | GET    | `/products/{id}`                     | Detalhes públicos de um produto.                               |
| Produtos     | GET    | `/products/profile/{profileId}`      | Produtos públicos vinculados a um perfil.                      |
| Produtos     | PUT    | `/products/{id}`                     | Atualiza produto do usuário autenticado.                       |
| Produtos     | DELETE | `/products/{id}`                     | Remove produto do usuário autenticado.                         |
| Imagens      | GET    | `/images/{blobName}`                 | Baixa a imagem original a partir do Blob Storage.              |
| Busca        | GET    | `/search/{username}`                 | Busca perfil por username (público).                           |
| Geocoding    | GET    | `/geo/reverse`                       | Proxy para Nominatim (lat/lng e idioma).                       |

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

Cada endpoint abaixo indica se exige **JWT** ou se é público.

### GET `/profile/me`

- **Autenticação:** JWT obrigatório.
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

- **Autenticação:** público.
- **Mesmos query params**: `page`, `size`.
- **Resposta**: `ProfileResponse` para o usuário solicitado.

### POST `/profile/upload-me`

- **Autenticação:** JWT obrigatório.
- **Content-Type**: `multipart/form-data`
- **Campo obrigatório**: `myImage` (arquivo).
- **Resposta**: `201 Created` sem corpo.

### PATCH `/profile/update`

- **Autenticação:** JWT obrigatório.
- **Content-Type**: `multipart/form-data`
- **Campos aceitos** (`UpdateUserRequest`): `name`, `username`, `phone`, `publicemail`, `userBio`, `profilePicture`.
- **Resposta**: `201 Created` sem corpo.

### POST `/profile/{username}/follow`

- **Path params**: `username` (string).
- **Requer JWT**: usa o perfil autenticado do token.
- Ação: segue o usuário indicado, atualizando a contagem de seguidores/seguidos.
- **Resposta 200** (`FollowResponse`):

```json
{ "message": "Perfil seguido com sucesso." }
```

### DELETE `/profile/{username}/unfollow`

- **Path params**: `username` (string).
- **Requer JWT**.
- Remove o follow previamente criado.
- **Resposta 200** (`FollowResponse`): retorna mensagem informando que o follow foi removido.

### GET `/profile/string/teste/organization`

- **Autenticação**: exige JWT cujo perfil possua `ROLE_ORGANIZATION`.
- Sem parâmetros.
- **Resposta 200**: corpo de texto simples `"teste"`.

### GET `/profile/{profileId}/products`

- **Path params**: `profileId` (UUID do perfil desejado).
- **Query params** (opcionais, default `page=0`, `size=10`): controlam a paginação.
- **Resposta 200**: `Page<ProductSummaryDTO>` contendo lista paginada de produtos do perfil.
- **Observações**: endpoint público; pode ser usado por perfis ou visitantes para listar produtos de artistas específicos.

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

## 🛍️ Produtos (`/products`)

Alguns endpoints são públicos (consulta) e outros exigem JWT (criação/edição/remoção).

### POST `/products`

- **Requer:** JWT (usuário autenticado).
- **Content-Type**: `multipart/form-data`.
- **Campos obrigatórios** (`CreateProductDTO`):
  - `title` (string)
  - `description` (string)
  - `price` (decimal)
  - `image` (arquivo) — opcional em alguns fluxos, mas aceito aqui.
- **Resposta 201** (`ProductResponseDTO`): retorna o produto criado.

Exemplo cURL:

```bash
curl -X POST http://localhost:8080/products \
  -H "Authorization: Bearer ${TOKEN}" \
  -F "title=Caneca personalizada" \
  -F "description=Caneca de cerâmica 350ml" \
  -F "price=39.90" \
  -F "image=@/tmp/mug.jpg"
```

### GET `/products`

- **Requer:** nenhum (público).
- **Query params**: `page` (default 0), `size` (default 10).
- **Resposta 200**: página de `ProductResponseDTO`.

Exemplo:

```bash
curl http://localhost:8080/products?page=0&size=10
```

### GET `/products/{id}`

- **Requer:** nenhum (público).
- **Resposta 200** (`ProductResponseDTO`): detalhes do produto.

Exemplo:

```bash
curl http://localhost:8080/products/<PRODUCT_ID>
```

### GET `/products/profile/{profileId}`

- **Requer:** nenhum (público).
- **Path params:** `profileId` (UUID do dono dos produtos).
- **Resposta 200**: lista de `ProductResponseDTO` pertencentes ao perfil.
- **Uso típico:** montar a vitrine de um artista específico em outra tela.

### PUT `/products/{id}`

- **Requer:** JWT (somente o dono do produto pode editar).
- **Content-Type**: `multipart/form-data`.
- **Campos aceitos** (`EditProductDTO`): `title`, `description`, `price`, `image` (todos opcionais).
- **Resposta 200** (`ProductResponseDTO`): produto atualizado.

Exemplo cURL (atualizar título e imagem):

```bash
curl -X PUT http://localhost:8080/products/<PRODUCT_ID> \
  -H "Authorization: Bearer ${TOKEN}" \
  -F "title=Novo título" \
  -F "image=@/tmp/new.jpg"
```

### DELETE `/products/{id}`

- **Requer:** JWT (somente o dono do produto pode remover).
- **Resposta 204**: sem conteúdo.

Exemplo:

```bash
curl -X DELETE http://localhost:8080/products/<PRODUCT_ID> \
  -H "Authorization: Bearer ${TOKEN}"
```

Formato de `ProductResponseDTO` (exemplo):

```json
{
  "id": "a1b2c3d4-...",
  "profileId": "394a77ba-9e56-47e7-a3d4-715dba81eaf9",
  "profileUsername": "artista_legal",
  "profileName": "Artista Legal",
  "profilePictureUrl": "https://cdn.inti.app/avatars/394a77ba-9e56.png",
  "title": "Caneca personalizada",
  "description": "Caneca de cerâmica 350ml",
  "price": 39.9,
  "imgLink": "/images/abcd-...jpg",
  "createdAt": "2025-11-25T17:08:15.123Z"
}
```

Observações:

- Os DTOs envolvidos são `CreateProductDTO`, `EditProductDTO` e `ProductResponseDTO`.
- `price` usa formato decimal (BigDecimal no backend).
- Uploads de imagem seguem os mesmos tipos aceitos pela API (`image/jpeg`, `image/png`, `image/webp`, etc.).

---

## 📰 Feed (`/feed`)

### GET `/feed`

- **Autenticação**: JWT obrigatório; sem token a API responde `401`.
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

- **Autenticação**: não requer JWT.
- **Resposta 200**: texto fixo `"Bem-vindo à área exclusiva de organizações!"`.

---

## Erros comuns

| Situação                                         | Resposta                                             |
| ------------------------------------------------ | ---------------------------------------------------- |
| Upload sem imagem                                | `400 Bad Request` com mensagem do Spring Validation. |
| JWT ausente                                      | `401 Unauthorized`.                                  |
| Usuário (ProfileType user) tentando criar evento | `403 Forbidden`.                                     |
| ID inexistente                                   | `404 Not Found`.                                     |
| Like duplicado                                   | `409 Conflict`.                                      |

---

## Testar mais rápido (cURL)

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
