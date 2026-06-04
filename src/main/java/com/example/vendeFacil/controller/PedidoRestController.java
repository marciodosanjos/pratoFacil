package com.example.vendeFacil.controller;

import com.example.vendeFacil.model.Pedido;
import com.example.vendeFacil.model.Status;
import com.example.vendeFacil.service.PedidoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// API REST dos pedidos. Cobre o ciclo de vida do pedido (criação e
// atualização de status), trafegando os dados em JSON.
// A regra de negócio mora no PedidoService; aqui só cuidamos do HTTP.
@RestController
@RequestMapping("/api/pedidos")
public class PedidoRestController {

    private final PedidoService service;

    public PedidoRestController(PedidoService service) {
        this.service = service;
    }

    // Listar todos os pedidos (GET)
    @GetMapping
    public List<Pedido> listar() {
        return service.listar();
    }

    // Buscar um pedido pelo id (404 Not Found se não existir)
    @GetMapping("/{id}")
    public Pedido buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    // RF02 - o cliente realiza um pedido (POST).
    // O valor total é calculado no servidor a partir dos pratos enviados.
    @PostMapping
    public ResponseEntity<Pedido> criar(@RequestBody Pedido pedido) {
        Pedido salvo = service.criar(pedido);
        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }

    // RF03 - o empreendedor atualiza o status do pedido (PUT).
    // Ex: /api/pedidos/1/status?status=SAIU_PARA_ENTREGA  (404 se não existir)
    @PutMapping("/{id}/status")
    public Pedido atualizarStatus(@PathVariable Long id, @RequestParam Status status) {
        return service.atualizarStatus(id, status);
    }
}
