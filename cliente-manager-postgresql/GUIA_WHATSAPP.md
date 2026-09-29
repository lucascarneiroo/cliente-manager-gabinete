# Guia do WhatsApp (GUIA_WHATSAPP.md)

A integracao com o WhatsApp e **opcional e desabilitada por padrao**. Usa
exclusivamente a **WhatsApp Business Cloud API oficial da Meta**. Este
sistema **nao** usa e **nao vai usar** automacao de navegador, leitura de QR
Code por biblioteca nao oficial, Selenium, Baileys, Venom, WPPConnect ou
qualquer metodo que possa violar as regras do WhatsApp ou bloquear o numero.

## Antes de comecar

Cada gabinete/cliente precisa ter, com sua **propria** conta:

- Uma conta comercial (Business Manager) da Meta
- Uma conta do WhatsApp Business Platform
- Um numero de telefone habilitado na Cloud API
- Um app configurado no Meta for Developers, com o produto "WhatsApp"

Nada disso e compartilhado entre clientes: cada instalacao usa suas proprias
credenciais, seu proprio numero e seu proprio banco de mensagens.

## Variaveis de ambiente

```
WHATSAPP_HABILITADO=true
WHATSAPP_ACCESS_TOKEN=<token de acesso da conta>
WHATSAPP_PHONE_NUMBER_ID=<ID do numero de telefone>
WHATSAPP_BUSINESS_ACCOUNT_ID=<ID da conta comercial>
WHATSAPP_WEBHOOK_VERIFY_TOKEN=<token que voce define, usado na verificacao do webhook>
WHATSAPP_APP_SECRET=<app secret do app na Meta, usado para validar a assinatura do webhook>
WHATSAPP_API_VERSION=v20.0
```

Nunca coloque esses valores no codigo, no `application.properties` versionado,
no banco de dados ou no GitHub. Configure-os apenas como variaveis de ambiente
do Web Service (no Render: **Environment**).

## Configurar o webhook usando o dominio do Render

1. No painel do app na Meta, va em **WhatsApp > Configuration > Webhook**.
2. Informe a URL de callback:
   ```
   https://<seu-dominio-no-render>/webhook/whatsapp
   ```
3. Informe o **Verify Token** com o mesmo valor de `WHATSAPP_WEBHOOK_VERIFY_TOKEN`.
4. A Meta faz uma requisicao `GET` de verificacao; o endpoint
   (`WhatsappWebhookController`) confirma o token e devolve o `hub.challenge`
   recebido.
5. Assine (subscribe) o campo `messages` para comecar a receber mensagens.

Cada notificacao recebida (`POST`) e validada com a assinatura
`X-Hub-Signature-256`, calculada com o `WHATSAPP_APP_SECRET`. Requisicoes sem
assinatura valida sao rejeitadas (HTTP 401) e nao sao processadas.

## O que fica disponivel quando habilitado

Um item de menu **"WhatsApp"** aparece para usuarios autenticados, com:

- Lista de conversas (por telefone/nome, com situacao: Aberta, Em atendimento,
  Finalizada)
- Historico de mensagens de cada conversa
- Resposta a mensagens (respeitando a janela de 24h - veja abaixo)
- Associar a conversa a um cidadao ja cadastrado, ou cadastrar um novo cidadao
  a partir dos dados basicos da conversa
- Registrar a conversa como um atendimento
- Atribuir a conversa a um usuario e marcar a situacao

## Janela de atendimento (24h) e modelos aprovados

A Meta so permite respostas de texto livre dentro de 24h da ultima mensagem
recebida do cidadao. Fora dessa janela, o sistema **recusa o envio livre** e
informa que e necessario um modelo (template) previamente aprovado pela Meta
(`WhatsappService.enviarModeloAprovado`). Isso e verificado automaticamente
pelo `ConversaService` antes de cada envio.

## O que permanece desabilitado por padrao

Com `WHATSAPP_HABILITADO=false` (padrao):

- Nenhuma chamada e feita a API da Meta.
- Nenhum erro de conexao e mostrado ao usuario.
- O item de menu "Mensagens" so aparece se o perfil `demo` estiver ativo
  (mostrando a simulacao) - caso contrario fica oculto.
- Se alguem acessar `/mensagens` diretamente, ve uma mensagem neutra
  informando que a integracao nao esta configurada; apenas administradores
  veem instrucoes de configuracao.

## Simulacao no perfil de demonstracao

No perfil `demo`, o modulo de mensagens sempre funciona em modo simulado
("Simulacao de atendimento"), independente de `WHATSAPP_HABILITADO`:

- Usa nomes e telefones ficticios.
- Nunca chama a API da Meta.
- Permite demonstrar a caixa de entrada, abrir conversas, responder
  (simuladamente), associar a um cidadao ficticio e registrar um atendimento
  ficticio.
- Exibe claramente o aviso **"Nenhuma mensagem real sera enviada"** em toda
  tela do modulo.

## Regras de envio respeitadas por design

- Sem disparo em massa.
- Sem propaganda eleitoral.
- Sem contorno da janela de atendimento de 24h.
- Tokens nunca aparecem em telas ou logs (apenas mensagens de erro genericas
  sao registradas).
- Retencao de mensagens configuravel (`app.whatsapp.retencao-dias-mensagens`,
  365 dias por padrao) - a exclusao automatica por prazo nao esta implementada
  nesta versao; a configuracao existe para orientar uma rotina de limpeza que
  o cliente pode agendar (ex.: um job periodico) conforme sua politica de
  retencao.

## Limitacoes desta entrega

A classe `WhatsappCloudApiService` segue a documentacao oficial da Cloud API
da Meta (endpoint `https://graph.facebook.com/{versao}/{phone_number_id}/messages`,
autenticacao Bearer, corpo JSON para mensagens de texto e de template), mas
**nao pode ser testada de ponta a ponta** sem uma conta comercial da Meta
aprovada e um numero de telefone habilitado - o que depende de cada cliente.

O que ja esta pronto e nao depende da conta da Meta:

- Estrutura modular completa (entidades, repositorios, services, controller)
- Simulacao funcional (perfil demo)
- Configuracao via variaveis de ambiente
- Webhook preparado, com validacao de assinatura e verificacao
- Logica de janela de 24h
- Testes automatizados com respostas simuladas (mockando a chamada real)

O que depende exclusivamente da conta comercial da Meta do cliente:

- Aprovar o numero de telefone e a conta comercial
- Gerar o `WHATSAPP_ACCESS_TOKEN`, `WHATSAPP_PHONE_NUMBER_ID` e
  `WHATSAPP_BUSINESS_ACCOUNT_ID`
- Aprovar os modelos (templates) de mensagem que serao usados fora da janela
  de 24h
- Testar o envio e o recebimento reais antes de liberar aos atendentes
