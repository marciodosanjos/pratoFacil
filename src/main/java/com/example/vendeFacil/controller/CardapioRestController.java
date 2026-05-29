package com.example.vendeFacil.controller;

import com.example.vendeFacil.model.Cardapio;
import com.example.vendeFacil.repository.CardapioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

// API REST do cardápio. Devolve e recebe JSON, podendo ser consumida
// por qualquer cliente (ex: Postman) via requisições HTTP.
@RestController
@RequestMapping("/api/pratos")
public class CardapioRestController {

    @Autowired
    private CardapioRepository repository;

    // RF01 - listar o cardápio disponível (GET)
    @GetMapping
    public List<Cardapio> listar() {
        return repository.findAll();
    }

    // Buscar um prato pelo id (404 Not Found se não existir)
    @GetMapping("/{id}")
    public Cardapio buscarPorId(@PathVariable Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Prato não encontrado: " + id));
    }

    // Cadastrar um novo prato (POST). Retorna 201 Created ou 400 Bad Request.
    @PostMapping
    public ResponseEntity<Cardapio> cadastrar(@RequestBody Cardapio cardapio) {
        validar(cardapio);
        Cardapio salvo = repository.save(cardapio);
        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }

    // Atualizar um prato existente (PUT). 404 se não existir, 400 se inválido.
    @PutMapping("/{id}")
    public Cardapio atualizar(@PathVariable Long id, @RequestBody Cardapio dados) {
        Cardapio prato = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Prato não encontrado: " + id));
        validar(dados);
        prato.setNome(dados.getNome());
        prato.setDescricao(dados.getDescricao());
        prato.setPreco(dados.getPreco());
        return repository.save(prato);
    }

    // Remover um prato (DELETE). Retorna 204 No Content ou 404 Not Found.
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Prato não encontrado: " + id);
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Validação simples dos dados recebidos -> 400 Bad Request
    private void validar(Cardapio c) {
        if (c.getNome() == null || c.getNome().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "O nome do prato é obrigatório");
        }
        if (c.getPreco() == null || c.getPreco() < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "O preço deve ser informado e não pode ser negativo");
        }
    }
}
