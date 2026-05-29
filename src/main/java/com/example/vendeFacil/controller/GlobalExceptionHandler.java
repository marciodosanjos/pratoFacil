package com.example.vendeFacil.controller;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;

// Tratamento central de erros das APIs REST. Devolve sempre um JSON
// simples e padronizado (sem o stacktrace) para cada situação.
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Erros lançados de propósito nos controllers (ex: 404 Not Found, 400 Bad Request)
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> tratarStatus(ResponseStatusException ex) {
        return montarResposta(HttpStatus.valueOf(ex.getStatusCode().value()), ex.getReason());
    }

    // Tentativa de remover um prato que ainda está vinculado a um pedido -> 409 Conflict
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> tratarIntegridade(DataIntegrityViolationException ex) {
        return montarResposta(HttpStatus.CONFLICT,
                "Não é possível remover: o item está vinculado a um pedido existente");
    }

    private ResponseEntity<Map<String, Object>> montarResposta(HttpStatus status, String mensagem) {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("status", status.value());
        corpo.put("error", status.getReasonPhrase());
        corpo.put("message", mensagem);
        return ResponseEntity.status(status).body(corpo);
    }
}
