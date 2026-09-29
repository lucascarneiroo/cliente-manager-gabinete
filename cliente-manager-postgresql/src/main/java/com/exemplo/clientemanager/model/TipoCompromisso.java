package com.exemplo.clientemanager.model;

public enum TipoCompromisso {
    REUNIAO("Reuniao"),
    SESSAO_LEGISLATIVA("Sessao legislativa"),
    VISITA("Visita"),
    AUDIENCIA("Audiencia"),
    EVENTO_PUBLICO("Evento publico"),
    ATENDIMENTO_CIDADAO("Atendimento ao cidadao"),
    VIAGEM("Viagem"),
    COMPROMISSO_INTERNO("Compromisso interno"),
    OUTRO("Outro");

    private final String descricao;

    TipoCompromisso(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
