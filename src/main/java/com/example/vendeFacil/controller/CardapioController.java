package com.example.vendeFacil.controller;

import com.example.vendeFacil.model.Cardapio;
import com.example.vendeFacil.model.Categoria;
import com.example.vendeFacil.model.Usuario;
import com.example.vendeFacil.service.CardapioService;
import com.example.vendeFacil.service.UsuarioService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

// Controller Web (Thymeleaf) do cardápio. Gestão é escopada à loja do admin logado.
@Controller
public class CardapioController {

    private final CardapioService service;
    private final UsuarioService usuarioService;

    public CardapioController(CardapioService service, UsuarioService usuarioService) {
        this.service = service;
        this.usuarioService = usuarioService;
    }

    private Usuario admin(UserDetails principal) {
        return usuarioService.buscarPorEmailOpcional(principal.getUsername())
                .orElseThrow(com.example.vendeFacil.exception.SessaoInvalidaException::new);
    }

    @PostMapping("/admin/pratos/registrar")
    public String registrarCardapio(@ModelAttribute Cardapio c,
                                    @AuthenticationPrincipal UserDetails principal) {
        service.criar(c, admin(principal).getLoja());
        return "redirect:/admin/pratos";
    }

    @GetMapping("/admin/pratos/registrar")
    public ModelAndView exibirFormCardapio() {
        ModelAndView mv = new ModelAndView("registrar-prato");
        mv.addObject("pratoObjeto", new Cardapio());
        mv.addObject("categorias", Categoria.values());
        return mv;
    }

    // A antiga página única de cardápio dá lugar à vitrine de lojas.
    @GetMapping("/pratos")
    public String redirecionarParaLojas() {
        return "redirect:/lojas";
    }

    @GetMapping("/admin")
    public ModelAndView homeAdmin(@AuthenticationPrincipal UserDetails principal) {
        Usuario a = admin(principal);
        ModelAndView mv = new ModelAndView("admin-home");
        mv.addObject("loja", a.getLoja());
        mv.addObject("nomeLoja", a.getLoja() != null ? a.getLoja().getNome() : "");
        return mv;
    }

    @GetMapping("/admin/pratos")
    public ModelAndView adminCardapio(@AuthenticationPrincipal UserDetails principal) {
        ModelAndView mv = new ModelAndView("admin");
        mv.addObject("itens", service.listarPorLoja(admin(principal).getLoja()));
        return mv;
    }

    @GetMapping("/admin/pratos/editar/{id}")
    public ModelAndView editarPratoFormAdmin(@PathVariable("id") Long id) {
        ModelAndView mv = new ModelAndView("editar-prato");
        mv.addObject("prato", service.buscarPorId(id));
        mv.addObject("categorias", Categoria.values());
        return mv;
    }

    @PostMapping("/admin/pratos/editar")
    public String atualizarPrato(@ModelAttribute Cardapio cardapio) {
        service.atualizar(cardapio.getId(), cardapio);
        return "redirect:/admin/pratos";
    }

    @PostMapping("/admin/pratos/deletar/{id}")
    public String deletarPrato(@PathVariable("id") Long id) {
        service.deletar(id);
        return "redirect:/admin/pratos";
    }
}
