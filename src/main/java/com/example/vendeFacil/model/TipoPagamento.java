package com.example.vendeFacil.model;

// Tipos de método de pagamento que o cliente pode cadastrar.
public enum TipoPagamento {
    PIX("Pix"),
    CARTAO_CREDITO("Cartão de crédito"),
    CARTAO_DEBITO("Cartão de débito"),
    DINHEIRO("Dinheiro");

    private final String descricao;

    TipoPagamento(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
