package com.example.vendeFacil.service;

import com.example.vendeFacil.model.Pedido;
import com.example.vendeFacil.model.StatusPagamento;
import com.example.vendeFacil.model.Usuario;
import com.example.vendeFacil.repository.PedidoRepository;
import com.example.vendeFacil.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

// Orquestra o pagamento de um pedido via Asaas: gera a cobrança ao criar o
// pedido e confirma o pagamento quando o webhook do Asaas avisa.
@Service
public class PagamentoService {

    private final AsaasService asaas;
    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;

    public PagamentoService(AsaasService asaas,
                            PedidoRepository pedidoRepository,
                            UsuarioRepository usuarioRepository) {
        this.asaas = asaas;
        this.pedidoRepository = pedidoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // Gera a cobrança PIX no Asaas para um pedido recém-criado.
    // Falhas no gateway NÃO quebram o pedido (ele apenas fica sem cobrança).
    public void gerarCobranca(Pedido pedido, Usuario cliente) {
        if (!asaas.isConfigurado() || cliente == null
                || pedido.getValorTotal() == null || pedido.getValorTotal() <= 0) {
            return;
        }
        try {
            String customerId = cliente.getAsaasCustomerId();
            if (customerId == null || customerId.isBlank()) {
                customerId = asaas.criarCliente(cliente.getNome(), cliente.getEmail());
                cliente.setAsaasCustomerId(customerId);
                usuarioRepository.save(cliente);
            }
            AsaasService.Cobranca c = asaas.criarCobrancaPix(customerId, pedido.getValorTotal(),
                    "Pedido #" + pedido.getId() + " - PratoFacil", String.valueOf(pedido.getId()));
            if (c != null) {
                pedido.setAsaasPaymentId(c.id());
                pedido.setPagamentoUrl(c.invoiceUrl());
                pedido.setStatusPagamento(StatusPagamento.PENDENTE);
                pedidoRepository.save(pedido);
            }
        } catch (Exception e) {
            // gateway indisponível ou erro: o pedido segue sem cobrança
        }
    }

    // Busca o QR Code PIX para exibir na tela de pagamento do app.
    // Só faz sentido se há cobrança ativa (pendente) e o gateway está configurado.
    public AsaasService.PixQrCode obterPix(Pedido pedido) {
        if (!asaas.isConfigurado() || pedido == null
                || pedido.getAsaasPaymentId() == null || pedido.getAsaasPaymentId().isBlank()
                || pedido.getStatusPagamento() != StatusPagamento.PENDENTE) {
            return null;
        }
        try {
            return asaas.obterPixQrCode(pedido.getAsaasPaymentId());
        } catch (Exception e) {
            return null; // sem QR: a tela cai no link externo de pagamento
        }
    }

    // Confirma o pagamento de um pedido a partir do id da cobrança (via webhook).
    public void confirmarPagamento(String asaasPaymentId) {
        if (asaasPaymentId == null) {
            return;
        }
        pedidoRepository.findByAsaasPaymentId(asaasPaymentId).ifPresent(p -> {
            p.setStatusPagamento(StatusPagamento.CONFIRMADO);
            pedidoRepository.save(p);
        });
    }
}
