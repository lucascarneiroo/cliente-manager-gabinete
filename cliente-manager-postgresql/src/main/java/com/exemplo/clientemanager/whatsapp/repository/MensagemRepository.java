package com.exemplo.clientemanager.whatsapp.repository;

import com.exemplo.clientemanager.whatsapp.model.Mensagem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MensagemRepository extends JpaRepository<Mensagem, Long> {
    List<Mensagem> findByConversaIdOrderByDataHoraAsc(Long conversaId);
}
