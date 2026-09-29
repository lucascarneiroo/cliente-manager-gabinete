package com.exemplo.clientemanager.whatsapp.model;

import com.exemplo.clientemanager.model.Cliente;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Representa uma conversa de WhatsApp com um numero externo. So existe
 * conteudo real quando a integracao esta habilitada (app.whatsapp.habilitado=true);
 * no perfil demo, as conversas sao sempre ficticias e simuladas.
 */
@Entity
@Table(name = "whatsapp_conversas")
public class Conversa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String telefone;

    @Column(name = "nome_contato", length = 150)
    private String nomeContato;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cidadao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SituacaoConversa situacao = SituacaoConversa.ABERTA;

    @Column(name = "atribuido_a", length = 60)
    private String atribuidoA;

    @Column(name = "simulada", nullable = false)
    private boolean simulada = false;

    @Column(name = "criado_em", updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    @PrePersist
    protected void aoCriar() {
        criadoEm = LocalDateTime.now();
        atualizadoEm = LocalDateTime.now();
    }

    @PreUpdate
    protected void aoAtualizar() {
        atualizadoEm = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getNomeContato() { return nomeContato; }
    public void setNomeContato(String nomeContato) { this.nomeContato = nomeContato; }

    public Cliente getCidadao() { return cidadao; }
    public void setCidadao(Cliente cidadao) { this.cidadao = cidadao; }

    public SituacaoConversa getSituacao() { return situacao; }
    public void setSituacao(SituacaoConversa situacao) { this.situacao = situacao; }

    public String getAtribuidoA() { return atribuidoA; }
    public void setAtribuidoA(String atribuidoA) { this.atribuidoA = atribuidoA; }

    public boolean isSimulada() { return simulada; }
    public void setSimulada(boolean simulada) { this.simulada = simulada; }

    public LocalDateTime getCriadoEm() { return criadoEm; }
    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }
}
