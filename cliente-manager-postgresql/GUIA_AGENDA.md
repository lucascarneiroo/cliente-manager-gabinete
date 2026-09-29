# Guia da Agenda (GUIA_AGENDA.md)

A Agenda organiza os compromissos do vereador/gabinete: reunioes, sessoes,
visitas, audiencias, eventos, atendimentos agendados, viagens e compromissos
internos.

## Onde encontrar

Menu principal > **Agenda**. A pagina inicial tambem mostra um resumo dos
compromissos de hoje, o proximo compromisso e os pendentes de confirmacao.

## Visualizacoes

- **Dia**: todos os compromissos de uma data especifica.
- **Semana**: de segunda a domingo, a partir da data de referencia.
- **Mes**: todos os compromissos do mes.
- Use as setas "Anterior" / "Proximo" para navegar, ou a busca por titulo,
  local ou responsavel.

## Campos de um compromisso

Titulo, descricao, data, horario de inicio/termino (ou "dia inteiro"), local,
tipo, situacao, prioridade, observacoes internas, nome e telefone do
responsavel, cidadao relacionado (opcional), alem de quem criou/alterou e
quando (auditoria automatica).

### Tipos

Reuniao, Sessao legislativa, Visita, Audiencia, Evento publico, Atendimento ao
cidadao, Viagem, Compromisso interno, Outro.

### Situacoes

Agendado, Confirmado, Realizado, Cancelado, Adiado.

### Prioridades

Baixa, Normal, Alta, Urgente.

## Criar um compromisso

1. Clique em **+ Novo compromisso** (na Agenda ou na pagina inicial).
2. Preencha os campos. Se marcar "dia inteiro", os horarios ficam opcionais.
3. Ao salvar, o sistema verifica automaticamente se ha **conflito de
   horario** com outro compromisso do mesmo dia (mesma faixa de horario, e
   ambos ainda "ocupando a agenda", ou seja, com situacao Agendado ou
   Confirmado).
4. Se houver conflito, o formulario reaparece com um aviso listando os
   compromissos conflitantes. Ajuste o horario, ou clique em **"Salvar mesmo
   assim"** para confirmar a sobreposicao intencionalmente.

## Mudar a situacao de um compromisso

Na pagina de detalhes de um compromisso, use os botoes **Confirmar**,
**Marcar como realizado**, **Adiar** ou **Cancelar**. O cancelamento e sempre
logico (o registro e mantido, apenas muda de situacao) para preservar o
historico.

## Exclusao definitiva

Somente administradores podem excluir um compromisso definitivamente (o botao
so aparece para o perfil ADMIN). Atendentes podem cancelar, mas nao excluir.

## Permissoes

| Acao | ADMIN | ATENDENTE |
|---|---|---|
| Criar / consultar / editar | sim | sim |
| Confirmar / marcar como realizado / adiar | sim | sim |
| Cancelar (exclusao logica) | sim | sim |
| Excluir definitivamente | sim | nao |
| Consultar quem criou/alterou cada compromisso | sim | sim (exibido na propria pagina de detalhes) |

## Associar a um cidadao

No formulario, o campo "Cidadao relacionado" permite vincular o compromisso a
um cidadao ja cadastrado (por exemplo, um atendimento agendado). O vinculo
aparece na pagina de detalhes do compromisso, com link direto para o
cadastro do cidadao.

## Impressao / PDF

A geracao de PDF da agenda diaria/semanal **nao foi incluida nesta versao**
por nao ter sido possivel implementa-la e testa-la de forma estavel no tempo
disponivel. Como alternativa imediata, a visualizacao "Dia" e "Semana" e
limpa o suficiente para impressao direta do navegador (Ctrl+P / Cmd+P). Se
esse recurso for prioritario para algum cliente, ele pode ser adicionado
depois com uma biblioteca de geracao de PDF (ex.: OpenPDF), seguindo o mesmo
padrao do restante do sistema.
