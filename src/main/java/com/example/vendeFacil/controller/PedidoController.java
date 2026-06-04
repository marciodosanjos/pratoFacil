package com.example.vendeFacil.controller;

import com.example.vendeFacil.model.Pedido;
import com.example.vendeFacil.model.Status;
import com.example.vendeFacil.service.PedidoService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

// Controller Web (Thymeleaf) dos pedidos. Renderiza páginas HTML para uso
// direto no navegador, reaproveitando a mesma regra de negócio do PedidoService.
@Controller
public class PedidoController {

    private final PedidoService service;

    public PedidoController(PedidoService service) {
        this.service = service;
    }

    @PostMapping("/novo-pedido")
    public String novoPedido(@ModelAttribute Pedido pedido) {
        service.criar(pedido);
        return "redirect:/pedido-sucesso";
    }

    @GetMapping("/pedido-sucesso")
    public String telaSucesso() {
        return "pedido-sucesso";
    }

    @GetMapping("/meus-pedidos")
    public ModelAndView listarMeusPedidos() {
        ModelAndView mv = new ModelAndView("lista-pedidos");
        mv.addObject("pedidos", service.listar());
        return mv;
    }

    // Rota para atualizar apenas o status de um pedido específico
    @PostMapping("/meus-pedidos/atualizar-status/{id}")
    public String atualizarStatusPedido(@PathVariable("id") Long id, @RequestParam("status") Status novoStatus) {
        service.atualizarStatus(id, novoStatus);
        return "redirect:/meus-pedidos";
    }
}
