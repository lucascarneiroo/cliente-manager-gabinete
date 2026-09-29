# Gestão de Gabinete

Sistema web para gestão de gabinetes parlamentares (vereadores, deputados), permitindo o acompanhamento de cidadãos atendidos, atendimentos prestados, agenda de compromissos e comunicação via WhatsApp — tudo em uma plataforma própria, sem depender de planilhas soltas.

## O que o sistema faz

- **Cadastro de Cidadãos**: registro completo de pessoas atendidas pelo gabinete (dados pessoais, CPF validado, contato, endereço, histórico).
- **Gestão de Atendimentos**: registro de demandas e serviços prestados a cada cidadão, com status e acompanhamento.
- **Agenda de Compromissos**: módulo completo com visualização por dia, semana e mês, detecção automática de conflitos de horário, tipos e prioridades de compromissos.
- **Gestão de Usuários**: controle de quem acessa o sistema, com dois perfis de permissão (Administrador e Atendente), cada um com regras de acesso aplicadas tanto na tela quanto no backend.
- **Login seguro**: autenticação obrigatória para qualquer uso do sistema, com sessão protegida e senha criptografada.
- **Integração com WhatsApp (opcional)**: comunicação com cidadãos via WhatsApp Business Cloud API (oficial da Meta), com controle da janela de atendimento de 24h e validação de segurança no webhook. Desativada por padrão.
- **Modo demonstração**: perfil especial que carrega dados fictícios automaticamente, usado para apresentar o sistema a clientes em potencial sem expor dados reais.
- **Marca personalizável**: nome do sistema, cores, logotipo e textos configuráveis por variáveis de ambiente, sem precisar mexer em código — cada cliente pode ter sua própria identidade visual.

## Tecnologias utilizadas

| Categoria | Tecnologia |
|---|---|
| Linguagem | Java 17 |
| Framework | Spring Boot 3.3.4 |
| Persistência | Spring Data JPA / Hibernate |
| Banco de dados | PostgreSQL (produção) / H2 (testes) |
| Segurança | Spring Security 6 |
| Templates/Frontend | Thymeleaf + HTML/CSS |
| Build | Maven |
| Integração externa | WhatsApp Business Cloud API (Meta) |
| Testes | JUnit, Mockito, Spring Security Test |
| Deploy | Render (com PostgreSQL dedicado por cliente) |

## Arquitetura e principais decisões técnicas

- **Sem multi-tenancy**: cada cliente tem sua própria instância, seu próprio banco de dados e seu próprio domínio — isolamento total entre clientes.
- **Permissões no backend**: todas as regras de acesso (`ADMIN` / `ATENDENTE`) são aplicadas via `@PreAuthorize`, não apenas escondendo botões na tela.
- **Bootstrap seguro do primeiro administrador**: o usuário admin inicial é criado a partir de variáveis de ambiente na primeira execução; se essas variáveis não existirem, a aplicação falha ao iniciar (por segurança, para nunca subir sem controle de acesso).
- **Configuração centralizada**: toda a identidade visual e institucional do sistema (nome, logo, cores, textos) vem de `@ConfigurationProperties`, facilitando a personalização por cliente sem alterar código.
- **WhatsApp com API oficial apenas**: a integração usa exclusivamente a Cloud API oficial da Meta, com validação de assinatura HMAC no webhook — sem scraping, sem QR code, sem bibliotecas não oficiais.

## Como executar localmente

1. Tenha Java 17+ e Maven instalados.
2. Suba um PostgreSQL local e crie o banco `gabinete_db`.
3. Configure as variáveis de ambiente:

SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/gabinete_db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=sua_senha
APP_ADMIN_NOME=Administrador
APP_ADMIN_USUARIO=admin
APP_ADMIN_SENHA=uma_senha_forte

4. Rode `mvn spring-boot:run` (ou execute pela IDE).
5. Acesse `http://localhost:8080` e faça login com o usuário/senha definidos acima.

## Modo demonstração

Para rodar com dados fictícios (ideal para mostrar a clientes):
SPRING_PROFILES_ACTIVE=demo
Isso carrega cidadãos, atendimentos e compromissos de exemplo automaticamente, apenas se o banco estiver vazio.

## Documentação adicional

- `GUIA_POSTGRESQL.md` — configuração do banco de dados
- `PERSONALIZACAO.md` — como personalizar o sistema para cada cliente
- `GUIA_RENDER.md` — deploy em produção no Render
- `GUIA_AGENDA.md` — detalhes do módulo de agenda
- `GUIA_WHATSAPP.md` — configuração da integração com WhatsApp

## Licença

Uso comercial — todos os direitos reservados.
