package com.exemplo.clientemanager.model;

public enum SituacaoCompromisso {
    AGENDADO("Agendado"),
    CONFIRMADO("Confirmado"),
    REALIZADO("Realizado"),
    CANCELADO("Cancelado"),
    ADIADO("Adiado");

    private final String descricao;

    SituacaoCompromisso(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    /** Situacoes que ainda "ocupam" a agenda para fins de deteccao de conflito. */
    public boolean ocupaHorario() {
        return this == AGENDADO || this == CONFIRMADO;
    }
}
