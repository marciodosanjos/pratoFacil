package com.example.vendeFacil.controller;

import com.example.vendeFacil.service.PagamentoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

// Recebe os eventos de pagamento do Asaas (webhook) e confirma o pedido pago.
@RestController
public class AsaasWebhookController {

    private final PagamentoService pagamentoService;
    private final String webhookToken;

    public AsaasWebhookController(PagamentoService pagamentoService,
                                  @Value("${asaas.webhook-token:}") String webhookToken) {
        this.pagamentoService = pagamentoService;
        this.webhookToken = webhookToken;
    }

    @PostMapping("/webhooks/asaas")
    public ResponseEntity<Void> receber(@RequestBody(required = false) Map<String, Object> payload,
                                        @RequestHeader(value = "asaas-access-token", required = false) String token) {
        // Se um token foi configurado (ASAAS_WEBHOOK_TOKEN), exige que o Asaas o
        // envie no header — garante que a chamada veio mesmo do Asaas. Sem token
        // configurado, o webhook é aceito (útil para testes locais).
        if (webhookToken != null && !webhookToken.isBlank() && !webhookToken.equals(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (payload != null) {
            String event = (String) payload.get("event");
            Object pagObj = payload.get("payment");
            if (pagObj instanceof Map<?, ?> payment
                    && ("PAYMENT_CONFIRMED".equals(event) || "PAYMENT_RECEIVED".equals(event))) {
                pagamentoService.confirmarPagamento((String) payment.get("id"));
            }
        }
        return ResponseEntity.ok().build();
    }
}
