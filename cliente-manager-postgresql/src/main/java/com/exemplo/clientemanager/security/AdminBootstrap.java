package com.exemplo.clientemanager.security;

import com.exemplo.clientemanager.config.AppProperties;
import com.exemplo.clientemanager.model.PerfilUsuario;
import com.exemplo.clientemanager.service.UsuarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Cria o primeiro administrador automaticamente, mas somente quando a
 * tabela "usuarios" ainda esta vazia (instalacao nova). Depois disso, a
 * criacao de usuarios passa a ser feita pela tela de gerenciamento.
 *
 * As credenciais nunca ficam no codigo/repositorio: vem das variaveis de
 * ambiente APP_ADMIN_NOME, APP_ADMIN_USUARIO e APP_ADMIN_SENHA.
 */
@Component
@Order(1)
public class AdminBootstrap implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    private final UsuarioService usuarioService;
    private final AppProperties appProperties;

    public AdminBootstrap(UsuarioService usuarioService, AppProperties appProperties) {
        this.usuarioService = usuarioService;
        this.appProperties = appProperties;
    }

    @Override
    public void run(String... args) {
        if (usuarioService.existeAlgumUsuario()) {
            return;
        }

        String nome = appProperties.getAdmin().getNome();
        String usuario = appProperties.getAdmin().getUsuario();
        String senha = appProperties.getAdmin().getSenha();

        if (nome == null || nome.isBlank() || usuario == null || usuario.isBlank()
                || senha == null || senha.isBlank()) {
            throw new IllegalStateException(
                "Nenhum usuario cadastrado e as variaveis de ambiente APP_ADMIN_NOME, "
                + "APP_ADMIN_USUARIO e APP_ADMIN_SENHA nao foram configuradas. "
                + "Defina essas variaveis e reinicie a aplicacao para criar o primeiro "
                + "administrador. A aplicacao nao sera iniciada sem um administrador inicial."
            );
        }

        usuarioService.criar(nome, usuario, senha, PerfilUsuario.ADMIN);
        // Nunca registrar a senha no log.
        log.info("Primeiro administrador criado com sucesso (usuario: {}). "
                + "Recomenda-se trocar a senha inicial apos o primeiro acesso.", usuario);
    }
}
