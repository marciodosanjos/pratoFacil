package com.example.vendeFacil.controller;

import com.example.vendeFacil.exception.RegraNegocioException;
import com.example.vendeFacil.exception.SessaoInvalidaException;
import com.example.vendeFacil.model.Usuario;
import com.example.vendeFacil.service.LojaService;
import com.example.vendeFacil.service.UsuarioService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;

// Perfil do lojista (ADMIN): editar logo (400x400), nome da loja, e-mail e senha.
@Controller
public class AdminPerfilController {

    private final UsuarioService usuarioService;
    private final LojaService lojaService;

    public AdminPerfilController(UsuarioService usuarioService, LojaService lojaService) {
        this.usuarioService = usuarioService;
        this.lojaService = lojaService;
    }

    private Usuario logado(UserDetails principal) {
        return usuarioService.buscarPorEmailOpcional(principal.getUsername())
                .orElseThrow(SessaoInvalidaException::new);
    }

    @GetMapping("/admin/perfil")
    public ModelAndView form(@AuthenticationPrincipal UserDetails principal) {
        Usuario admin = logado(principal);
        ModelAndView mv = new ModelAndView("admin-perfil");
        mv.addObject("usuario", admin);
        mv.addObject("loja", admin.getLoja());
        return mv;
    }

    @PostMapping("/admin/perfil")
    public String salvar(@RequestParam String nomeLoja,
                         @RequestParam String email,
                         @RequestParam(required = false) String senha,
                         @RequestParam(value = "logo", required = false) MultipartFile logo,
                         @AuthenticationPrincipal UserDetails principal) {
        Usuario admin = logado(principal);
        try {
            byte[] logoBytes = (logo != null && !logo.isEmpty()) ? logo.getBytes() : null;
            lojaService.atualizar(admin.getLoja(), nomeLoja, logoBytes);
            boolean emailMudou = usuarioService.atualizarLogin(admin, email, senha);
            if (emailMudou) {
                // O login mudou: o usuario precisa entrar novamente com o novo e-mail.
                return "redirect:/login?emailAlterado";
            }
        } catch (RegraNegocioException | IOException e) {
            return "redirect:/admin/perfil?erro";
        }
        return "redirect:/admin/perfil?ok";
    }
}
