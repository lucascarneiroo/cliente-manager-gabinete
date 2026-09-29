# Gestao de Gabinete

Sistema de organizacao de cidadaos, atendimentos e compromissos de um gabinete
parlamentar. Este e um **modelo comercial generico**: nao esta associado a
nenhum vereador, partido ou gabinete especifico. Cada cliente recebe sua
propria instalacao, personalizada com nome, logotipo e cores proprios (veja
[PERSONALIZACAO.md](PERSONALIZACAO.md)).

## Objetivo

- Servir de **demonstracao comercial** (perfil `demo`, com dados ficticios) para
  apresentar o sistema a possiveis clientes (vereadores/gabinetes).
- Servir de **base reutilizavel** que, apos a venda, e personalizada e implantada
  como uma instalacao independente para cada gabinete (banco, dominio, usuarios
  e agenda proprios).
- Cada gabinete usa uma instalacao no Render e um PostgreSQL exclusivo. **Este
  projeto nao implementa multi-tenancy**: um deploy = um gabinete.

Fluxo comercial: modelo generico -> demonstracao -> venda -> personalizacao ->
instalacao e banco exclusivos.

## Funcionalidades

- Cadastro, edicao, exclusao e busca de cidadaos (por nome, CPF ou telefone)
- Validacao real de CPF (digitos verificadores) e bloqueio de CPF duplicado
- Historico de atendimentos por cidadao (data, assunto, observacoes, valor
  opcional, status realizado/pendente)
- Aviso de aniversariantes do dia
- **Login obrigatorio** (Spring Security), com perfis **ADMIN** e **ATENDENTE**
- Gerenciamento de usuarios (criar, bloquear, reativar, redefinir senha) restrito
  a administradores
- **Agenda do gabinete**: compromissos com tipo, situacao, prioridade, deteccao
  de conflito de horario, visualizacoes por dia/semana/mes
- **Modulo de mensagens/WhatsApp** (opcional, desabilitado por padrao): integracao
  com a Cloud API oficial da Meta, com simulacao visual no perfil de demonstracao
- Identidade visual (nome, logotipo, cores, rodape) centralizada e configuravel
  por variaveis de ambiente, sem precisar mexer no codigo
- API REST em `/api/clientes` (protegida por login; como o CSRF continua
  habilitado globalmente, chamadas `POST`/`PUT`/`DELETE` via API exigem uma
  sessao autenticada com o token CSRF correspondente - a mesma sessao do
  navegador, nao um token de API separado)
- Interface responsiva em portugues do Brasil

## Tecnologias

Java 17, Spring Boot 3, Spring Data JPA/Hibernate, Spring Security, Thymeleaf,
Maven, PostgreSQL, HTML/CSS responsivo. Nenhuma outra stack foi introduzida.

## Pre-requisitos

- Java 17+
- Maven (ou use `mvn` via gerenciador de pacotes: `brew install maven`,
  `choco install maven`, `apt install maven`, etc.)
- PostgreSQL (local, via Docker, ou um servico gerenciado como o Render) - este
  projeto usa **exclusivamente PostgreSQL** em desenvolvimento e producao. Um
  banco H2 em memoria e usado **somente durante `mvn test`** (veja abaixo);
  isso nao substitui o PostgreSQL na execucao normal da aplicacao.

## Configuracao (variaveis de ambiente)

Nenhuma senha ou token real deve ser colocado no `application.properties` ou no
GitHub. Configure via variaveis de ambiente (ou um arquivo `.env` local, que
esta no `.gitignore`):

| Variavel | Obrigatoria | Descricao |
|---|---|---|
| `SPRING_DATASOURCE_URL` | sim (fora do H2 de teste) | Ex: `jdbc:postgresql://localhost:5432/gabinete_db` |
| `SPRING_DATASOURCE_USERNAME` | sim | Usuario do PostgreSQL |
| `SPRING_DATASOURCE_PASSWORD` | sim | Senha do PostgreSQL |
| `PORT` | nao | Porta HTTP (o Render define automaticamente) |
| `APP_ADMIN_NOME` | sim, na 1a execucao | Nome do primeiro administrador |
| `APP_ADMIN_USUARIO` | sim, na 1a execucao | Usuario do primeiro administrador |
| `APP_ADMIN_SENHA` | sim, na 1a execucao | Senha do primeiro administrador (min. 10 caracteres) |
| `APP_NOME_GABINETE`, `APP_NOME_VEREADOR`, `APP_SUBTITULO`, `APP_LOGO`, `APP_COR_PRIMARIA`, `APP_COR_SECUNDARIA`, `APP_RODAPE` | nao | Personalizacao visual - ver PERSONALIZACAO.md |
| `APP_DEMO_USUARIO`, `APP_DEMO_SENHA` | somente no perfil `demo` | Usuario de demonstracao (perfil ATENDENTE) |
| `WHATSAPP_HABILITADO` e demais `WHATSAPP_*` | nao | Ver GUIA_WHATSAPP.md |

## Execucao local

```bash
# Opcao 1: PostgreSQL via Docker (ver GUIA_POSTGRESQL.md)
docker compose up -d

export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/gabinete_db
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=postgres
export APP_ADMIN_NOME="Administrador"
export APP_ADMIN_USUARIO=admin
export APP_ADMIN_SENHA="DefinaUmaSenhaForte123"

mvn spring-boot:run
```

Acesse `http://localhost:8080`, faca login com o usuario/senha definidos acima
e troque a senha assim que possivel (nao ha tela de "esqueci minha senha":
a redefinicao e feita por outro administrador em **Usuarios**).

## Gerar o JAR

```bash
mvn clean package
java -jar target/cliente-manager-1.0.0.jar
```

## Testes automatizados

```bash
mvn test
```

Os testes usam um banco H2 em memoria configurado em
`src/test/resources/application.properties`, exclusivamente para a execucao de
testes - a aplicacao real continua usando somente PostgreSQL.

> **Nota sobre este repositorio**: o codigo foi escrito e revisado
> manualmente com atencao a correcao de sintaxe e das APIs do Spring Boot 3 /
> Spring Security 6, mas `mvn clean package` e `mvn test` **precisam ser
> executados no seu ambiente** (com acesso ao Maven Central) para a validacao
> final, pois o ambiente onde este projeto foi preparado nao tinha acesso a
> rede liberado para baixar as dependencias do Maven. Rode os dois comandos
> acima antes de colocar em producao.

## Login, perfis e permissoes

Resumo:

- **ADMIN**: acesso total, inclusive gerenciar usuarios e excluir registros
  definitivamente.
- **ATENDENTE**: cadastra/edita cidadaos e atendimentos, gerencia a agenda
  (exceto exclusao definitiva), responde mensagens atribuidas; nao gerencia
  usuarios nem exclui cidadaos/atendimentos/compromissos definitivamente.

O primeiro administrador e criado automaticamente na primeira execucao, apenas
se a tabela `usuarios` estiver vazia, usando `APP_ADMIN_NOME`,
`APP_ADMIN_USUARIO` e `APP_ADMIN_SENHA`. Sem essas variaveis, a aplicacao
recusa iniciar (mensagem clara, sem senha padrao previsivel).

## Agenda

Veja [GUIA_AGENDA.md](GUIA_AGENDA.md).

## Modo de demonstracao

Ative com `SPRING_PROFILES_ACTIVE=demo`. Carrega cidadaos, atendimentos e
compromissos ficticios (apenas se o banco estiver vazio), exibe a faixa
"Ambiente de demonstracao" e nunca envia mensagens reais de WhatsApp. Veja
detalhes em [PERSONALIZACAO.md](PERSONALIZACAO.md#modo-de-demonstracao).

## Personalizacao

Veja [PERSONALIZACAO.md](PERSONALIZACAO.md).

## Implantacao no Render

Veja [GUIA_RENDER.md](GUIA_RENDER.md).

## Integracao opcional com WhatsApp

Veja [GUIA_WHATSAPP.md](GUIA_WHATSAPP.md). Resumo: desabilitada por padrao
(`app.whatsapp.habilitado=false`); quando desabilitada, nada tenta se conectar
a Meta e o restante do sistema funciona normalmente. Usa exclusivamente a
Cloud API oficial da Meta - nenhuma automacao de navegador, QR code ou
biblioteca nao oficial.

### Limitacoes da integracao

- A implementacao (`WhatsappCloudApiService`) segue a documentacao oficial da
  Cloud API, mas **nao pode ser validada de ponta a ponta** sem uma conta
  comercial da Meta aprovada e um numero de telefone habilitado. Teste com sua
  propria conta antes de usar em producao.
- Nao ha disparo em massa, nem contorno da janela de 24h de atendimento, nem
  qualquer uso para propaganda eleitoral - por design.

## Seguranca e LGPD

- Login obrigatorio em todas as paginas e na API; unicas rotas publicas:
  `/login`, `/css/**`, `/js/**`, `/images/**`, `/error` e o webhook do
  WhatsApp (validado por assinatura).
- Senhas sempre com BCrypt; nunca expostas em telas, logs ou respostas de API.
- CSRF habilitado (nao desativado globalmente).
- Sessao expira apos 30 minutos de inatividade; cookie `HttpOnly` (e seguro em
  producao via `SERVER_SERVLET_SESSION_COOKIE_SECURE=true`, quando atras de
  HTTPS).
- Este sistema armazena dados pessoais (CPF, telefone, endereco, data de
  nascimento, agenda e, se habilitado, mensagens). Recomendacoes gerais de
  LGPD: use apenas para a finalidade do atendimento do gabinete, restrinja o
  acesso a quem precisa, defina uma politica de retencao (o modulo de
  mensagens ja tem uma configuracao de retencao em dias), e nunca reutilize o
  banco de demonstracao com dados reais.

## Estrutura do projeto

```
cliente-manager-postgresql/
├── pom.xml
├── docker-compose.yml                 -> PostgreSQL local para desenvolvimento
├── PERSONALIZACAO.md, GUIA_*.md        -> documentacao
├── src/main/java/com/exemplo/clientemanager/
│   ├── ClienteManagerApplication.java
│   ├── config/                        -> AppProperties, WhatsappProperties, DemoDataLoader, GlobalModelAdvice
│   ├── model/                         -> Cliente, Servico, Usuario, Compromisso e enums
│   ├── repository/                    -> Spring Data JPA
│   ├── service/                       -> regras de negocio
│   ├── security/                      -> SecurityConfig, CustomUserDetailsService, AdminBootstrap
│   ├── controller/                    -> paginas HTML e API REST
│   ├── validation/                    -> validacao de CPF
│   └── whatsapp/                      -> model/repository/service/controller do modulo de mensagens
└── src/main/resources/
    ├── application.properties, application-demo.properties
    ├── templates/                     -> Thymeleaf (fragments, agenda, usuarios, mensagens, etc.)
    └── static/                        -> css e imagens (logo generico substituivel)
```

## Proximas melhorias possiveis

- Paginacao na listagem de cidadaos (para volumes grandes)
- Exportacao de relatorios em PDF/Excel
- Geracao de PDF da agenda diaria/semanal
- Historico de alteracoes (auditoria detalhada por campo)
- Internacionalizacao (caso um cliente precise de outro idioma)
