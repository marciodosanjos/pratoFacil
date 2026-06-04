package com.example.vendeFacil.model;

import jakarta.persistence.*;

// Uma loja (estabelecimento) do marketplace. Cada empreendedor (ADMIN) é dono de
// uma loja, que possui seu próprio cardápio e recebe seus próprios pedidos.
@Entity
@Table(name = "loja")
public class Loja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String descricao;

    public Loja() {
    }

    public Loja(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
    }

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

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}
