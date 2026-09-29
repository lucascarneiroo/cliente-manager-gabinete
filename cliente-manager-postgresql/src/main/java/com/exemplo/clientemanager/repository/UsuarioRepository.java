package com.exemplo.clientemanager.repository;

import com.exemplo.clientemanager.model.PerfilUsuario;
import com.exemplo.clientemanager.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByNomeUsuarioIgnoreCase(String nomeUsuario);

    boolean existsByNomeUsuarioIgnoreCase(String nomeUsuario);

    long countByPerfilAndAtivoTrue(PerfilUsuario perfil);
}
