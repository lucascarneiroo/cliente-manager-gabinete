package com.exemplo.clientemanager.service;

import com.exemplo.clientemanager.model.PerfilUsuario;
import com.exemplo.clientemanager.model.Usuario;
import com.exemplo.clientemanager.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UsuarioServiceTest {

    private UsuarioRepository usuarioRepository;
    private PasswordEncoder passwordEncoder;
    private UsuarioService usuarioService;

    @BeforeEach
    void configurar() {
        usuarioRepository = mock(UsuarioRepository.class);
        passwordEncoder = new BCryptPasswordEncoder();
        usuarioService = new UsuarioService(usuarioRepository, passwordEncoder);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private Usuario criarUsuario(Long id, String nomeUsuario, PerfilUsuario perfil, boolean ativo) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNomeCompleto("Fulano de Tal");
        usuario.setNomeUsuario(nomeUsuario);
        usuario.setSenhaHash(passwordEncoder.encode("SenhaForte123"));
        usuario.setPerfil(perfil);
        usuario.setAtivo(ativo);
        return usuario;
    }

    @Test
    void naoPermiteSenhaComMenosDeDezCaracteres() {
        when(usuarioRepository.existsByNomeUsuarioIgnoreCase("novo.usuario")).thenReturn(false);
        assertThrows(IllegalArgumentException.class,
                () -> usuarioService.criar("Novo Usuario", "novo.usuario", "curta1", PerfilUsuario.ATENDENTE));
    }

    @Test
    void permiteSenhaComDezOuMaisCaracteres() {
        when(usuarioRepository.existsByNomeUsuarioIgnoreCase("novo.usuario")).thenReturn(false);
        Usuario criado = usuarioService.criar("Novo Usuario", "novo.usuario", "SenhaForte123", PerfilUsuario.ATENDENTE);
        assertNotNull(criado.getSenhaHash());
        assertNotEquals("SenhaForte123", criado.getSenhaHash());
    }

    @Test
    void naoPermiteBloquearOUltimoAdministradorAtivo() {
        Usuario admin = criarUsuario(1L, "admin", PerfilUsuario.ADMIN, true);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(usuarioRepository.countByPerfilAndAtivoTrue(PerfilUsuario.ADMIN)).thenReturn(1L);

        assertThrows(IllegalArgumentException.class, () -> usuarioService.bloquear(1L, "outro.usuario"));
    }

    @Test
    void permiteBloquearAdminQuandoExisteOutroAdminAtivo() {
        Usuario admin = criarUsuario(1L, "admin", PerfilUsuario.ADMIN, true);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(usuarioRepository.countByPerfilAndAtivoTrue(PerfilUsuario.ADMIN)).thenReturn(2L);

        assertDoesNotThrow(() -> usuarioService.bloquear(1L, "outro.usuario"));
        assertFalse(admin.isAtivo());
    }

    @Test
    void naoPermiteBloquearAPropriaContaNaSessaoAtual() {
        Usuario admin = criarUsuario(1L, "admin", PerfilUsuario.ADMIN, true);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin));

        assertThrows(IllegalArgumentException.class, () -> usuarioService.bloquear(1L, "admin"));
        Mockito.verify(usuarioRepository, never()).save(any());
    }

    @Test
    void naoPermiteRebaixarOUltimoAdministradorAtivo() {
        Usuario admin = criarUsuario(1L, "admin", PerfilUsuario.ADMIN, true);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(usuarioRepository.countByPerfilAndAtivoTrue(PerfilUsuario.ADMIN)).thenReturn(1L);

        assertThrows(IllegalArgumentException.class,
                () -> usuarioService.atualizarDados(1L, "Admin Renomeado", PerfilUsuario.ATENDENTE));
    }

    @Test
    void redefinirSenhaExigeConfirmacaoIgual() {
        Usuario usuario = criarUsuario(2L, "atendente", PerfilUsuario.ATENDENTE, true);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario));

        assertThrows(IllegalArgumentException.class,
                () -> usuarioService.redefinirSenha(2L, "SenhaForte123", "SenhaDiferente1"));
    }
}
