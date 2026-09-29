package com.exemplo.clientemanager.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "compromissos")
public class Compromisso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Titulo e obrigatorio")
    @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String titulo;

    @Size(max = 2000)
    @Column(length = 2000)
    private String descricao;

    @NotNull(message = "Data e obrigatoria")
    @Column(nullable = false)
    private LocalDate data;

    @Column(name = "hora_inicio")
    private LocalTime horaInicio;

    @Column(name = "hora_fim")
    private LocalTime horaFim;

    @Column(name = "dia_inteiro", nullable = false)
    private boolean diaInteiro = false;

    @Size(max = 200)
    @Column(length = 200)
    private String local;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoCompromisso tipo = TipoCompromisso.OUTRO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SituacaoCompromisso situacao = SituacaoCompromisso.AGENDADO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PrioridadeCompromisso prioridade = PrioridadeCompromisso.NORMAL;

    @Size(max = 2000)
    @Column(name = "observacoes_internas", length = 2000)
    private String observacoesInternas;

    @Size(max = 150)
    @Column(name = "nome_responsavel", length = 150)
    private String nomeResponsavel;

    @Size(max = 20)
    @Column(name = "telefone_contato", length = 20)
    private String telefoneContato;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cidadao;

    @Column(name = "criado_por", length = 60)
    private String criadoPor;

    @Column(name = "criado_em", updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_por", length = 60)
    private String atualizadoPor;

    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    @Column(name = "aviso_whatsapp_enviado")
    private boolean avisoWhatsappEnviado = false;

    @PrePersist
    protected void aoCriar() {
        criadoEm = LocalDateTime.now();
        atualizadoEm = LocalDateTime.now();
    }

    @PreUpdate
    protected void aoAtualizar() {
        atualizadoEm = LocalDateTime.now();
    }

    /** Considera dia inteiro como ocupando das 00:00 as 23:59 para deteccao de conflito. */
    public LocalTime getInicioEfetivo() {
        return diaInteiro || horaInicio == null ? LocalTime.MIN : horaInicio;
    }

    public LocalTime getFimEfetivo() {
        return diaInteiro || horaFim == null ? LocalTime.of(23, 59) : horaFim;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }

    public LocalTime getHoraFim() { return horaFim; }
    public void setHoraFim(LocalTime horaFim) { this.horaFim = horaFim; }

    public boolean isDiaInteiro() { return diaInteiro; }
    public void setDiaInteiro(boolean diaInteiro) { this.diaInteiro = diaInteiro; }

    public String getLocal() { return local; }
    public void setLocal(String local) { this.local = local; }

    public TipoCompromisso getTipo() { return tipo; }
    public void setTipo(TipoCompromisso tipo) { this.tipo = tipo; }

    public SituacaoCompromisso getSituacao() { return situacao; }
    public void setSituacao(SituacaoCompromisso situacao) { this.situacao = situacao; }

    public PrioridadeCompromisso getPrioridade() { return prioridade; }
    public void setPrioridade(PrioridadeCompromisso prioridade) { this.prioridade = prioridade; }

    public String getObservacoesInternas() { return observacoesInternas; }
    public void setObservacoesInternas(String observacoesInternas) { this.observacoesInternas = observacoesInternas; }

    public String getNomeResponsavel() { return nomeResponsavel; }
    public void setNomeResponsavel(String nomeResponsavel) { this.nomeResponsavel = nomeResponsavel; }

    public String getTelefoneContato() { return telefoneContato; }
    public void setTelefoneContato(String telefoneContato) { this.telefoneContato = telefoneContato; }

    public Cliente getCidadao() { return cidadao; }
    public void setCidadao(Cliente cidadao) { this.cidadao = cidadao; }

    public String getCriadoPor() { return criadoPor; }
    public void setCriadoPor(String criadoPor) { this.criadoPor = criadoPor; }

    public LocalDateTime getCriadoEm() { return criadoEm; }

    public String getAtualizadoPor() { return atualizadoPor; }
    public void setAtualizadoPor(String atualizadoPor) { this.atualizadoPor = atualizadoPor; }

    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }

    public boolean isAvisoWhatsappEnviado() { return avisoWhatsappEnviado; }
    public void setAvisoWhatsappEnviado(boolean avisoWhatsappEnviado) { this.avisoWhatsappEnviado = avisoWhatsappEnviado; }
}
