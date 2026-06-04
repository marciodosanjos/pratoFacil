package com.example.vendeFacil.model;

// Categorias para organizar os itens do cardápio.
public enum Categoria {
    PRATOS_PRINCIPAIS("Pratos Principais"),
    LANCHES("Lanches"),
    PIZZAS("Pizzas"),
    PORCOES("Porções"),
    ACAI("Açaí"),
    SOBREMESAS("Sobremesas"),
    BEBIDAS("Bebidas"),
    OUTROS("Outros");

    private final String descricao;

    Categoria(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
