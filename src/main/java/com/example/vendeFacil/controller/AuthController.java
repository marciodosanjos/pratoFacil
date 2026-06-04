package com.example.vendeFacil.controller;

import com.example.vendeFacil.exception.RegraNegocioException;
import com.example.vendeFacil.service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

// Paginas de login e cadastro de cliente.
@Controller
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/cadastro")
    public String cadastroForm() {
        return "cadastro";
    }

    @PostMapping("/cadastro")
    public String cadastrar(@RequestParam(defaultValue = "cliente") String tipo,
                            @RequestParam String nome,
                            @RequestParam String email,
                            @RequestParam String senha,
                            @RequestParam(required = false) String nomeLoja,
                            @RequestParam(required = false) String descricaoLoja) {
        try {
            if ("vendedor".equals(tipo)) {
                usuarioService.registrarVendedor(nome, email, senha, nomeLoja, descricaoLoja);
            } else {
                usuarioService.registrarCliente(nome, email, senha);
            }
        } catch (RegraNegocioException e) {
            // Volta ao formulario sinalizando o erro (ex.: e-mail ja cadastrado)
            return "redirect:/cadastro?erro";
        }
        return "redirect:/login?cadastro";
    }
}
