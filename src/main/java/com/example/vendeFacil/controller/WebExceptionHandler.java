package com.example.vendeFacil.controller;

import com.example.vendeFacil.exception.SessaoInvalidaException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

// Tratamento de erros das telas web (Thymeleaf). Diferente do GlobalExceptionHandler
// (que responde JSON para a API), aqui redirecionamos o usuário para uma página.
@ControllerAdvice
public class WebExceptionHandler {

    // Sessão presa em um usuário que não existe mais -> volta ao login.
    @ExceptionHandler(SessaoInvalidaException.class)
    public String sessaoInvalida() {
        return "redirect:/login?sessao";
    }
}
