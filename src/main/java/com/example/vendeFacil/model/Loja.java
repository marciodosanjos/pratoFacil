package com.example.vendeFacil.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    // Logo da loja (imagem 400x400), guardada como bytes e servida em /lojas/{id}/logo.
    @JsonIgnore
    @Column(length = 1048576)
    private byte[] logo;

    private String logoTipo;

    public Loja() {
    }

    public Loja(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
    }

    public boolean temLogo() {
        return logo != null && logo.length > 0;
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

    public byte[] getLogo() {
        return logo;
    }

    public void setLogo(byte[] logo) {
        this.logo = logo;
    }

    public String getLogoTipo() {
        return logoTipo;
    }

    public void setLogoTipo(String logoTipo) {
        this.logoTipo = logoTipo;
    }
}
