package com.exemplo.clientemanager.whatsapp.service;

import com.exemplo.clientemanager.model.Cliente;
import com.exemplo.clientemanager.model.Servico;
import com.exemplo.clientemanager.service.ClienteService;
import com.exemplo.clientemanager.service.ServicoService;
import com.exemplo.clientemanager.whatsapp.model.*;
import com.exemplo.clientemanager.whatsapp.repository.ConversaRepository;
import com.exemplo.clientemanager.whatsapp.repository.MensagemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Regras de negocio do modulo de mensagens. Funciona tanto para conversas
 * reais (integracao habilitada) quanto para a simulacao do perfil demo -
 * a diferenca fica a cargo de quem envia a mensagem ({@link WhatsappService}).
 */
@Service
public class ConversaService {

    private static final int JANELA_ATENDIMENTO_HORAS = 24;

    private final ConversaRepository conversaRepository;
    private final MensagemRepository mensagemRepository;
    private final WhatsappService whatsappService;
    private final ClienteService clienteService;
    private final ServicoService servicoService;

    public ConversaService(ConversaRepository conversaRepository,
                            MensagemRepository mensagemRepository,
                            WhatsappService whatsappService,
                            ClienteService clienteService,
                            ServicoService servicoService) {
        this.conversaRepository = conversaRepository;
        this.mensagemRepository = mensagemRepository;
        this.whatsappService = whatsappService;
        this.clienteService = clienteService;
        this.servicoService = servicoService;
    }

    public List<Conversa> listarTodas() {
        return conversaRepository.findAll();
    }

    public List<Conversa> buscar(String termo) {
        if (termo == null || termo.isBlank()) {
            return listarTodas();
        }
        return conversaRepository.findByNomeContatoContainingIgnoreCaseOrTelefoneContaining(termo, termo);
    }

    public Conversa buscarPorId(Long id) {
        return conversaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Conversa nao encontrada: " + id));
    }

    public List<Mensagem> historico(Long conversaId) {
        return mensagemRepository.findByConversaIdOrderByDataHoraAsc(conversaId);
    }

    @Transactional
    public void atribuir(Long conversaId, String nomeUsuario) {
        Conversa conversa = buscarPorId(conversaId);
        conversa.setAtribuidoA(nomeUsuario);
        conversa.setSituacao(SituacaoConversa.EM_ATENDIMENTO);
        conversaRepository.save(conversa);
    }

    @Transactional
    public void mudarSituacao(Long conversaId, SituacaoConversa situacao) {
        Conversa conversa = buscarPorId(conversaId);
        conversa.setSituacao(situacao);
        conversaRepository.save(conversa);
    }

    @Transactional
    public Cliente cadastrarCidadaoDaConversa(Long conversaId, String nome, String cpf, String endereco) {
        Conversa conversa = buscarPorId(conversaId);
        Cliente cliente = new Cliente();
        cliente.setNome(nome);
        cliente.setCpf(cpf);
        cliente.setTelefone(conversa.getTelefone());
        cliente.setEndereco(endereco);
        Cliente salvo = clienteService.salvar(cliente);
        conversa.setCidadao(salvo);
        conversaRepository.save(conversa);
        return salvo;
    }

    @Transactional
    public Servico registrarComoAtendimento(Long conversaId, String descricaoAtendimento) {
        Conversa conversa = buscarPorId(conversaId);
        if (conversa.getCidadao() == null) {
            throw new IllegalArgumentException("Associe a conversa a um cidadao antes de registrar o atendimento");
        }
        Servico servico = new Servico();
        servico.setDescricao(descricaoAtendimento);
        servico.setObservacoes("Atendimento originado de conversa de WhatsApp (" + conversa.getTelefone() + ")");
        return servicoService.salvar(conversa.getCidadao().getId(), servico);
    }

    /**
     * Registra a mensagem recebida do cidadao (usada pelo webhook real e
     * pela simulacao do perfil demo).
     */
    @Transactional
    public Mensagem receberMensagem(String telefone, String nomeContato, String texto, boolean simulada) {
        Conversa conversa = conversaRepository.findByTelefone(telefone).orElseGet(() -> {
            Conversa nova = new Conversa();
            nova.setTelefone(telefone);
            nova.setNomeContato(nomeContato);
            nova.setSimulada(simulada);
            return conversaRepository.save(nova);
        });

        Mensagem mensagem = new Mensagem();
        mensagem.setConversa(conversa);
        mensagem.setRemetente(RemetenteMensagem.CIDADAO);
        mensagem.setConteudo(texto);
        mensagem.setStatus(StatusMensagem.RECEBIDO);
        mensagem.setSimulada(simulada);
        return mensagemRepository.save(mensagem);
    }

    /**
     * Responde uma conversa com texto livre, respeitando a janela de 24h de
     * atendimento da WhatsApp Business Platform. Fora da janela, recusa o
     * envio e sinaliza que e necessario um modelo aprovado.
     */
    @Transactional
    public WhatsappService.ResultadoEnvio responderLivre(Long conversaId, String texto, String nomeUsuario, boolean simulada) {
        Conversa conversa = buscarPorId(conversaId);

        if (!simulada && !janelaAtendimentoAberta(conversaId)) {
            return WhatsappService.ResultadoEnvio.falha(
                "Mais de 24h desde a ultima mensagem do cidadao: use um modelo aprovado para retomar a conversa.");
        }

        WhatsappService.ResultadoEnvio resultado = simulada
                ? WhatsappService.ResultadoEnvio.ok()
                : whatsappService.enviarMensagemLivre(conversa.getTelefone(), texto);

        Mensagem mensagem = new Mensagem();
        mensagem.setConversa(conversa);
        mensagem.setRemetente(RemetenteMensagem.GABINETE);
        mensagem.setConteudo(texto);
        mensagem.setUsuarioResposta(nomeUsuario);
        mensagem.setSimulada(simulada);
        mensagem.setStatus(resultado.sucesso() ? StatusMensagem.ENVIADO : StatusMensagem.FALHOU);
        mensagemRepository.save(mensagem);

        return resultado;
    }

    private boolean janelaAtendimentoAberta(Long conversaId) {
        return historico(conversaId).stream()
                .filter(m -> m.getRemetente() == RemetenteMensagem.CIDADAO)
                .map(Mensagem::getDataHora)
                .max(LocalDateTime::compareTo)
                .map(ultima -> ultima.isAfter(LocalDateTime.now().minusHours(JANELA_ATENDIMENTO_HORAS)))
                .orElse(false);
    }
}
