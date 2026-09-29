package com.exemplo.clientemanager.whatsapp.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "whatsapp_mensagens")
public class Mensagem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversa_id", nullable = false)
    private Conversa conversa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RemetenteMensagem remetente;

    @Column(nullable = false, length = 4000)
    private String conteudo;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusMensagem status = StatusMensagem.RECEBIDO;

    /** Nome de usuario do atendente que respondeu, quando aplicavel. */
    @Column(name = "usuario_resposta", length = 60)
    private String usuarioResposta;

    /** Marca mensagens ficticias criadas pela simulacao do perfil demo. */
    @Column(name = "simulada", nullable = false)
    private boolean simulada = false;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Conversa getConversa() { return conversa; }
    public void setConversa(Conversa conversa) { this.conversa = conversa; }

    public RemetenteMensagem getRemetente() { return remetente; }
    public void setRemetente(RemetenteMensagem remetente) { this.remetente = remetente; }

    public String getConteudo() { return conteudo; }
    public void setConteudo(String conteudo) { this.conteudo = conteudo; }

    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }

    public StatusMensagem getStatus() { return status; }
    public void setStatus(StatusMensagem status) { this.status = status; }

    public String getUsuarioResposta() { return usuarioResposta; }
    public void setUsuarioResposta(String usuarioResposta) { this.usuarioResposta = usuarioResposta; }

    public boolean isSimulada() { return simulada; }
    public void setSimulada(boolean simulada) { this.simulada = simulada; }
}
