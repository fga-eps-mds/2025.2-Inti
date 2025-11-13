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

O **MUSA** é uma aplicação web moderna desenvolvida com HTML5, CSS3 e JavaScript puro, seguindo o padrão **SPA (Single Page Application)**. A arquitetura foi projetada para ser **web-centric**, garantindo um *single-source-of-truth* (fonte única de verdade) para o código.


### Distribuição Mobile

A aplicação pode ser distribuída para plataformas móveis (iOS e Android) através do **Capacitor**, que atua como um *wrapper* nativo instanciando uma **WebView** que carrega o site web principal a partir de sua URL hospedada.

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
```

---

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
```

---

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

