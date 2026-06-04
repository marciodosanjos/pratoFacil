package com.example.vendeFacil.model;

import jakarta.persistence.*;

// Um item de um pedido: qual prato e em qual quantidade.
@Entity
@Table(name = "item_pedido")
public class ItemPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cardapio_id")
    private Cardapio cardapio;

    private int quantidade;

    public ItemPedido() {
    }

    public ItemPedido(Cardapio cardapio, int quantidade) {
        this.cardapio = cardapio;
        this.quantidade = quantidade;
    }

    // Subtotal do item (preço unitário x quantidade).
    public double getSubtotal() {
        double preco = (cardapio != null && cardapio.getPreco() != null) ? cardapio.getPreco() : 0.0;
        return preco * quantidade;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Cardapio getCardapio() {
        return cardapio;
    }

    public void setCardapio(Cardapio cardapio) {
        this.cardapio = cardapio;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }
}
