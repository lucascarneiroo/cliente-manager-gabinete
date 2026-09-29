package com.exemplo.clientemanager.repository;

import com.exemplo.clientemanager.model.Servico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServicoRepository extends JpaRepository<Servico, Long> {
    List<Servico> findByClienteIdOrderByDataServicoDescIdDesc(Long clienteId);
    void deleteByClienteId(Long clienteId);
}
