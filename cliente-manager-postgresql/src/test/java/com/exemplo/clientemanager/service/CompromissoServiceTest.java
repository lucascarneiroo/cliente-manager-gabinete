package com.exemplo.clientemanager.service;

import com.exemplo.clientemanager.model.Compromisso;
import com.exemplo.clientemanager.model.SituacaoCompromisso;
import com.exemplo.clientemanager.repository.CompromissoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CompromissoServiceTest {

    private CompromissoRepository compromissoRepository;
    private CompromissoService compromissoService;

    @BeforeEach
    void configurar() {
        compromissoRepository = mock(CompromissoRepository.class);
        compromissoService = new CompromissoService(compromissoRepository);
    }

    private Compromisso compromisso(Long id, LocalDate data, LocalTime inicio, LocalTime fim, SituacaoCompromisso situacao) {
        Compromisso c = new Compromisso();
        c.setId(id);
        c.setTitulo("Compromisso " + id);
        c.setData(data);
        c.setHoraInicio(inicio);
        c.setHoraFim(fim);
        c.setSituacao(situacao);
        return c;
    }

    @Test
    void detectaConflitoQuandoHorariosSeSobrepoem() {
        LocalDate hoje = LocalDate.now();
        Compromisso existente = compromisso(1L, hoje, LocalTime.of(9, 0), LocalTime.of(10, 0), SituacaoCompromisso.CONFIRMADO);
        when(compromissoRepository.findByDataOrderByHoraInicioAsc(hoje)).thenReturn(List.of(existente));

        Compromisso novo = compromisso(null, hoje, LocalTime.of(9, 30), LocalTime.of(10, 30), SituacaoCompromisso.AGENDADO);

        List<Compromisso> conflitos = compromissoService.detectarConflitos(novo);
        assertEquals(1, conflitos.size());
    }

    @Test
    void naoDetectaConflitoQuandoHorariosNaoSeSobrepoem() {
        LocalDate hoje = LocalDate.now();
        Compromisso existente = compromisso(1L, hoje, LocalTime.of(9, 0), LocalTime.of(10, 0), SituacaoCompromisso.CONFIRMADO);
        when(compromissoRepository.findByDataOrderByHoraInicioAsc(hoje)).thenReturn(List.of(existente));

        Compromisso novo = compromisso(null, hoje, LocalTime.of(10, 0), LocalTime.of(11, 0), SituacaoCompromisso.AGENDADO);

        assertTrue(compromissoService.detectarConflitos(novo).isEmpty());
    }

    @Test
    void ignoraOProprioCompromissoAoEditar() {
        LocalDate hoje = LocalDate.now();
        Compromisso existente = compromisso(5L, hoje, LocalTime.of(9, 0), LocalTime.of(10, 0), SituacaoCompromisso.CONFIRMADO);
        when(compromissoRepository.findByDataOrderByHoraInicioAsc(hoje)).thenReturn(List.of(existente));

        Compromisso editado = compromisso(5L, hoje, LocalTime.of(9, 0), LocalTime.of(10, 0), SituacaoCompromisso.CONFIRMADO);

        assertTrue(compromissoService.detectarConflitos(editado).isEmpty());
    }

    @Test
    void naoConsideraConflitoComCompromissoCancelado() {
        LocalDate hoje = LocalDate.now();
        Compromisso cancelado = compromisso(1L, hoje, LocalTime.of(9, 0), LocalTime.of(10, 0), SituacaoCompromisso.CANCELADO);
        when(compromissoRepository.findByDataOrderByHoraInicioAsc(hoje)).thenReturn(List.of(cancelado));

        Compromisso novo = compromisso(null, hoje, LocalTime.of(9, 30), LocalTime.of(10, 30), SituacaoCompromisso.AGENDADO);

        assertTrue(compromissoService.detectarConflitos(novo).isEmpty());
    }

    @Test
    void excluirDelegaAoRepository() {
        compromissoService.excluir(9L);
        org.mockito.Mockito.verify(compromissoRepository).deleteById(9L);
    }
}
