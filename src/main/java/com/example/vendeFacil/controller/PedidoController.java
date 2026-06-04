package com.example.vendeFacil.controller;

import com.example.vendeFacil.exception.RegraNegocioException;
import com.example.vendeFacil.model.Pedido;
import com.example.vendeFacil.model.Status;
import com.example.vendeFacil.model.Usuario;
import com.example.vendeFacil.service.PedidoService;
import com.example.vendeFacil.service.UsuarioService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

// Controller Web (Thymeleaf) dos pedidos: área do cliente (seus pedidos +
// acompanhamento) e área do empreendedor (todos os pedidos + gestão de status).
@Controller
public class PedidoController {

    private final PedidoService service;
    private final UsuarioService usuarioService;

    public PedidoController(PedidoService service, UsuarioService usuarioService) {
        this.service = service;
        this.usuarioService = usuarioService;
    }

    // Cliente confirma o pedido (a partir do cardápio). Vincula ao usuário logado.
    @PostMapping("/novo-pedido")
    public String novoPedido(@ModelAttribute Pedido pedido,
                             @AuthenticationPrincipal UserDetails principal) {
        Usuario cliente = usuarioService.buscarPorEmail(principal.getUsername());
        try {
            Pedido salvo = service.criar(pedido, cliente);
            return "redirect:/meus-pedidos/" + salvo.getId();
        } catch (RegraNegocioException e) {
            // Nenhum item selecionado: volta ao cardápio sinalizando.
            return "redirect:/pratos?vazio";
        }
    }

    @GetMapping("/pedido-sucesso")
    public String telaSucesso() {
        return "pedido-sucesso";
    }

    // Área do cliente: somente os seus próprios pedidos.
    @GetMapping("/meus-pedidos")
    public ModelAndView listarMeusPedidos(@AuthenticationPrincipal UserDetails principal) {
        Usuario cliente = usuarioService.buscarPorEmail(principal.getUsername());
        ModelAndView mv = new ModelAndView("lista-pedidos");
        mv.addObject("pedidos", service.listarDoCliente(cliente));
        return mv;
    }

    // Página de acompanhamento (timeline) de um pedido do próprio cliente.
    @GetMapping("/meus-pedidos/{id}")
    public ModelAndView acompanhar(@PathVariable Long id,
                                   @AuthenticationPrincipal UserDetails principal) {
        Usuario cliente = usuarioService.buscarPorEmail(principal.getUsername());
        Pedido pedido = service.buscarDoCliente(id, cliente);
        ModelAndView mv = new ModelAndView("acompanhar");
        mv.addObject("pedido", pedido);
        mv.addObject("statusList", Status.values());
        return mv;
    }

    // Área do empreendedor: todos os pedidos + gestão de status (RF03).
    @GetMapping("/admin/pedidos")
    public ModelAndView adminPedidos() {
        ModelAndView mv = new ModelAndView("admin-pedidos");
        mv.addObject("pedidos", service.listar());
        mv.addObject("statusList", Status.values());
        return mv;
    }

    @PostMapping("/admin/pedidos/atualizar-status/{id}")
    public String atualizarStatusPedido(@PathVariable("id") Long id,
                                        @RequestParam("status") Status novoStatus) {
        service.atualizarStatus(id, novoStatus);
        return "redirect:/admin/pedidos";
    }
}
