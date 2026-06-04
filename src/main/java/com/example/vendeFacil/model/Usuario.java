package com.example.vendeFacil.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

// Usuario do sistema. O e-mail é o login; a senha é armazenada como hash BCrypt.
// Quando o usuario é ADMIN, ele é dono de uma Loja (campo loja).
@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    @Column(unique = true, nullable = false)
    private String email;

    @JsonIgnore
    private String senha; // hash BCrypt (nunca a senha em texto puro)

    @Enumerated(EnumType.STRING)
    private Role role;

    // Loja do empreendedor (preenchida apenas para usuarios ADMIN).
    @ManyToOne
    @JoinColumn(name = "loja_id")
    private Loja loja;

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Loja getLoja() {
        return loja;
    }

    public void setLoja(Loja loja) {
        this.loja = loja;
    }
}
