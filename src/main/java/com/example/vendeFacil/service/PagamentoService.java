package com.example.vendeFacil.service;

import com.example.vendeFacil.dto.PagamentoCartaoForm;
import com.example.vendeFacil.exception.RegraNegocioException;
import com.example.vendeFacil.model.Pedido;
import com.example.vendeFacil.model.StatusPagamento;
import com.example.vendeFacil.model.TipoPagamento;
import com.example.vendeFacil.model.Usuario;
import com.example.vendeFacil.repository.PedidoRepository;
import com.example.vendeFacil.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            String customerId = garantirCustomerId(cliente);
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

    // Confirma o pagamento pela tela do app, registrando o método escolhido
    // (usado no fluxo do PIX). Em sandbox a confirmação é simulada; o PIX real
    // também confirma sozinho via webhook do Asaas.
    public void confirmarManual(Pedido pedido, TipoPagamento metodo) {
        if (pedido == null) {
            return;
        }
        pedido.setMetodoPagamento(metodo);
        pedido.setStatusPagamento(StatusPagamento.CONFIRMADO);
        pedidoRepository.save(pedido);
    }

    // Processa o pagamento por CARTÃO DE CRÉDITO de fato no Asaas: cria a cobrança
    // CREDIT_CARD com os dados do cartão e do titular e a captura na hora. Se o
    // Asaas recusar (cartão/CPF inválido etc.), lança RegraNegocioException com o
    // motivo para a tela exibir. Sem chave do Asaas, cai no modo simulação local.
    public void pagarComCartao(Pedido pedido, Usuario cliente, PagamentoCartaoForm form, String remoteIp) {
        if (pedido == null) {
            return;
        }
        pedido.setMetodoPagamento(TipoPagamento.CARTAO_CREDITO);

        // Modo simulação (sem integração ativa): confirma localmente, como antes.
        if (!asaas.isConfigurado() || cliente == null
                || pedido.getValorTotal() == null || pedido.getValorTotal() <= 0) {
            pedido.setStatusPagamento(StatusPagamento.CONFIRMADO);
            pedidoRepository.save(pedido);
            return;
        }

        String customerId = garantirCustomerId(cliente);

        String nomeTitular = (form.getTitular() != null && !form.getTitular().isBlank())
                ? form.getTitular().trim() : cliente.getNome();

        String[] validade = separarValidade(form.getValidade());
        AsaasService.DadosCartao cartao = new AsaasService.DadosCartao(
                nomeTitular, digitos(form.getNumero()), validade[0], validade[1], digitos(form.getCvv()));

        String numeroEndereco = (form.getNumeroEndereco() != null && !form.getNumeroEndereco().isBlank())
                ? form.getNumeroEndereco().trim() : "S/N";
        AsaasService.DadosTitular titular = new AsaasService.DadosTitular(
                nomeTitular, cliente.getEmail(), digitos(form.getCpf()),
                digitos(form.getCep()), numeroEndereco, digitos(form.getTelefone()));

        // Cobrança PIX criada na abertura do pedido: vira órfã ao pagar por cartão.
        String cobrancaPixPendente = pedido.getAsaasPaymentId();

        try {
            AsaasService.Cobranca c = asaas.criarCobrancaCartao(
                    customerId, pedido.getValorTotal(),
                    "Pedido #" + pedido.getId() + " - PratoFacil", String.valueOf(pedido.getId()),
                    cartao, titular, remoteIp);
            if (c == null) {
                throw new RegraNegocioException("Não foi possível processar o pagamento no cartão. Tente novamente.");
            }
            pedido.setAsaasPaymentId(c.id());
            pedido.setPagamentoUrl(c.invoiceUrl());
            boolean pago = "CONFIRMED".equals(c.status()) || "RECEIVED".equals(c.status());
            pedido.setStatusPagamento(pago ? StatusPagamento.CONFIRMADO : StatusPagamento.PENDENTE);
            pedidoRepository.save(pedido);

            // Remove a cobrança PIX pendente do pedido para não sobrar lixo no
            // painel do Asaas (best-effort: se falhar, o pagamento já está feito).
            if (cobrancaPixPendente != null && !cobrancaPixPendente.equals(c.id())) {
                try {
                    asaas.removerCobranca(cobrancaPixPendente);
                } catch (Exception ignored) {
                    // segue mesmo se a remoção falhar
                }
            }
        } catch (RestClientResponseException e) {
            throw new RegraNegocioException(extrairMensagemAsaas(e));
        }
    }

    // Garante que o cliente tenha um id de cliente no Asaas (cria e guarda se faltar).
    private String garantirCustomerId(Usuario cliente) {
        String customerId = cliente.getAsaasCustomerId();
        if (customerId == null || customerId.isBlank()) {
            customerId = asaas.criarCliente(cliente.getNome(), cliente.getEmail());
            cliente.setAsaasCustomerId(customerId);
            usuarioRepository.save(cliente);
        }
        return customerId;
    }

    // Mantém apenas os dígitos de um campo (número do cartão, CPF, CEP, telefone).
    private static String digitos(String valor) {
        return valor == null ? "" : valor.replaceAll("\\D", "");
    }

    // Separa "MM/AA" (ou "MM/AAAA") em [mes, ano] no formato esperado pelo Asaas.
    private static String[] separarValidade(String validade) {
        String v = validade == null ? "" : validade.trim();
        int barra = v.indexOf('/');
        String mes = barra > 0 ? v.substring(0, barra).trim() : "";
        String ano = barra > 0 ? v.substring(barra + 1).trim() : "";
        if (ano.length() == 2) {
            ano = "20" + ano; // 28 -> 2028
        }
        return new String[]{mes, ano};
    }

    // Extrai a mensagem de erro retornada pelo Asaas ("description") para exibir
    // ao cliente; se não conseguir, usa uma mensagem genérica.
    private static String extrairMensagemAsaas(RestClientResponseException e) {
        String corpo = e.getResponseBodyAsString();
        if (corpo != null) {
            Matcher m = Pattern.compile("\"description\"\\s*:\\s*\"([^\"]+)\"").matcher(corpo);
            if (m.find()) {
                return m.group(1);
            }
        }
        return "Não foi possível processar o pagamento no cartão. Verifique os dados e tente novamente.";
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
