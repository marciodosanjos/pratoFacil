package com.example.vendeFacil.controller;

import com.example.vendeFacil.model.Pedido;
import com.example.vendeFacil.model.Status;
import com.example.vendeFacil.service.CardapioService;
import com.example.vendeFacil.service.PedidoService;
import com.example.vendeFacil.service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

// Dashboard do empreendedor: resumo do negócio (faturamento, pedidos por status,
// cardápio e clientes). Protegido por /admin/** (somente ADMIN).
@Controller
public class DashboardController {

    private final PedidoService pedidoService;
    private final CardapioService cardapioService;
    private final UsuarioService usuarioService;

    public DashboardController(PedidoService pedidoService,
                              CardapioService cardapioService,
                              UsuarioService usuarioService) {
        this.pedidoService = pedidoService;
        this.cardapioService = cardapioService;
        this.usuarioService = usuarioService;
    }

    @GetMapping("/admin/dashboard")
    public ModelAndView dashboard() {
        List<Pedido> pedidos = pedidoService.listar();

        double faturamento = pedidos.stream()
                .filter(p -> p.getValorTotal() != null)
                .mapToDouble(Pedido::getValorTotal)
                .sum();
        long emPreparo = pedidos.stream().filter(p -> p.getStatus() == Status.EM_PREPARO).count();
        long saiuEntrega = pedidos.stream().filter(p -> p.getStatus() == Status.SAIU_PARA_ENTREGA).count();
        long entregue = pedidos.stream().filter(p -> p.getStatus() == Status.ENTREGUE).count();

        ModelAndView mv = new ModelAndView("dashboard");
        mv.addObject("totalPedidos", pedidos.size());
        mv.addObject("faturamento", faturamento);
        mv.addObject("emPreparo", emPreparo);
        mv.addObject("saiuEntrega", saiuEntrega);
        mv.addObject("entregue", entregue);
        mv.addObject("totalPratos", cardapioService.listar().size());
        mv.addObject("clientes", usuarioService.contarClientes());
        return mv;
    }
}
