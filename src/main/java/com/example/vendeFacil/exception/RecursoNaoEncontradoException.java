package com.example.vendeFacil.exception;

// Lançada quando um recurso (prato ou pedido) não é encontrado.
// É mapeada para HTTP 404 Not Found no GlobalExceptionHandler.
public class RecursoNaoEncontradoException extends RuntimeException {
    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
