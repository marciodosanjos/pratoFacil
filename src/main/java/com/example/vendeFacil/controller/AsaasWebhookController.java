package com.example.vendeFacil.controller;

import com.example.vendeFacil.service.PagamentoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

// Recebe os eventos de pagamento do Asaas (webhook) e confirma o pedido pago.
@RestController
public class AsaasWebhookController {

    private final PagamentoService pagamentoService;

    public AsaasWebhookController(PagamentoService pagamentoService) {
        this.pagamentoService = pagamentoService;
    }

    @PostMapping("/webhooks/asaas")
    public ResponseEntity<Void> receber(@RequestBody(required = false) Map<String, Object> payload) {
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
