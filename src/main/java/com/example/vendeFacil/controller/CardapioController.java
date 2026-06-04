package com.example.vendeFacil.controller;

import com.example.vendeFacil.model.Cardapio;
import com.example.vendeFacil.service.CardapioService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

// Controller Web (Thymeleaf) do cardápio. Renderiza páginas HTML para uso
// direto no navegador, reaproveitando a mesma regra de negócio do CardapioService.
@Controller
public class CardapioController {

    private final CardapioService service;

    public CardapioController(CardapioService service) {
        this.service = service;
    }

    @PostMapping("/admin/pratos/registrar")
    public String registrarCardapio(@ModelAttribute Cardapio c) {
        service.criar(c);
        return "redirect:/pratos";
    }

    // Exibe o formulário de cadastro de prato
    @GetMapping("/admin/pratos/registrar")
    public ModelAndView exibirFormCardapio() {
        ModelAndView mv = new ModelAndView("registrar-prato");
        mv.addObject("pratoObjeto", new Cardapio());
        return mv;
    }

    @GetMapping("/pratos")
    public ModelAndView verCardapio() {
        ModelAndView mv = new ModelAndView("pratos");
        mv.addObject("pratos", service.listar());
        return mv;
    }

    @GetMapping("/pratos/{id}")
    public ModelAndView editarPratoForm(@PathVariable("id") Long id) {
        ModelAndView mv = new ModelAndView("editar-prato");
        mv.addObject("prato", service.buscarPorId(id));
        return mv;
    }

    // Rotas para área administrativa do cardápio
    @GetMapping("/admin")
    public String homeAdmin() {
        return "admin-home";
    }

    @GetMapping("/admin/pratos")
    public ModelAndView adminCardapio() {
        ModelAndView mv = new ModelAndView("admin");
        mv.addObject("itens", service.listar());
        return mv;
    }

    @GetMapping("/admin/pratos/editar/{id}")
    public ModelAndView editarPratoFormAdmin(@PathVariable("id") Long id) {
        ModelAndView mv = new ModelAndView("editar-prato");
        mv.addObject("prato", service.buscarPorId(id));
        return mv;
    }

    @PostMapping("/admin/pratos/editar")
    public String atualizarPrato(@ModelAttribute Cardapio cardapio) {
        service.atualizar(cardapio.getId(), cardapio);
        return "redirect:/admin/pratos";
    }

    // Rota para deletar um prato do cardápio pelo ID
    @PostMapping("/admin/pratos/deletar/{id}")
    public String deletarPrato(@PathVariable("id") Long id) {
        service.deletar(id);
        return "redirect:/admin/pratos";
    }
}
