# Personalizacao (PERSONALIZACAO.md)

Este sistema foi desenhado para que **nada de identidade fique escrito no
codigo ou nos templates**. Tudo que muda de um cliente para outro (nome,
logotipo, cores, subtitulo, rodape) vem de configuracao centralizada em
`AppProperties` (`src/main/java/.../config/AppProperties.java`), preenchida
por `application.properties` a partir de variaveis de ambiente.

## O que personalizar por cliente

| O que muda | Variavel de ambiente | Padrao |
|---|---|---|
| Nome do gabinete | `APP_NOME_GABINETE` | `Gabinete Parlamentar` |
| Nome do vereador (opcional) | `APP_NOME_VEREADOR` | vazio (nao exibido) |
| Subtitulo | `APP_SUBTITULO` | `Gestao de atendimentos` |
| Descricao (usada no painel inicial) | `APP_DESCRICAO` | texto generico |
| Logotipo | `APP_LOGO` | `/images/logo-placeholder.svg` |
| Cor primaria | `APP_COR_PRIMARIA` | `#173B67` |
| Cor secundaria | `APP_COR_SECUNDARIA` | `#2563A6` |
| Rodape | `APP_RODAPE` | `Sistema de Gestao de Gabinete` |

Nenhuma dessas variaveis e obrigatoria: se nao forem definidas, o sistema usa
os valores genericos acima (nome do sistema "Gestao de Gabinete", sem nome de
vereador, sem partido, sem numero eleitoral, sem slogan).

## Como trocar o nome e o subtitulo

Defina as variaveis de ambiente antes de subir a aplicacao (local, Docker ou
Render):

```bash
export APP_NOME_GABINETE="Gabinete do Vereador Exemplo"
export APP_NOME_VEREADOR="Fulano de Tal"
export APP_SUBTITULO="Atendimento ao cidadao"
```

O titulo exibido no topo do sistema combina os dois: "Gabinete do Vereador
Exemplo - Fulano de Tal" (o nome do vereador so aparece se for informado).

## Como trocar o logotipo

1. Coloque o arquivo de imagem em `src/main/resources/static/images/`
   (ex.: `logo-cliente.png`).
2. Defina `APP_LOGO=/images/logo-cliente.png`.
3. Reimplante a aplicacao.

Se `APP_LOGO` nao for definido, ou se o arquivo nao existir, a interface
mostra automaticamente um **monograma generico** (a primeira letra do nome do
gabinete, dentro de um circulo colorido) - nunca uma imagem quebrada.

Formatos recomendados: SVG ou PNG com fundo transparente, proporcao 1:1 (ex.:
200x200px).

## Como trocar as cores

```bash
export APP_COR_PRIMARIA="#0F3D2E"
export APP_COR_SECUNDARIA="#1E7A54"
```

As cores sao aplicadas a todo o sistema (cabecalho, botoes, destaques) por meio
de uma unica tag `<style>` injetada em cada pagina (fragmento `cores` em
`templates/fragments/layout.html`), que sobrescreve as variaveis CSS
`--navy`/`--blue`. Nenhum outro arquivo precisa ser tocado.

## Como trocar o rodape

```bash
export APP_RODAPE="Gabinete do Vereador Exemplo - Camara Municipal de Exemplo"
```

## Modo de demonstracao

Ative com `SPRING_PROFILES_ACTIVE=demo`. Nesse perfil:

- O nome exibido passa a ser "Gestao de Gabinete - Demonstracao".
- Uma faixa "Ambiente de demonstracao" aparece no topo de toda pagina
  (inclusive no login).
- Cidadaos, atendimentos, compromissos e uma conversa de WhatsApp ficticios
  sao criados automaticamente **apenas se o banco estiver vazio** (nao
  duplicam a cada reinicio).
- Um usuario de demonstracao (perfil ATENDENTE, sem acesso a "Usuarios") e
  criado a partir de `APP_DEMO_USUARIO` / `APP_DEMO_SENHA`, se informados.
- O modulo de mensagens funciona como uma **simulacao visual**: nenhuma
  chamada real e feita a Meta, mesmo que `WHATSAPP_HABILITADO=true` tenha
  sido definido por engano nesse ambiente.

**Nunca ative o perfil `demo` na instalacao real de um cliente**, e nunca
reaproveite o banco de dados de demonstracao para uma instalacao real.

### Restaurar os dados de demonstracao

Como os dados ficticios so sao recriados quando o banco esta vazio, para
"resetar" a demonstracao:

1. Pare a aplicacao.
2. Apague (ou recrie) o banco PostgreSQL de demonstracao - nunca o de um
   cliente real.
3. Suba a aplicacao novamente com `SPRING_PROFILES_ACTIVE=demo`: os dados
   ficticios serao recriados automaticamente.

## Criando a instalacao de um novo cliente (resumo)

1. Crie um novo Web Service e um novo PostgreSQL no Render, exclusivos para
   este cliente (ver [GUIA_RENDER.md](GUIA_RENDER.md)).
2. Configure as variaveis `APP_*` deste guia com a identidade do cliente.
3. Configure `APP_ADMIN_NOME` / `APP_ADMIN_USUARIO` / `APP_ADMIN_SENHA` para
   criar o primeiro administrador daquela instalacao.
4. **Nao** defina `SPRING_PROFILES_ACTIVE=demo` (deixe em branco) - isso e
   exclusivo do ambiente de demonstracao comercial.
5. Se o cliente for usar WhatsApp, configure as variaveis `WHATSAPP_*` com a
   conta comercial do proprio cliente (ver [GUIA_WHATSAPP.md](GUIA_WHATSAPP.md)).
6. Vincule o dominio/subdominio proprio do cliente.

Cada cliente tem banco, usuarios, agenda, identidade visual e (se aplicavel)
credenciais de WhatsApp completamente isolados dos demais.
