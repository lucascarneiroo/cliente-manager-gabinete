package com.exemplo.clientemanager.whatsapp.repository;

import com.exemplo.clientemanager.whatsapp.model.Conversa;
import com.exemplo.clientemanager.whatsapp.model.SituacaoConversa;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConversaRepository extends JpaRepository<Conversa, Long> {

    // O cidadao vinculado e carregado junto, pois as telas o exibem fora da
    // transacao (spring.jpa.open-in-view=false).
    @Override
    @EntityGraph(attributePaths = "cidadao")
    Optional<Conversa> findById(Long id);

    @Override
    @EntityGraph(attributePaths = "cidadao")
    List<Conversa> findAll();

    Optional<Conversa> findByTelefone(String telefone);

    List<Conversa> findBySituacao(SituacaoConversa situacao);

    @EntityGraph(attributePaths = "cidadao")
    List<Conversa> findByNomeContatoContainingIgnoreCaseOrTelefoneContaining(String nome, String telefone);
}
