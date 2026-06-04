package com.example.vendeFacil.exception;

// Lançada quando o usuário autenticado na sessão não existe mais no banco
// (ex.: conta removida). Nas telas web, leva o usuário de volta ao login.
public class SessaoInvalidaException extends RuntimeException {
}
