package com.example.vendeFacil.controller;

import com.example.vendeFacil.model.Cardapio;
import com.example.vendeFacil.model.Pedido;
import com.example.vendeFacil.model.Status;
import com.example.vendeFacil.repository.CardapioRepository;
import com.example.vendeFacil.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

// API REST dos pedidos. Cobre o ciclo de vida do pedido (criação e
// atualização de status), trafegando os dados em JSON.
@RestController
@RequestMapping("/api/pedidos")
public class PedidoRestController {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private CardapioRepository cardapioRepository;

    // Listar todos os pedidos (GET)
    @GetMapping
    public List<Pedido> listar() {
        return pedidoRepository.findAll();
    }

    // Buscar um pedido pelo id (404 Not Found se não existir)
    @GetMapping("/{id}")
    public Pedido buscarPorId(@PathVariable Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Pedido não encontrado: " + id));
    }

    // RF02 - o cliente realiza um pedido (POST).
    // O valor total é calculado no servidor a partir dos pratos enviados.
    @PostMapping
    public ResponseEntity<Pedido> criar(@RequestBody Pedido pedido) {
        if (pedido.getCardapios() == null || pedido.getCardapios().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "O pedido precisa ter pelo menos um item");
        }

        List<Cardapio> itens = new ArrayList<>();
        double total = 0.0;

        for (Cardapio item : pedido.getCardapios()) {
            Long itemId = (long) item.getId();
            Cardapio prato = cardapioRepository.findById(itemId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, "Prato inválido no pedido: " + itemId));
            itens.add(prato);
            if (prato.getPreco() != null) {
                total += prato.getPreco();
            }
        }

        pedido.setCardapios(itens);
        pedido.setValorTotal(total);
        pedido.setStatus(Status.EM_PREPARO); // todo pedido novo começa em preparo

        if (pedido.getNome() == null || pedido.getNome().isBlank()) {
            pedido.setNome("Pedido #" + (System.currentTimeMillis() % 10000));
        }

        Pedido salvo = pedidoRepository.save(pedido);
        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }

    // RF03 - o empreendedor atualiza o status do pedido (PUT).
    // Ex: /api/pedidos/1/status?status=SAIU_PARA_ENTREGA  (404 se não existir)
    @PutMapping("/{id}/status")
    public Pedido atualizarStatus(@PathVariable Long id, @RequestParam Status status) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Pedido não encontrado: " + id));
        pedido.setStatus(status);
        return pedidoRepository.save(pedido);
    }
}
