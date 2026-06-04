package com.example.vendeFacil.controller;

import com.example.vendeFacil.exception.RegraNegocioException;
import com.example.vendeFacil.exception.SessaoInvalidaException;
import com.example.vendeFacil.model.Usuario;
import com.example.vendeFacil.service.UsuarioService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

// Área de perfil do usuário logado (ver e editar nome/senha).
@Controller
public class PerfilController {

    private final UsuarioService usuarioService;

    public PerfilController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    private Usuario logado(UserDetails principal) {
        return usuarioService.buscarPorEmailOpcional(principal.getUsername())
                .orElseThrow(SessaoInvalidaException::new);
    }

    @GetMapping("/perfil")
    public ModelAndView perfil(@AuthenticationPrincipal UserDetails principal) {
        ModelAndView mv = new ModelAndView("perfil");
        mv.addObject("usuario", logado(principal));
        return mv;
    }

    @PostMapping("/perfil")
    public String salvar(@RequestParam String nome,
                         @RequestParam(required = false) String senha,
                         @AuthenticationPrincipal UserDetails principal) {
        Usuario u = logado(principal);
        try {
            usuarioService.atualizarPerfil(u, nome, senha);
        } catch (RegraNegocioException e) {
            return "redirect:/perfil?erro";
        }
        return "redirect:/perfil?ok";
    }
}
