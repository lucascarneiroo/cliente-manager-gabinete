package com.exemplo.clientemanager.whatsapp.repository;

import com.exemplo.clientemanager.whatsapp.model.Conversa;
import com.exemplo.clientemanager.whatsapp.model.SituacaoConversa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConversaRepository extends JpaRepository<Conversa, Long> {

    Optional<Conversa> findByTelefone(String telefone);

    List<Conversa> findBySituacao(SituacaoConversa situacao);

    List<Conversa> findByNomeContatoContainingIgnoreCaseOrTelefoneContaining(String nome, String telefone);
}
