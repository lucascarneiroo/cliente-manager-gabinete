package com.exemplo.clientemanager.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "servicos")
public class Servico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @NotNull(message = "Data do servico e obrigatoria")
    @Column(name = "data_servico", nullable = false)
    private LocalDate dataServico = LocalDate.now();

    @NotBlank(message = "Informe o servico prestado")
    @Size(max = 200, message = "Servico deve ter ate 200 caracteres")
    @Column(nullable = false, length = 200)
    private String descricao;

    @DecimalMin(value = "0.0", inclusive = true, message = "Valor nao pode ser negativo")
    @Column(precision = 12, scale = 2)
    private BigDecimal valor;

    @Size(max = 1000, message = "Observacoes devem ter ate 1000 caracteres")
    @Column(length = 1000)
    private String observacoes;

    @Column(name = "realizado")
    private Boolean realizado = false;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public LocalDate getDataServico() { return dataServico; }
    public void setDataServico(LocalDate dataServico) { this.dataServico = dataServico; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
    public Boolean getRealizado() { return realizado; }
    public void setRealizado(Boolean realizado) { this.realizado = realizado; }
}
