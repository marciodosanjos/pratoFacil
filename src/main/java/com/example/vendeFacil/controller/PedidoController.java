package com.example.vendeFacil.controller;

import com.example.vendeFacil.model.Pedido;
import com.example.vendeFacil.model.Cardapio;
import com.example.vendeFacil.model.Status;
import com.example.vendeFacil.repository.PedidoRepository;
import com.example.vendeFacil.repository.CardapioRepository; // ◄ IMPORTANTE: Importe seu repository de cardápio
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import java.util.ArrayList;
import java.util.List;

@Controller
public class PedidoController {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private CardapioRepository cardapioRepository; // ◄ Injete o repositório do cardápio

    @PostMapping("/novo-pedido")
    public String novoPedido(@ModelAttribute Pedido pedido) {

        // 1. Garante que o status inicial seja EM_PREPARO
        if (pedido.getStatus() == null) {
            pedido.setStatus(Status.EM_PREPARO);
        }

        // 2. Define um nome padrão/identificador para o pedido já que o form não envia um campo de texto "nome"
        if (pedido.getNome() == null || pedido.getNome().trim().isEmpty()) {
            pedido.setNome("Pedido #" + (System.currentTimeMillis() % 10000));
        }

        double totalCalculado = 0.0;

        // 3. Verifica se vieram itens selecionados do formulário HTML
        if (pedido.getCardapios() != null && !pedido.getCardapios().isEmpty()) {
            List<Cardapio> listaItensCompletos = new ArrayList<>();

            for (Cardapio itemSimplificado : pedido.getCardapios()) {
                // 1. Validação corrigida para int primitivo
                if (itemSimplificado != null && itemSimplificado.getId() != 0) {

                    // 2. CORREÇÃO AQUI: Fazemos o cast explícito transformando o int em Long
                    Long idLong = Long.valueOf(itemSimplificado.getId());

                    // 3. Agora o findById aceita o parâmetro perfeitamente sem erros!
                    cardapioRepository.findById(idLong).ifPresent(pratoDoBanco -> {
                        listaItensCompletos.add(pratoDoBanco);
                    });
                }
            }

            // Atualiza a lista do pedido com os objetos persistidos completos para salvar na tabela intermediária
            pedido.setCardapios(listaItensCompletos);

            // Soma o preço de cada prato encontrado na lista
            for (Cardapio prato : listaItensCompletos) {
                if (prato.getPreco() != null) {
                    totalCalculado += prato.getPreco();
                }
            }
        }

        // 4. Atribui o valor real somado ao invés do 0.0 fixo
        pedido.setValorTotal(totalCalculado);

        // Salva o pedido preenchido com valores e relacionamentos corretos
        pedidoRepository.save(pedido);

        return "redirect:/pedido-sucesso";
    }

    @GetMapping("/pedido-sucesso")
    public String telaSucesso() {
        return "pedido-sucesso";
    }

    @GetMapping("/meus-pedidos")
    public ModelAndView listarMeusPedidos() {
        ModelAndView mv = new ModelAndView("lista-pedidos");
        mv.addObject("pedidos", pedidoRepository.findAll());
        return mv;
    }

    // Rota para atualizar apenas o status de um pedido específico
    @PostMapping("/meus-pedidos/atualizar-status/{id}")
    public String atualizarStatusPedido(@PathVariable("id") int id, @RequestParam("status") Status novoStatus) {
        pedidoRepository.findById((long) id).ifPresent(pedido -> {
            pedido.setStatus(novoStatus);
            pedidoRepository.save(pedido);
        });
        return "redirect:/meus-pedidos";
    }

}