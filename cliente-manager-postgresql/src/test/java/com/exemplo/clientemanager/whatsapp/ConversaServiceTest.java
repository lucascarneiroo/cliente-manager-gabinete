package com.exemplo.clientemanager.whatsapp;

import com.exemplo.clientemanager.service.ClienteService;
import com.exemplo.clientemanager.service.ServicoService;
import com.exemplo.clientemanager.whatsapp.model.*;
import com.exemplo.clientemanager.whatsapp.repository.ConversaRepository;
import com.exemplo.clientemanager.whatsapp.repository.MensagemRepository;
import com.exemplo.clientemanager.whatsapp.service.ConversaService;
import com.exemplo.clientemanager.whatsapp.service.WhatsappService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ConversaServiceTest {

    private ConversaRepository conversaRepository;
    private MensagemRepository mensagemRepository;
    private WhatsappService whatsappService;
    private ConversaService conversaService;

    @BeforeEach
    void configurar() {
        conversaRepository = mock(ConversaRepository.class);
        mensagemRepository = mock(MensagemRepository.class);
        whatsappService = mock(WhatsappService.class);
        conversaService = new ConversaService(conversaRepository, mensagemRepository, whatsappService,
                mock(ClienteService.class), mock(ServicoService.class));

        when(conversaRepository.save(any(Conversa.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mensagemRepository.save(any(Mensagem.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private Conversa conversaComId(Long id) {
        Conversa c = new Conversa();
        c.setId(id);
        c.setTelefone("5581999990000");
        return c;
    }

    @Test
    void respostaSimuladaNuncaChamaOWhatsappServiceReal() {
        Conversa conversa = conversaComId(1L);
        when(conversaRepository.findById(1L)).thenReturn(Optional.of(conversa));
        when(mensagemRepository.findByConversaIdOrderByDataHoraAsc(1L)).thenReturn(List.of());

        WhatsappService.ResultadoEnvio resultado = conversaService.responderLivre(1L, "Ola (simulado)", "atendente.demo", true);

        assertTrue(resultado.sucesso());
        verify(whatsappService, never()).enviarMensagemLivre(any(), any());
    }

    @Test
    void recusaRespostaLivreForaDaJanelaDe24Horas() {
        Conversa conversa = conversaComId(2L);
        when(conversaRepository.findById(2L)).thenReturn(Optional.of(conversa));

        Mensagem mensagemAntiga = new Mensagem();
        mensagemAntiga.setRemetente(RemetenteMensagem.CIDADAO);
        mensagemAntiga.setDataHora(LocalDateTime.now().minusHours(30));
        when(mensagemRepository.findByConversaIdOrderByDataHoraAsc(2L)).thenReturn(List.of(mensagemAntiga));

        WhatsappService.ResultadoEnvio resultado = conversaService.responderLivre(2L, "Ola", "atendente", false);

        assertFalse(resultado.sucesso());
        verify(whatsappService, never()).enviarMensagemLivre(any(), any());
    }

    @Test
    void permiteRespostaLivreDentroDaJanelaDe24Horas() {
        Conversa conversa = conversaComId(3L);
        when(conversaRepository.findById(3L)).thenReturn(Optional.of(conversa));
        when(whatsappService.enviarMensagemLivre(any(), any())).thenReturn(WhatsappService.ResultadoEnvio.ok());

        Mensagem mensagemRecente = new Mensagem();
        mensagemRecente.setRemetente(RemetenteMensagem.CIDADAO);
        mensagemRecente.setDataHora(LocalDateTime.now().minusHours(2));
        when(mensagemRepository.findByConversaIdOrderByDataHoraAsc(3L)).thenReturn(List.of(mensagemRecente));

        WhatsappService.ResultadoEnvio resultado = conversaService.responderLivre(3L, "Ola", "atendente", false);

        assertTrue(resultado.sucesso());
        verify(whatsappService).enviarMensagemLivre("5581999990000", "Ola");
    }
}
