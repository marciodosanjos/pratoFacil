package com.example.vendeFacil.controller;

import com.example.vendeFacil.dto.PedidoRequest;
import com.example.vendeFacil.exception.RegraNegocioException;
import com.example.vendeFacil.model.Pedido;
import com.example.vendeFacil.model.Status;
import com.example.vendeFacil.model.Usuario;
import com.example.vendeFacil.service.PagamentoService;
import com.example.vendeFacil.service.PedidoService;
import com.example.vendeFacil.service.UsuarioService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

// Controller Web (Thymeleaf) dos pedidos: área do cliente (seus pedidos +
// acompanhamento) e área do empreendedor (pedidos da SUA loja + gestão de status).
@Controller
public class PedidoController {

    private final PedidoService service;
    private final UsuarioService usuarioService;
    private final PagamentoService pagamentoService;

    public PedidoController(PedidoService service, UsuarioService usuarioService, PagamentoService pagamentoService) {
        this.service = service;
        this.usuarioService = usuarioService;
        this.pagamentoService = pagamentoService;
    }

    private Usuario logado(UserDetails principal) {
        return usuarioService.buscarPorEmailOpcional(principal.getUsername())
                .orElseThrow(com.example.vendeFacil.exception.SessaoInvalidaException::new);
    }

    // Cliente confirma o pedido (a partir do cardápio de uma loja).
    @PostMapping("/novo-pedido")
    public String novoPedido(@ModelAttribute PedidoRequest request,
                             @RequestParam(value = "lojaId", required = false) Long lojaId,
                             @AuthenticationPrincipal UserDetails principal) {
        try {
            Usuario cliente = logado(principal);
            Pedido salvo = service.criar(request, cliente);
            pagamentoService.gerarCobranca(salvo, cliente);
            // Após confirmar o pedido, o cliente vai para a tela de pagamento.
            return "redirect:/meus-pedidos/" + salvo.getId() + "/pagamento";
        } catch (RegraNegocioException e) {
            return lojaId != null ? "redirect:/lojas/" + lojaId + "?vazio" : "redirect:/lojas";
        }
    }

    @GetMapping("/pedido-sucesso")
    public String telaSucesso() {
        return "pedido-sucesso";
    }

    // Área do cliente: somente os seus próprios pedidos.
    @GetMapping("/meus-pedidos")
    public ModelAndView listarMeusPedidos(@AuthenticationPrincipal UserDetails principal) {
        ModelAndView mv = new ModelAndView("lista-pedidos");
        mv.addObject("pedidos", service.listarDoCliente(logado(principal)));
        return mv;
    }

    // Página de acompanhamento (timeline) de um pedido do próprio cliente.
    @GetMapping("/meus-pedidos/{id}")
    public ModelAndView acompanhar(@PathVariable Long id,
                                   @AuthenticationPrincipal UserDetails principal) {
        Pedido pedido = service.buscarDoCliente(id, logado(principal));
        ModelAndView mv = new ModelAndView("acompanhar");
        mv.addObject("pedido", pedido);
        mv.addObject("statusList", Status.values());
        return mv;
    }

    // Tela de pagamento PIX do pedido (QR Code + copia-e-cola), dentro do app.
    @GetMapping("/meus-pedidos/{id}/pagamento")
    public ModelAndView pagamento(@PathVariable Long id,
                                  @AuthenticationPrincipal UserDetails principal) {
        Pedido pedido = service.buscarDoCliente(id, logado(principal));
        ModelAndView mv = new ModelAndView("pagamento");
        mv.addObject("pedido", pedido);
        mv.addObject("pix", pagamentoService.obterPix(pedido));
        return mv;
    }

    // Confirma o pagamento na tela do app (PIX ou cartão). Em sandbox a
    // confirmação é simulada; com a chave Asaas o PIX real também confirma
    // sozinho via webhook.
    @PostMapping("/meus-pedidos/{id}/pagamento/confirmar")
    public String confirmarPagamento(@PathVariable Long id,
                                     @RequestParam("metodo") com.example.vendeFacil.model.TipoPagamento metodo,
                                     @AuthenticationPrincipal UserDetails principal) {
        Pedido pedido = service.buscarDoCliente(id, logado(principal));
        pagamentoService.confirmarManual(pedido, metodo);
        return "redirect:/meus-pedidos/" + id + "/pagamento?pago";
    }

    // Área do empreendedor: pedidos recebidos pela SUA loja + gestão de status (RF03).
    @GetMapping("/admin/pedidos")
    public ModelAndView adminPedidos(@AuthenticationPrincipal UserDetails principal) {
        ModelAndView mv = new ModelAndView("admin-pedidos");
        mv.addObject("pedidos", service.listarDaLoja(logado(principal).getLoja()));
        mv.addObject("statusList", Status.values());
        return mv;
    }

    // Detalhe de um pedido recebido pela loja do empreendedor.
    @GetMapping("/admin/pedidos/{id}")
    public ModelAndView adminPedidoDetalhe(@PathVariable Long id,
                                           @AuthenticationPrincipal UserDetails principal) {
        Pedido pedido = service.buscarDaLoja(id, logado(principal).getLoja());
        ModelAndView mv = new ModelAndView("admin-pedido-detalhe");
        mv.addObject("pedido", pedido);
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
