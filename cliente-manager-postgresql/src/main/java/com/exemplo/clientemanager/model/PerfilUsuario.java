package com.exemplo.clientemanager.model;

/**
 * Perfis de acesso do sistema. Usado tanto na autorizacao do Spring Security
 * (com o prefixo ROLE_) quanto nas regras de negocio dos services/controllers.
 */
public enum PerfilUsuario {

    ADMIN("Administrador"),
    ATENDENTE("Atendente");

    private final String descricao;

    PerfilUsuario(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
