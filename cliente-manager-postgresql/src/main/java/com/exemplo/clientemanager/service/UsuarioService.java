package com.exemplo.clientemanager.service;

import com.exemplo.clientemanager.model.PerfilUsuario;
import com.exemplo.clientemanager.model.Usuario;
import com.exemplo.clientemanager.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioService {

    public static final int TAMANHO_MINIMO_SENHA = 10;

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario nao encontrado: " + id));
    }

    public boolean existeAlgumUsuario() {
        return usuarioRepository.count() > 0;
    }

    /** Usado pelo AdminBootstrap e pelo DemoDataLoader para criar usuarios iniciais. */
    @Transactional
    public Usuario criar(String nomeCompleto, String nomeUsuario, String senha, PerfilUsuario perfil) {
        if (usuarioRepository.existsByNomeUsuarioIgnoreCase(nomeUsuario)) {
            throw new IllegalArgumentException("Ja existe um usuario com este nome de usuario");
        }
        validarSenha(senha);
        Usuario usuario = new Usuario();
        usuario.setNomeCompleto(nomeCompleto);
        usuario.setNomeUsuario(nomeUsuario);
        usuario.setSenhaHash(passwordEncoder.encode(senha));
        usuario.setPerfil(perfil);
        usuario.setAtivo(true);
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario atualizarDados(Long id, String nomeCompleto, PerfilUsuario novoPerfil) {
        Usuario usuario = buscarPorId(id);

        boolean estaRebaixando = usuario.getPerfil() == PerfilUsuario.ADMIN && novoPerfil != PerfilUsuario.ADMIN;
        if (estaRebaixando) {
            garantirQueNaoEUltimoAdminAtivo(usuario);
        }

        usuario.setNomeCompleto(nomeCompleto);
        usuario.setPerfil(novoPerfil);
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void redefinirSenha(Long id, String novaSenha, String confirmacao) {
        if (novaSenha == null || !novaSenha.equals(confirmacao)) {
            throw new IllegalArgumentException("A senha e a confirmacao nao coincidem");
        }
        validarSenha(novaSenha);
        Usuario usuario = buscarPorId(id);
        usuario.setSenhaHash(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void bloquear(Long id, String nomeUsuarioLogado) {
        Usuario usuario = buscarPorId(id);

        if (usuario.getNomeUsuario().equalsIgnoreCase(nomeUsuarioLogado)) {
            throw new IllegalArgumentException("Voce nao pode bloquear a propria conta durante a sessao");
        }
        if (usuario.getPerfil() == PerfilUsuario.ADMIN) {
            garantirQueNaoEUltimoAdminAtivo(usuario);
        }

        usuario.setAtivo(false);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void reativar(Long id) {
        Usuario usuario = buscarPorId(id);
        usuario.setAtivo(true);
        usuarioRepository.save(usuario);
    }

    private void garantirQueNaoEUltimoAdminAtivo(Usuario usuario) {
        long adminsAtivos = usuarioRepository.countByPerfilAndAtivoTrue(PerfilUsuario.ADMIN);
        boolean esteEUmAdminAtivo = usuario.getPerfil() == PerfilUsuario.ADMIN && usuario.isAtivo();
        if (esteEUmAdminAtivo && adminsAtivos <= 1) {
            throw new IllegalArgumentException("Nao e possivel bloquear ou rebaixar o ultimo administrador ativo");
        }
    }

    public void validarSenha(String senha) {
        if (senha == null || senha.length() < TAMANHO_MINIMO_SENHA) {
            throw new IllegalArgumentException("A senha deve ter pelo menos " + TAMANHO_MINIMO_SENHA + " caracteres");
        }
    }
}
