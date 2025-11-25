<<<<<<< HEAD
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
=======
# MUSA

[![Version](https://img.shields.io/badge/version-2.0.0-blue.svg)](https://github.com/FGA0138-MDS-Ajax/2025.2-Inti)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)
[![HTML5](https://img.shields.io/badge/HTML5-E34F26.svg?logo=html5&logoColor=white)](https://developer.mozilla.org/en-US/docs/Web/HTML)
[![CSS3](https://img.shields.io/badge/CSS3-1572B6.svg?logo=css3&logoColor=white)](https://developer.mozilla.org/en-US/docs/Web/CSS)
[![JavaScript](https://img.shields.io/badge/JavaScript-F7DF1E.svg?logo=javascript&logoColor=black)](https://developer.mozilla.org/en-US/docs/Web/JavaScript)
[![Capacitor](https://img.shields.io/badge/Capacitor-119EFF.svg?logo=capacitor&logoColor=white)](https://capacitorjs.com/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-6DB33F.svg?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1.svg?logo=postgresql&logoColor=white)](https://www.postgresql.org/)

Repositório principal do aplicativo **MUSA** desenvolvido pelo grupo `Inti` da turma 2025.2 da disciplina de Métodos de Desenvolvimento de Software (MDS) da Faculdade de Ciências e Tecnologia em Engenharias da Universidade de Brasília (FCTE - UnB).

---

## Sobre o Projeto

O **MUSA** é uma aplicação web moderna desenvolvida com HTML5, CSS3 e JavaScript puro, seguindo o padrão **SPA (Single Page Application)**. A arquitetura foi projetada para ser **web-centric**, garantindo um _single-source-of-truth_ (fonte única de verdade) para o código.

### Distribuição Mobile

A aplicação pode ser distribuída para plataformas móveis (iOS e Android) através do **Capacitor**, que atua como um _wrapper_ nativo instanciando uma **WebView** que carrega o site web principal a partir de sua URL hospedada.

---

## Arquitetura

O projeto MUSA segue uma arquitetura moderna de três camadas:

![Arquitetura MUSA](./assets/ArquiteturaDOCS.png)

### Componentes do Sistema

#### 1. **Frontend (Cliente Web)**

- Aplicação SPA em HTML/CSS/JavaScript puro
- Hospedagem em servidor estático
- Roteamento client-side
- Comunicação com backend via REST API

#### 2. **Mobile (Capacitor)**

- Wrapper nativo para iOS e Android
- WebView integrada
- Acesso a recursos nativos (notificações, câmera, etc.)
- Carregamento do frontend via URL remota

#### 3. **Backend (API REST)**

- Implementado em Java Spring Boot
- Autenticação e autorização
- Lógica de negócio
- Persistência em PostgreSQL

### Fluxo de Dados

```
Usuário → App Nativo (Capacitor) → WebView →
  ├─→ Frontend (HTML/JS/CSS)
  └─→ Backend API (Spring Boot) → PostgreSQL
>>>>>>> feature/announce-service
```

---

<<<<<<< HEAD
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
=======
## 📂 Estrutura de Diretórios

```
musa/
│
├── .vscode/                    # Configurações do VS Code
│   └── settings.json           # Configurações do workspace
│
├── css/                        # Folhas de estilo
│   └── style.css               # Estilos globais da aplicação
│
├── js/                         # Scripts JavaScript
│   ├── app.js                  # Orquestrador principal da aplicação
│   ├── auth.js                 # Módulo de autenticação e gestão de sessão
│   └── router.js               # Sistema de roteamento SPA
│
├── pages/                      # Views/Templates HTML
│   ├── login.html              # Página de autenticação
│   ├── cadastro.html           # Página de registro de usuário
│   ├── home.html               # Página inicial (dashboard)
│   ├── eventos.html            # Listagem de eventos
│   ├── create.html             # Criação de novos eventos
│   ├── search.html             # Busca e filtros
│   └── user.html               # Perfil do usuário
│
├── assets/                     # Recursos estáticos
│   └── ArquiteturaDOCS.png     # Diagrama de arquitetura
│
├── node_modules/               # Dependências do projeto (gerenciadas pelo npm)
│
├── .prettierignore             # Arquivos ignorados pelo Prettier
├── .prettierrc                 # Configurações do Prettier
├── index.html                  # Ponto de entrada da aplicação (SPA)
├── package.json                # Dependências e scripts npm
├── package-lock.json           # Lock de versões das dependências
└── README.md                   # Documentação do projeto
>>>>>>> feature/announce-service
```

---

<<<<<<< HEAD
Se quiser que eu adicione exemplos de request/response em Java (RestTemplate/WebClient) ou em JS (fetch/axios), diga
qual você prefere e eu adiciono.
=======
## Pré-requisitos

- **[Git](https://git-scm.com/downloads)** - Controle de versão
- **[Node.js](https://nodejs.org/)** (v18 ou superior) - Runtime JavaScript
- **[npm](https://www.npmjs.com/)** - Gerenciador de pacotes (incluído com Node.js)

### Recomendados

- **[Visual Studio Code](https://code.visualstudio.com/)** - Editor de código
- **Extensão [Live Server](https://marketplace.visualstudio.com/items?itemName=ritwickdey.LiveServer)** - Servidor de desenvolvimento com hot-reload
- **Extensão [Prettier](https://marketplace.visualstudio.com/items?itemName=esbenp.prettier-vscode)** - Formatação automática de código

## 🚀 Como Rodar o Projeto

### 1️⃣ Clone o Repositório

```bash
git clone https://github.com/fga-eps-mds/2025.2-Inti.git
cd 2025.2-Inti
```

### 2️⃣ Instale as Dependências

```bash
npm install
```

### 3️⃣ Execute o Projeto

#### 🐧 **Linux / macOS** e 💻 **Windows**

- Instale a extensão [Live Server](https://marketplace.visualstudio.com/items?itemName=ritwickdey.LiveServer) no VS Code
- Clique com botão direito em index.html
- Selecione "Open with Live Server"

#### Teste de Responsividade Mobile

Para testar a aplicação em diferentes tamanhos de tela:

1. Abra as **DevTools** do navegador (F12)
2. Ative o **modo de visualização mobile** (Ctrl+Shift+M)
3. Teste em diferentes resoluções

**Extensão Recomendada**: [Mobile Simulator](https://chromewebstore.google.com/detail/mobile-simulator-responsi/ckejmhbmlajgoklhgbapkiccekfoccmk) para Chrome

---

## Padrões de Desenvolvimento

**SEMPRE** execute o comando abaixo antes de fazer commit:

```bash
npx prettier --write .
```

- Formate o código antes de cada commit
- Teste suas alterações antes de commitar

```bash
# Fluxo correto de commit
npx prettier --write .
git add .
git commit -m "[FEAT]: adiciona página de eventos"
git push
```

---

## Tecnologias Utilizadas

### Frontend

- **HTML5** - Estrutura semântica
- **CSS3** - Estilização e responsividade
- **JavaScript (ES6+)** - Lógica da aplicação

### Ferramentas de Desenvolvimento

- **Node.js** - Runtime JavaScript
- **npm** - Gerenciamento de pacotes
- **Prettier** - Formatação de código
- **Live Server** - Servidor de desenvolvimento

### Mobile

- **Capacitor** - Wrapper nativo para iOS/Android

### Backend (Separado)

- **Java 17+**
- **Spring Boot** - Framework backend
- **PostgreSQL** - Banco de dados relacional
- **JWT** - Autenticação e autorização
>>>>>>> feature/announce-service
