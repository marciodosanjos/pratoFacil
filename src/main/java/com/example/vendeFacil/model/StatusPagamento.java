package com.example.vendeFacil.model;

// Status do pagamento de um pedido.
public enum StatusPagamento {
    PENDENTE("Aguardando pagamento"),
    CONFIRMADO("Pago");

    private final String descricao;

    StatusPagamento(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
