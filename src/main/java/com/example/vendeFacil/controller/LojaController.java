package com.example.vendeFacil.controller;

import com.example.vendeFacil.model.Cardapio;
import com.example.vendeFacil.model.Categoria;
import com.example.vendeFacil.model.Loja;
import com.example.vendeFacil.service.CardapioService;
import com.example.vendeFacil.service.LojaService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
        List<Cardapio> pratos = cardapioService.listarPorLoja(loja);

        // Agrupa os pratos por categoria, na ordem do enum (categorias vazias sao omitidas).
        Map<Categoria, List<Cardapio>> porCategoria = new LinkedHashMap<>();
        for (Categoria cat : Categoria.values()) {
            List<Cardapio> doCat = new ArrayList<>();
            for (Cardapio p : pratos) {
                if (p.getCategoria() == cat) {
                    doCat.add(p);
                }
            }
            if (!doCat.isEmpty()) {
                porCategoria.put(cat, doCat);
            }
        }
        // Pratos sem categoria (dados antigos) vao para "Outros".
        List<Cardapio> semCategoria = new ArrayList<>();
        for (Cardapio p : pratos) {
            if (p.getCategoria() == null) {
                semCategoria.add(p);
            }
        }
        if (!semCategoria.isEmpty()) {
            porCategoria.computeIfAbsent(Categoria.OUTROS, k -> new ArrayList<>()).addAll(semCategoria);
        }

        ModelAndView mv = new ModelAndView("pratos");
        mv.addObject("loja", loja);
        mv.addObject("porCategoria", porCategoria);
        return mv;
    }
}
