package com.exemplo.clientemanager.repository;

import com.exemplo.clientemanager.model.Compromisso;
import com.exemplo.clientemanager.model.SituacaoCompromisso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface CompromissoRepository extends JpaRepository<Compromisso, Long> {

    List<Compromisso> findByDataBetweenOrderByDataAscHoraInicioAsc(LocalDate inicio, LocalDate fim);

    List<Compromisso> findByDataOrderByHoraInicioAsc(LocalDate data);

    List<Compromisso> findByDataAndSituacaoNotOrderByHoraInicioAsc(LocalDate data, SituacaoCompromisso situacaoExcluida);

    List<Compromisso> findByDataGreaterThanEqualAndSituacaoInOrderByDataAscHoraInicioAsc(
            LocalDate apartirDe, List<SituacaoCompromisso> situacoes);

    List<Compromisso> findBySituacaoOrderByDataAscHoraInicioAsc(SituacaoCompromisso situacao);

    List<Compromisso> findByTituloContainingIgnoreCaseOrLocalContainingIgnoreCaseOrNomeResponsavelContainingIgnoreCase(
            String titulo, String local, String responsavel);
}
