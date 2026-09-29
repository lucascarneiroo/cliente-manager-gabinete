package com.exemplo.clientemanager.whatsapp.model;

public enum SituacaoConversa {
    ABERTA("Aberta"),
    EM_ATENDIMENTO("Em atendimento"),
    FINALIZADA("Finalizada");

    private final String descricao;

    SituacaoConversa(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
