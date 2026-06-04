package com.example.vendeFacil.exception;

// Lançada quando os dados violam uma regra de negócio (ex: prato sem nome,
// pedido sem itens). É mapeada para HTTP 400 Bad Request no GlobalExceptionHandler.
public class RegraNegocioException extends RuntimeException {
    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
