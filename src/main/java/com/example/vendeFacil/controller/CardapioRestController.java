package com.example.vendeFacil.controller;

import com.example.vendeFacil.model.Cardapio;
import com.example.vendeFacil.service.CardapioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// API REST do cardápio. Devolve e recebe JSON, podendo ser consumida
// por qualquer cliente (ex: Postman) via requisições HTTP.
// A regra de negócio mora no CardapioService; aqui só cuidamos do HTTP.
@RestController
@RequestMapping("/api/pratos")
public class CardapioRestController {

    private final CardapioService service;

    public CardapioRestController(CardapioService service) {
        this.service = service;
    }

    // RF01 - listar o cardápio disponível (GET)
    @GetMapping
    public List<Cardapio> listar() {
        return service.listar();
    }

    // Buscar um prato pelo id (404 Not Found se não existir)
    @GetMapping("/{id}")
    public Cardapio buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    // Cadastrar um novo prato (POST). Retorna 201 Created ou 400 Bad Request.
    @PostMapping
    public ResponseEntity<Cardapio> cadastrar(@RequestBody Cardapio cardapio) {
        Cardapio salvo = service.criar(cardapio);
        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }

    // Atualizar um prato existente (PUT). 404 se não existir, 400 se inválido.
    @PutMapping("/{id}")
    public Cardapio atualizar(@PathVariable Long id, @RequestBody Cardapio dados) {
        return service.atualizar(id, dados);
    }

    // Remover um prato (DELETE). Retorna 204 No Content, 404 Not Found ou 409 Conflict.
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
