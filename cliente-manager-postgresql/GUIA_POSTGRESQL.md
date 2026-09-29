# Guia do PostgreSQL

Este sistema usa **exclusivamente PostgreSQL** para armazenar dados reais
(cidadaos, atendimentos, usuarios, agenda e, se habilitado, mensagens). Nao
ha suporte a H2 fora dos testes automatizados (`mvn test`), e nao deve ser
apresentado como "configurado" para uso normal - ele so existe em
`src/test/resources/application.properties`.

## Opcao 1: PostgreSQL local via Docker

Ja existe um `docker-compose.yml` na raiz do projeto:

```bash
docker compose up -d
```

Isso sobe um PostgreSQL 16 com:

- Banco: `gabinete_db`
- Usuario: `postgres`
- Senha: `postgres`
- Porta: `5432`

Esses valores servem apenas para desenvolvimento local. Nunca use senhas assim
em producao.

Para conferir se subiu corretamente:

```bash
docker compose ps
docker compose logs postgres --tail 50
```

Para parar (mantendo os dados no volume):

```bash
docker compose down
```

Para apagar tambem os dados (recomeçar do zero):

```bash
docker compose down -v
```

## Opcao 2: PostgreSQL instalado localmente

Se preferir nao usar Docker, instale o PostgreSQL normalmente para seu sistema
operacional e crie um banco:

```sql
CREATE DATABASE gabinete_db;
CREATE USER gabinete_app WITH PASSWORD 'defina-uma-senha-forte';
GRANT ALL PRIVILEGES ON DATABASE gabinete_db TO gabinete_app;
```

Depois aponte as variaveis de ambiente:

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/gabinete_db
export SPRING_DATASOURCE_USERNAME=gabinete_app
export SPRING_DATASOURCE_PASSWORD=defina-uma-senha-forte
```

## Como as tabelas sao criadas

`spring.jpa.hibernate.ddl-auto=update` esta configurado em
`application.properties`. Isso significa que, ao iniciar, o Hibernate cria/
atualiza automaticamente as tabelas com base nas entidades (`Cliente`,
`Servico`, `Usuario`, `Compromisso`, `Conversa`, `Mensagem`). Nao ha scripts
SQL manuais a rodar.

Isso e adequado para uma aplicacao de porte pequeno/medio como esta. Se no
futuro voce quiser controle de schema mais rigoroso (migrations versionadas),
considere adotar Flyway ou Liquibase - isso nao esta implementado neste
projeto.

## PostgreSQL no Render

Veja o passo a passo completo em [GUIA_RENDER.md](GUIA_RENDER.md). Resumo:

1. Crie um recurso "PostgreSQL" no painel do Render, um por cliente/instalacao.
2. O Render fornece uma "Internal Database URL" (ou externa) - use-a para
   compor `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` e
   `SPRING_DATASOURCE_PASSWORD` como variaveis de ambiente do Web Service.
3. Nunca compartilhe o mesmo banco PostgreSQL entre dois clientes diferentes.

## Backup

Para ambientes de producao (fora do escopo automatizado deste projeto),
recomenda-se configurar backups automaticos no proprio Render (recurso pago)
ou via `pg_dump` agendado. Ex.:

```bash
pg_dump "$SPRING_DATASOURCE_URL" -U "$SPRING_DATASOURCE_USERNAME" -F c -f backup.dump
```
