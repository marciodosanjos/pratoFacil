package com.example.vendeFacil.controller;
import com.example.vendeFacil.model.Cardapio;
import com.example.vendeFacil.repository.CardapioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class CardapioController {

    @Autowired
    private CardapioRepository repository;

    @PostMapping("/admin/pratos/registrar")
    public String registrarCardapio(@ModelAttribute Cardapio c) {
        repository.save(c);
        return "redirect:/pratos";
    }

    // 2. Rota RECEBE dados enviados via HTML
    // Como os dados vêm de um form tradicional, usamos @ModelAttribute
    @GetMapping("/admin/pratos/registrar")
    public ModelAndView exibirFormCardapio() {
        ModelAndView mv = new ModelAndView("registrar-prato");
        mv.addObject("pratoObjeto", new Cardapio());
        return mv;
    }

    @GetMapping("/pratos")
    public ModelAndView verCardapio() {
        ModelAndView mv = new ModelAndView("pratos");
        mv.addObject("pratos", repository.findAll());
        return mv;
    }

    @GetMapping("/pratos/{id}")
    public ModelAndView editarPratoForm(@PathVariable("id") int id) {
        ModelAndView mv = new ModelAndView("editar-prato");

        // Busca o prato ou redireciona se não achar
        Cardapio prato = repository.findById((long) id)
                .orElseThrow(() -> new IllegalArgumentException("Prato inválido:" + id));

        mv.addObject("prato", prato);
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
        mv.addObject("itens", repository.findAll());
        return mv;
    }

    @GetMapping("/admin/pratos/editar/{id}")
    public ModelAndView editarPratoForm(@PathVariable("id") long id) { // ◄ Verifique se o "id" está aqui
        ModelAndView mv = new ModelAndView("editar-prato");

        // Convertemos para Long aqui dentro para o findById, se seu repository exigir Long
        Cardapio prato = repository.findById((long) id)
                .orElseThrow(() -> new IllegalArgumentException("Prato inválido: " + id));

        mv.addObject("prato", prato);
        return mv;
    }

    @PostMapping("/admin/pratos/editar")
    public String atualizarPrato(@ModelAttribute Cardapio cardapio) {
        repository.save(cardapio);
        return "redirect:/admin/pratos";
    }

    // Rota para deletar um prato do cardápio pelo ID
    @PostMapping("/admin/pratos/deletar/{id}")
    public String deletarPrato(@PathVariable("id") int id) {
        repository.deleteById((long) id);
        return "redirect:/admin/pratos";
    }

}
