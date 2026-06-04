package com.example.vendeFacil.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.HashMap;
import java.util.Map;

// Disponibiliza para todas as telas (Thymeleaf) o estado de autenticacao,
// para o menu mostrar os links certos: anonimo (Entrar/Criar conta),
// cliente (Meus Pedidos/Perfil) ou vendedor (Meu Painel). Sem consultar o banco
// (le o papel direto das authorities da sessao).
@ControllerAdvice
public class GlobalModelAdvice {

    @ModelAttribute("auth")
    public Map<String, Object> auth(@AuthenticationPrincipal UserDetails principal) {
        Map<String, Object> info = new HashMap<>();
        boolean logado = principal != null;
        info.put("logado", logado);
        info.put("admin", logado && principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        return info;
    }
}
