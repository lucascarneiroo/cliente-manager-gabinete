package com.exemplo.clientemanager.service;

import com.exemplo.clientemanager.model.Compromisso;
import com.exemplo.clientemanager.model.SituacaoCompromisso;
import com.exemplo.clientemanager.repository.CompromissoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class CompromissoService {

    private final CompromissoRepository compromissoRepository;

    public CompromissoService(CompromissoRepository compromissoRepository) {
        this.compromissoRepository = compromissoRepository;
    }

    public Compromisso buscarPorId(Long id) {
        return compromissoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Compromisso nao encontrado: " + id));
    }

    public List<Compromisso> listarPorPeriodo(LocalDate inicio, LocalDate fim) {
        return compromissoRepository.findByDataBetweenOrderByDataAscHoraInicioAsc(inicio, fim);
    }

    public List<Compromisso> listarDoDia(LocalDate data) {
        return compromissoRepository.findByDataOrderByHoraInicioAsc(data);
    }

    public List<Compromisso> proximosCompromissos(LocalDate apartirDe) {
        return compromissoRepository.findByDataGreaterThanEqualAndSituacaoInOrderByDataAscHoraInicioAsc(
                apartirDe, List.of(SituacaoCompromisso.AGENDADO, SituacaoCompromisso.CONFIRMADO));
    }

    public List<Compromisso> pendentesDeConfirmacao() {
        return compromissoRepository.findBySituacaoOrderByDataAscHoraInicioAsc(SituacaoCompromisso.AGENDADO);
    }

    public List<Compromisso> buscar(String termo) {
        if (termo == null || termo.isBlank()) {
            return compromissoRepository.findAll();
        }
        return compromissoRepository
                .findByTituloContainingIgnoreCaseOrLocalContainingIgnoreCaseOrNomeResponsavelContainingIgnoreCase(
                        termo, termo, termo);
    }

    /**
     * Retorna os compromissos que conflitam de horario com o compromisso informado
     * (mesma data, horarios sobrepostos, situacao que ainda ocupa a agenda).
     * O id do proprio compromisso (em edicoes) e ignorado.
     */
    public List<Compromisso> detectarConflitos(Compromisso candidato) {
        return compromissoRepository.findByDataOrderByHoraInicioAsc(candidato.getData()).stream()
                .filter(existente -> candidato.getId() == null || !existente.getId().equals(candidato.getId()))
                .filter(existente -> existente.getSituacao().ocupaHorario())
                .filter(existente -> horariosSeSobrepoe(candidato, existente))
                .toList();
    }

    private boolean horariosSeSobrepoe(Compromisso a, Compromisso b) {
        return a.getInicioEfetivo().isBefore(b.getFimEfetivo()) && b.getInicioEfetivo().isBefore(a.getFimEfetivo());
    }

    @Transactional
    public Compromisso salvar(Compromisso compromisso, String usuarioLogado) {
        if (compromisso.getId() == null) {
            compromisso.setCriadoPor(usuarioLogado);
        }
        compromisso.setAtualizadoPor(usuarioLogado);
        return compromissoRepository.save(compromisso);
    }

    @Transactional
    public void mudarSituacao(Long id, SituacaoCompromisso novaSituacao, String usuarioLogado) {
        Compromisso compromisso = buscarPorId(id);
        compromisso.setSituacao(novaSituacao);
        compromisso.setAtualizadoPor(usuarioLogado);
        compromissoRepository.save(compromisso);
    }

    /** Cancelamento logico (preserva o historico). Disponivel para ADMIN e ATENDENTE. */
    @Transactional
    public void cancelar(Long id, String usuarioLogado) {
        mudarSituacao(id, SituacaoCompromisso.CANCELADO, usuarioLogado);
    }

    /** Exclusao definitiva. Deve ser restrita a ADMIN no controller. */
    @Transactional
    public void excluir(Long id) {
        compromissoRepository.deleteById(id);
    }
}
