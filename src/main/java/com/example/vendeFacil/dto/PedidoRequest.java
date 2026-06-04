package com.example.vendeFacil.dto;

import java.util.ArrayList;
import java.util.List;

// Dados que o cliente envia para criar um pedido: uma lista de itens,
// cada um com o id do prato e a quantidade desejada. Usado tanto pela
// interface web (formulario) quanto pela API REST (JSON).
public class PedidoRequest {

    private List<ItemRequest> itens = new ArrayList<>();

    public List<ItemRequest> getItens() {
        return itens;
    }

    public void setItens(List<ItemRequest> itens) {
        this.itens = itens;
    }

    public static class ItemRequest {
        private Long cardapioId;
        private int quantidade;

        public Long getCardapioId() {
            return cardapioId;
        }

        public void setCardapioId(Long cardapioId) {
            this.cardapioId = cardapioId;
        }

        public int getQuantidade() {
            return quantidade;
        }

        public void setQuantidade(int quantidade) {
            this.quantidade = quantidade;
        }
    }
}
