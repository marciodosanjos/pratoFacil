package com.example.vendeFacil.controller;

import com.example.vendeFacil.exception.RegraNegocioException;
import com.example.vendeFacil.exception.SessaoInvalidaException;
import com.example.vendeFacil.model.TipoPagamento;
import com.example.vendeFacil.model.Usuario;
import com.example.vendeFacil.service.MetodoPagamentoService;
import com.example.vendeFacil.service.UsuarioService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

// Área de perfil do usuário logado: editar nome/senha e gerenciar os
// métodos de pagamento (adicionar/excluir).
@Controller
public class PerfilController {

    private final UsuarioService usuarioService;
    private final MetodoPagamentoService metodoService;

    public PerfilController(UsuarioService usuarioService, MetodoPagamentoService metodoService) {
        this.usuarioService = usuarioService;
        this.metodoService = metodoService;
    }

    private Usuario logado(UserDetails principal) {
        return usuarioService.buscarPorEmailOpcional(principal.getUsername())
                .orElseThrow(SessaoInvalidaException::new);
    }

    @GetMapping("/perfil")
    public ModelAndView perfil(@AuthenticationPrincipal UserDetails principal) {
        Usuario u = logado(principal);
        ModelAndView mv = new ModelAndView("perfil");
        mv.addObject("usuario", u);
        mv.addObject("metodos", metodoService.listar(u));
        mv.addObject("tiposPagamento", TipoPagamento.values());
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

    @PostMapping("/perfil/metodo")
    public String adicionarMetodo(@RequestParam TipoPagamento tipo,
                                  @RequestParam(required = false) String descricao,
                                  @AuthenticationPrincipal UserDetails principal) {
        try {
            metodoService.adicionar(logado(principal), tipo, descricao);
        } catch (RegraNegocioException e) {
            return "redirect:/perfil?erroMetodo";
        }
        return "redirect:/perfil?metodoOk";
    }

    @PostMapping("/perfil/metodo/{id}/excluir")
    public String removerMetodo(@PathVariable Long id,
                                @AuthenticationPrincipal UserDetails principal) {
        try {
            metodoService.remover(id, logado(principal));
        } catch (RuntimeException ignored) {
            // método já removido ou de outro usuário: ignora
        }
        return "redirect:/perfil";
    }
}
