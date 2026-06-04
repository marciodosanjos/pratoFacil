package com.example.vendeFacil.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
    private Double valorTotal;

    // Itens do pedido (cada um com a sua quantidade).
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "pedido_id")
    private List<ItemPedido> itens = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private Status status;

    // Cliente dono do pedido. @JsonIgnore para nao expor os dados do usuario na API.
    @ManyToOne
    @JoinColumn(name = "cliente_id")
    @JsonIgnore
    private Usuario cliente;

    // Loja para a qual o pedido foi feito.
    @ManyToOne
    @JoinColumn(name = "loja_id")
    @JsonIgnore
    private Loja loja;

    private LocalDateTime dataCriacao;

    // Pagamento (integracao Asaas)
    private String asaasPaymentId;
    private String pagamentoUrl;

    @Enumerated(EnumType.STRING)
    private StatusPagamento statusPagamento;

    // Como o cliente pagou (PIX ou cartão), definido na tela de pagamento.
    @Enumerated(EnumType.STRING)
    private TipoPagamento metodoPagamento;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Double getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(Double valorTotal) {
        this.valorTotal = valorTotal;
    }

    public List<ItemPedido> getItens() {
        return itens;
    }

    public void setItens(List<ItemPedido> itens) {
        this.itens = itens;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Usuario getCliente() {
        return cliente;
    }

    public void setCliente(Usuario cliente) {
        this.cliente = cliente;
    }

    public Loja getLoja() {
        return loja;
    }

    public void setLoja(Loja loja) {
        this.loja = loja;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    // Data/hora formatada para exibicao (vazio se nao houver).
    public String getDataFormatada() {
        return dataCriacao == null ? "" : dataCriacao.format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm"));
    }

    // Quantidade total de unidades no pedido (soma das quantidades dos itens).
    public int getTotalUnidades() {
        int t = 0;
        for (ItemPedido i : itens) {
            t += i.getQuantidade();
        }
        return t;
    }

    public String getAsaasPaymentId() {
        return asaasPaymentId;
    }

    public void setAsaasPaymentId(String asaasPaymentId) {
        this.asaasPaymentId = asaasPaymentId;
    }

    public String getPagamentoUrl() {
        return pagamentoUrl;
    }

    public void setPagamentoUrl(String pagamentoUrl) {
        this.pagamentoUrl = pagamentoUrl;
    }

    public StatusPagamento getStatusPagamento() {
        return statusPagamento;
    }

    public void setStatusPagamento(StatusPagamento statusPagamento) {
        this.statusPagamento = statusPagamento;
    }

    public TipoPagamento getMetodoPagamento() {
        return metodoPagamento;
    }

    public void setMetodoPagamento(TipoPagamento metodoPagamento) {
        this.metodoPagamento = metodoPagamento;
    }
}
