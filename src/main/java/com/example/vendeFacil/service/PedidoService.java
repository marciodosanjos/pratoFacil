package com.example.vendeFacil.service;

import com.example.vendeFacil.exception.RecursoNaoEncontradoException;
import com.example.vendeFacil.exception.RegraNegocioException;
import com.example.vendeFacil.model.Cardapio;
import com.example.vendeFacil.model.Pedido;
import com.example.vendeFacil.model.Status;
import com.example.vendeFacil.model.Usuario;
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

    // Todos os pedidos (visao do empreendedor).
    public List<Pedido> listar() {
        return pedidoRepository.findAll();
    }

    // Apenas os pedidos do cliente informado (area "Meus Pedidos").
    public List<Pedido> listarDoCliente(Usuario cliente) {
        return pedidoRepository.findByClienteOrderByIdDesc(cliente);
    }

    public Pedido buscarPorId(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido não encontrado: " + id));
    }

    // Busca um pedido garantindo que ele pertence ao cliente informado
    // (um cliente nunca acessa o pedido de outro).
    public Pedido buscarDoCliente(Long id, Usuario cliente) {
        Pedido pedido = buscarPorId(id);
        if (cliente == null || pedido.getCliente() == null
                || !pedido.getCliente().getId().equals(cliente.getId())) {
            throw new RecursoNaoEncontradoException("Pedido não encontrado: " + id);
        }
        return pedido;
    }

    // RF02 - cria o pedido vinculado ao cliente logado. O valor total é SEMPRE
    // calculado no servidor a partir dos pratos enviados (o cliente não envia preço).
    public Pedido criar(Pedido pedido, Usuario cliente) {
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
        pedido.setCliente(cliente);

        if (pedido.getNome() == null || pedido.getNome().isBlank()) {
            String quem = (cliente != null && cliente.getNome() != null) ? cliente.getNome() : "Cliente";
            pedido.setNome("Pedido de " + quem);
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
