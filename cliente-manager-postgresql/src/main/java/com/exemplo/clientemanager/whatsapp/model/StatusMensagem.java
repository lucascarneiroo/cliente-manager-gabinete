package com.exemplo.clientemanager.whatsapp.model;

public enum StatusMensagem {
    ENVIADO("Enviado"),
    ENTREGUE("Entregue"),
    LIDO("Lido"),
    FALHOU("Falhou"),
    RECEBIDO("Recebido");

    private final String descricao;

    StatusMensagem(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
