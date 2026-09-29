package com.exemplo.clientemanager.security;

import com.exemplo.clientemanager.model.Usuario;
import com.exemplo.clientemanager.repository.UsuarioRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String nomeUsuario) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByNomeUsuarioIgnoreCase(nomeUsuario)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario ou senha invalidos"));

        return User.builder()
                .username(usuario.getNomeUsuario())
                .password(usuario.getSenhaHash())
                .disabled(!usuario.isAtivo())
                .authorities(new SimpleGrantedAuthority("ROLE_" + usuario.getPerfil().name()))
                .build();
    }
}
