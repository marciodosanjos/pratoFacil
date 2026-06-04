package com.example.vendeFacil.service;

import com.example.vendeFacil.dto.PedidoRequest;
import com.example.vendeFacil.exception.RecursoNaoEncontradoException;
import com.example.vendeFacil.exception.RegraNegocioException;
import com.example.vendeFacil.model.Cardapio;
import com.example.vendeFacil.model.ItemPedido;
import com.example.vendeFacil.model.Loja;
import com.example.vendeFacil.model.Pedido;
import com.example.vendeFacil.model.Status;
import com.example.vendeFacil.model.StatusPagamento;
import com.example.vendeFacil.model.Usuario;
import com.example.vendeFacil.repository.CardapioRepository;
import com.example.vendeFacil.repository.PedidoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Regras de negócio dos pedidos: monta o pedido a partir dos itens (com
// quantidade) enviados, calcula o valor total no servidor e controla o status.
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

    public List<Pedido> listarDoCliente(Usuario cliente) {
        return pedidoRepository.findByClienteOrderByIdDesc(cliente);
    }

    public List<Pedido> listarDaLoja(Loja loja) {
        return pedidoRepository.findByLojaOrderByIdDesc(loja);
    }

    public Pedido buscarPorId(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido não encontrado: " + id));
    }

    public Pedido buscarDoCliente(Long id, Usuario cliente) {
        Pedido pedido = buscarPorId(id);
        if (cliente == null || pedido.getCliente() == null
                || !pedido.getCliente().getId().equals(cliente.getId())) {
            throw new RecursoNaoEncontradoException("Pedido não encontrado: " + id);
        }
        return pedido;
    }

    // Busca um pedido garantindo que ele pertence a loja informada
    // (um lojista nunca acessa o pedido de outra loja).
    public Pedido buscarDaLoja(Long id, Loja loja) {
        Pedido pedido = buscarPorId(id);
        if (loja == null || pedido.getLoja() == null
                || !pedido.getLoja().getId().equals(loja.getId())) {
            throw new RecursoNaoEncontradoException("Pedido não encontrado: " + id);
        }
        return pedido;
    }

    // RF02 - cria o pedido a partir dos itens (com quantidade) enviados pelo cliente.
    // O valor total é SEMPRE calculado no servidor; a loja é derivada dos itens.
    public Pedido criar(PedidoRequest request, Usuario cliente) {
        if (request == null || request.getItens() == null || request.getItens().isEmpty()) {
            throw new RegraNegocioException("O pedido precisa ter pelo menos um item");
        }

        List<ItemPedido> itens = new ArrayList<>();
        double total = 0.0;
        Loja loja = null;

        for (PedidoRequest.ItemRequest ir : request.getItens()) {
            if (ir == null || ir.getCardapioId() == null || ir.getQuantidade() <= 0) {
                continue; // ignora itens nao selecionados (quantidade 0)
            }
            Long itemId = ir.getCardapioId();
            Cardapio prato = cardapioRepository.findById(itemId)
                    .orElseThrow(() -> new RegraNegocioException("Prato inválido no pedido: " + itemId));

            // Todos os itens precisam ser da mesma loja
            if (loja == null) {
                loja = prato.getLoja();
            } else if (prato.getLoja() != null && !prato.getLoja().getId().equals(loja.getId())) {
                throw new RegraNegocioException("Um pedido só pode conter itens da mesma loja");
            }

            itens.add(new ItemPedido(prato, ir.getQuantidade()));
            if (prato.getPreco() != null) {
                total += prato.getPreco() * ir.getQuantidade();
            }
        }

        if (itens.isEmpty()) {
            throw new RegraNegocioException("Selecione ao menos um item (quantidade maior que zero)");
        }

        Pedido pedido = new Pedido();
        pedido.setItens(itens);
        pedido.setValorTotal(total);
        pedido.setStatus(Status.EM_PREPARO);
        // Todo pedido nasce aguardando pagamento; a confirmação acontece na
        // tela de pagamento (PIX/cartão) ou via webhook do Asaas.
        pedido.setStatusPagamento(StatusPagamento.PENDENTE);
        pedido.setCliente(cliente);
        pedido.setLoja(loja);
        String quem = (cliente != null && cliente.getNome() != null) ? cliente.getNome() : "Cliente";
        pedido.setNome("Pedido de " + quem);
        pedido.setDataCriacao(LocalDateTime.now());

        return pedidoRepository.save(pedido);
    }

    // RF03 - atualiza o status do pedido (ciclo de vida da entrega).
    public Pedido atualizarStatus(Long id, Status status) {
        Pedido pedido = buscarPorId(id);
        pedido.setStatus(status);
        return pedidoRepository.save(pedido);
    }
}
