# Guia do Render (GUIA_RENDER.md)

Cada cliente (gabinete) recebe sua propria instalacao: um Web Service e um
PostgreSQL exclusivos no Render. Este guia cobre a implantacao de **uma**
instalacao - repita para cada cliente.

## 1. Criar o PostgreSQL

1. No painel do Render, clique em **New > PostgreSQL**.
2. De um nome que identifique o cliente (ex.: `gabinete-fulano-db`).
3. Escolha a regiao mais proxima do publico do gabinete.
4. Apos criado, anote a **Internal Database URL** (para o Web Service na
   mesma regiao/rede do Render) e as credenciais (usuario, senha, host,
   porta, nome do banco).

## 2. Criar o Web Service

1. **New > Web Service** e conecte o repositorio do GitHub com este projeto
   (um repositorio/branch por cliente, ou o mesmo repositorio generico com
   variaveis de ambiente diferentes por servico).
2. Ambiente: **Java**.
3. Comando de build:
   ```
   mvn clean package -DskipTests
   ```
   (os testes rodam separadamente em CI/local; no Render, priorize um build
   rapido. Se preferir rodar os testes no proprio build, remova
   `-DskipTests` - isso exige que o ambiente de build tenha acesso ao Maven
   Central.)
4. Comando de start:
   ```
   java -jar target/cliente-manager-1.0.0.jar
   ```

## 3. Configurar variaveis de ambiente

No painel do Web Service, em **Environment**, adicione:

```
SPRING_DATASOURCE_URL=<Internal Database URL do PostgreSQL, no formato jdbc:postgresql://...>
SPRING_DATASOURCE_USERNAME=<usuario do banco>
SPRING_DATASOURCE_PASSWORD=<senha do banco>

APP_ADMIN_NOME=<nome do primeiro administrador>
APP_ADMIN_USUARIO=<usuario do primeiro administrador>
APP_ADMIN_SENHA=<senha forte, min. 10 caracteres>

APP_NOME_GABINETE=<nome do gabinete do cliente>
APP_NOME_VEREADOR=<nome do vereador, se aplicavel>
APP_SUBTITULO=<subtitulo>
APP_LOGO=/images/<arquivo do logotipo, se personalizado>
APP_COR_PRIMARIA=<hex>
APP_COR_SECUNDARIA=<hex>
APP_RODAPE=<texto do rodape>

SERVER_SERVLET_SESSION_COOKIE_SECURE=true
```

> O Render fornece `PORT` automaticamente; nao e preciso defini-la.

**Nunca** cole a URL do PostgreSQL com a senha embutida diretamente em
`SPRING_DATASOURCE_URL` se puder evitar - prefira separar usuario e senha
sempre que o formato do Render permitir. Nunca commit essas variaveis no
GitHub.

## 4. Ativar a demonstracao (somente na instalacao de demonstracao comercial)

Apenas no Web Service **usado para demonstrar o sistema a clientes**, adicione:

```
SPRING_PROFILES_ACTIVE=demo
APP_DEMO_USUARIO=<usuario de demonstracao>
APP_DEMO_SENHA=<senha de demonstracao>
```

Nunca defina `SPRING_PROFILES_ACTIVE=demo` na instalacao real de um cliente.

## 5. Configurar uma instalacao real de cliente

Repita os passos 1-3 com um novo PostgreSQL e um novo Web Service, usando as
credenciais e a identidade daquele cliente especifico. Nao defina
`SPRING_PROFILES_ACTIVE` (ou defina como vazio/`default`).

## 6. Vincular o dominio

No Web Service, va em **Settings > Custom Domains** e adicione o dominio ou
subdominio do cliente (ex.: `gabinete.exemplo.com.br`), seguindo as
instrucoes de DNS exibidas pelo Render (registro CNAME ou A, conforme o
caso). O Render emite o certificado HTTPS automaticamente.

## 7. Primeiro login

1. Acesse a URL do Web Service (ou o dominio vinculado).
2. Entre com o `APP_ADMIN_USUARIO` / `APP_ADMIN_SENHA` configurados no passo 3.
3. Va em **Usuarios** e troque a propria senha (crie um novo usuario para si
   mesmo com uma senha pessoal, se preferir manter a conta `admin` gerada
   apenas como backup) - nao ha "esqueci minha senha": a redefinicao e sempre
   feita por outro administrador.

## 8. Personalizar o gabinete

Ver [PERSONALIZACAO.md](PERSONALIZACAO.md) - basta ajustar as variaveis `APP_*`
e reiniciar o servico (o Render reinicia automaticamente apos salvar as
variaveis de ambiente).

## 9. Configurar a agenda

Nenhuma configuracao adicional e necessaria - a agenda fica disponivel assim
que o primeiro usuario faz login. Ver [GUIA_AGENDA.md](GUIA_AGENDA.md).

## 10. Configurar a integracao oficial com a Meta (opcional)

Ver [GUIA_WHATSAPP.md](GUIA_WHATSAPP.md), incluindo como apontar o webhook
para o dominio do Render.

## 11. Desabilitar totalmente o WhatsApp

Nao defina nenhuma variavel `WHATSAPP_*`, ou defina explicitamente:

```
WHATSAPP_HABILITADO=false
```

O item de menu "Mensagens" so aparece quando `WHATSAPP_HABILITADO=true` ou
quando o perfil `demo` esta ativo (mostrando a simulacao).
