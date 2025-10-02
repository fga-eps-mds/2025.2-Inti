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
