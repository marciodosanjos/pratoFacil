package com.example.vendeFacil.controller;

import com.example.vendeFacil.model.Cardapio;
import com.example.vendeFacil.model.Loja;
import com.example.vendeFacil.service.CardapioService;
import com.example.vendeFacil.service.LojaService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;

import java.util.Comparator;
import java.util.List;

// Vitrine de lojas (o que o cliente vê ao entrar) e o cardápio de cada loja.
@Controller
public class LojaController {

    private final LojaService lojaService;
    private final CardapioService cardapioService;

    public LojaController(LojaService lojaService, CardapioService cardapioService) {
        this.lojaService = lojaService;
        this.cardapioService = cardapioService;
    }

    @GetMapping("/lojas")
    public ModelAndView vitrine() {
        ModelAndView mv = new ModelAndView("lojas");
        mv.addObject("lojas", lojaService.listar());
        return mv;
    }

    @GetMapping("/lojas/{id}")
    public ModelAndView cardapioDaLoja(@PathVariable Long id) {
        Loja loja = lojaService.buscarPorId(id);
        // Lista ordenada por categoria (para exibir os itens agrupados, com um
        // indice global continuo usado no formulario do pedido).
        List<Cardapio> pratos = cardapioService.listarPorLoja(loja);
        pratos.sort(Comparator.comparingInt(
                p -> p.getCategoria() != null ? p.getCategoria().ordinal() : 999));

        ModelAndView mv = new ModelAndView("pratos");
        mv.addObject("loja", loja);
        mv.addObject("pratos", pratos);
        return mv;
    }
}
