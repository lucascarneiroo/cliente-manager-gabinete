package com.exemplo.clientemanager.model;

public enum PrioridadeCompromisso {
    BAIXA("Baixa"),
    NORMAL("Normal"),
    ALTA("Alta"),
    URGENTE("Urgente");

    private final String descricao;

    PrioridadeCompromisso(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
