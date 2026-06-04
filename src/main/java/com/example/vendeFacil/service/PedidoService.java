package com.example.vendeFacil.service;

import com.example.vendeFacil.exception.RecursoNaoEncontradoException;
import com.example.vendeFacil.exception.RegraNegocioException;
import com.example.vendeFacil.model.Cardapio;
import com.example.vendeFacil.model.Pedido;
import com.example.vendeFacil.model.Status;
import com.example.vendeFacil.repository.CardapioRepository;
import com.example.vendeFacil.repository.PedidoRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

// Regras de negócio dos pedidos: monta o pedido a partir dos pratos enviados,
// calcula o valor total no servidor e controla o ciclo de vida do status.
@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final CardapioRepository cardapioRepository;

    public PedidoService(PedidoRepository pedidoRepository, CardapioRepository cardapioRepository) {
        this.pedidoRepository = pedidoRepository;
        this.cardapioRepository = cardapioRepository;
    }

    public List<Pedido> listar() {
        return pedidoRepository.findAll();
    }

    public Pedido buscarPorId(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido não encontrado: " + id));
    }

    // RF02 - cria o pedido. O valor total é SEMPRE calculado no servidor
    // a partir dos pratos enviados, evitando que o cliente informe o preço.
    public Pedido criar(Pedido pedido) {
        if (pedido.getCardapios() == null || pedido.getCardapios().isEmpty()) {
            throw new RegraNegocioException("O pedido precisa ter pelo menos um item");
        }

        List<Cardapio> itens = new ArrayList<>();
        double total = 0.0;

        for (Cardapio item : pedido.getCardapios()) {
            if (item == null || item.getId() == null) {
                continue; // ignora itens vazios vindos do formulário
            }
            Long itemId = item.getId();
            Cardapio prato = cardapioRepository.findById(itemId)
                    .orElseThrow(() -> new RegraNegocioException("Prato inválido no pedido: " + itemId));
            itens.add(prato);
            if (prato.getPreco() != null) {
                total += prato.getPreco();
            }
        }

        if (itens.isEmpty()) {
            throw new RegraNegocioException("O pedido precisa ter pelo menos um item válido");
        }

        pedido.setCardapios(itens);
        pedido.setValorTotal(total);
        pedido.setStatus(Status.EM_PREPARO); // todo pedido novo começa em preparo

        if (pedido.getNome() == null || pedido.getNome().isBlank()) {
            pedido.setNome("Pedido #" + (System.currentTimeMillis() % 10000));
        }

        return pedidoRepository.save(pedido);
    }

    // RF03 - atualiza o status do pedido (ciclo de vida da entrega).
    public Pedido atualizarStatus(Long id, Status status) {
        Pedido pedido = buscarPorId(id);
        pedido.setStatus(status);
        return pedidoRepository.save(pedido);
    }
}
