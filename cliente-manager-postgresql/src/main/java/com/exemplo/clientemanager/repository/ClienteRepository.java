package com.exemplo.clientemanager.repository;

import com.exemplo.clientemanager.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByCpf(String cpf);

    // Busca por nome OU cpf contendo o termo digitado (usado na barra de busca)
    List<Cliente> findByNomeContainingIgnoreCaseOrCpfContainingOrTelefoneContaining(
            String nome, String cpf, String telefone);
}
