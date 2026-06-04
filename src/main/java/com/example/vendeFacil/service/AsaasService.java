package com.example.vendeFacil.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

// Cliente HTTP para a API do Asaas (sandbox por padrão): cria o cliente
// e a cobrança PIX. A chave vem da configuração asaas.api-key.
@Service
public class AsaasService {

    private final RestClient client;
    private final String apiKey;

    public AsaasService(@Value("${asaas.base-url}") String baseUrl,
                        @Value("${asaas.api-key:}") String apiKey) {
        this.apiKey = apiKey;
        this.client = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("access_token", apiKey == null ? "" : apiKey)
                .build();
    }

    public boolean isConfigurado() {
        return apiKey != null && !apiKey.isBlank();
    }

    public String criarCliente(String nome, String email) {
        Map<String, Object> body = new HashMap<>();
        body.put("name", (nome == null || nome.isBlank()) ? "Cliente PratoFacil" : nome);
        body.put("email", email);
        body.put("cpfCnpj", "24971563792"); // CPF de teste do sandbox (simulacao)
        Map<?, ?> resp = client.post().uri("/customers")
                .contentType(MediaType.APPLICATION_JSON).body(body)
                .retrieve().body(Map.class);
        return resp != null ? (String) resp.get("id") : null;
    }

    public Cobranca criarCobrancaPix(String customerId, double valor, String descricao, String referencia) {
        Map<String, Object> body = new HashMap<>();
        body.put("customer", customerId);
        body.put("billingType", "PIX");
        body.put("value", valor);
        body.put("dueDate", LocalDate.now().plusDays(3).toString());
        body.put("description", descricao);
        body.put("externalReference", referencia);
        Map<?, ?> resp = client.post().uri("/payments")
                .contentType(MediaType.APPLICATION_JSON).body(body)
                .retrieve().body(Map.class);
        if (resp == null) {
            return null;
        }
        return new Cobranca((String) resp.get("id"), (String) resp.get("invoiceUrl"), (String) resp.get("status"));
    }

    // Cria uma cobrança de CARTÃO DE CRÉDITO e já tenta capturá-la na hora,
    // enviando os dados do cartão e do titular. Se o Asaas aprovar, a cobrança
    // volta com status CONFIRMED/RECEIVED. Em caso de recusa/dados inválidos, o
    // Asaas responde com erro HTTP 4xx (a chamada lança exceção).
    public Cobranca criarCobrancaCartao(String customerId, double valor, String descricao, String referencia,
                                        DadosCartao cartao, DadosTitular titular, String remoteIp) {
        Map<String, Object> creditCard = new HashMap<>();
        creditCard.put("holderName", cartao.holderName());
        creditCard.put("number", cartao.number());
        creditCard.put("expiryMonth", cartao.expiryMonth());
        creditCard.put("expiryYear", cartao.expiryYear());
        creditCard.put("ccv", cartao.ccv());

        Map<String, Object> holderInfo = new HashMap<>();
        holderInfo.put("name", titular.name());
        holderInfo.put("email", titular.email());
        holderInfo.put("cpfCnpj", titular.cpfCnpj());
        holderInfo.put("postalCode", titular.postalCode());
        holderInfo.put("addressNumber", titular.addressNumber());
        holderInfo.put("phone", titular.phone());

        Map<String, Object> body = new HashMap<>();
        body.put("customer", customerId);
        body.put("billingType", "CREDIT_CARD");
        body.put("value", valor);
        body.put("dueDate", LocalDate.now().toString()); // captura imediata
        body.put("description", descricao);
        body.put("externalReference", referencia);
        body.put("creditCard", creditCard);
        body.put("creditCardHolderInfo", holderInfo);
        if (remoteIp != null && !remoteIp.isBlank()) {
            body.put("remoteIp", remoteIp); // exigido pelo Asaas na cobrança por cartão
        }

        Map<?, ?> resp = client.post().uri("/payments")
                .contentType(MediaType.APPLICATION_JSON).body(body)
                .retrieve().body(Map.class);
        if (resp == null) {
            return null;
        }
        return new Cobranca((String) resp.get("id"), (String) resp.get("invoiceUrl"), (String) resp.get("status"));
    }

    // Remove uma cobrança no Asaas (best-effort): usado para limpar a cobrança PIX
    // pendente quando o pedido acaba sendo pago por cartão.
    public void removerCobranca(String paymentId) {
        if (paymentId == null || paymentId.isBlank()) {
            return;
        }
        client.delete().uri("/payments/{id}", paymentId).retrieve().toBodilessEntity();
    }

    // Busca o QR Code PIX de uma cobrança: imagem (base64) + código copia-e-cola.
    public PixQrCode obterPixQrCode(String paymentId) {
        Map<?, ?> resp = client.get().uri("/payments/{id}/pixQrCode", paymentId)
                .retrieve().body(Map.class);
        if (resp == null) {
            return null;
        }
        return new PixQrCode((String) resp.get("encodedImage"), (String) resp.get("payload"));
    }

    // Resultado de uma cobrança: id do pagamento, link de pagamento e status.
    public record Cobranca(String id, String invoiceUrl, String status) {
    }

    // QR Code PIX: imagem em base64 (PNG) e o payload "copia e cola".
    public record PixQrCode(String encodedImage, String payload) {
    }

    // Dados do cartão enviados ao Asaas (não são armazenados no banco).
    public record DadosCartao(String holderName, String number, String expiryMonth, String expiryYear, String ccv) {
    }

    // Dados do titular exigidos pelo Asaas para a cobrança por cartão.
    public record DadosTitular(String name, String email, String cpfCnpj, String postalCode,
                               String addressNumber, String phone) {
    }
}
