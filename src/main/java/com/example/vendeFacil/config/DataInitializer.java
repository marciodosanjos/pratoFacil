package com.example.vendeFacil.config;

import com.example.vendeFacil.model.Cardapio;
import com.example.vendeFacil.model.Categoria;
import com.example.vendeFacil.model.Loja;
import com.example.vendeFacil.model.Role;
import com.example.vendeFacil.model.Usuario;
import com.example.vendeFacil.repository.CardapioRepository;
import com.example.vendeFacil.repository.LojaRepository;
import com.example.vendeFacil.repository.UsuarioRepository;
import com.example.vendeFacil.util.ImagemUtil;
import org.springframework.core.io.ClassPathResource;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

// Carga inicial do marketplace: cria 4 lojas de exemplo, cada uma com seu
// proprio ADMIN (login/senha) e seu cardapio (com categorias).
// So roda se ainda nao houver lojas.
@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final CardapioRepository cardapioRepository;
    private final LojaRepository lojaRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UsuarioRepository usuarioRepository,
                           CardapioRepository cardapioRepository,
                           LojaRepository lojaRepository,
                           PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.cardapioRepository = cardapioRepository;
        this.lojaRepository = lojaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (lojaRepository.count() > 0) {
            return; // ja semeado
        }

        // 1) Comida de Vo  (login: comidadavo@pratofacil.com / vovo123)
        Loja comidaVo = criarLoja("Comida de Vó", "Comida caseira de verdade",
                "comidadavo@pratofacil.com", "vovo123", "comida-de-vo.jpg");
        salvarPratos(comidaVo,
                prato("Feijoada tradicional", "", 39.00, Categoria.PRATOS_PRINCIPAIS),
                prato("Feijoada + torresmo", "", 44.00, Categoria.PRATOS_PRINCIPAIS),
                prato("Prato feito", "Bife, arroz, feijão, fritas e salada", 24.00, Categoria.PRATOS_PRINCIPAIS),
                prato("Frango grelhado", "Com arroz e purê", 22.00, Categoria.PRATOS_PRINCIPAIS),
                prato("Linguiça acebolada", "Com arroz e farofa", 23.00, Categoria.PRATOS_PRINCIPAIS),
                prato("Strogonoff de frango", "", 28.00, Categoria.PRATOS_PRINCIPAIS),
                prato("Omelete completa", "Com arroz e salada", 20.00, Categoria.PRATOS_PRINCIPAIS),
                prato("Macarrão à bolonhesa", "", 21.00, Categoria.PRATOS_PRINCIPAIS),
                prato("Batata frita", "Porção", 16.00, Categoria.PORCOES),
                prato("Torresmo crocante", "Porção", 18.00, Categoria.PORCOES),
                prato("Farofa da casa", "Porção", 8.00, Categoria.PORCOES),
                prato("Vinagrete", "Porção", 6.00, Categoria.PORCOES),
                prato("Pudim de leite condensado", "", 12.00, Categoria.SOBREMESAS),
                prato("Mousse de maracujá", "", 12.00, Categoria.SOBREMESAS),
                prato("Refrigerante lata", "", 6.00, Categoria.BEBIDAS),
                prato("Suco natural", "", 8.00, Categoria.BEBIDAS),
                prato("Água mineral", "", 4.00, Categoria.BEBIDAS),
                prato("Guaraná 1L", "", 10.00, Categoria.BEBIDAS));

        // 2) Maozinha Burger  (login: maozinhaburger@pratofacil.com / burger123)
        Loja burger = criarLoja("Mãozinha Burger", "Hambúrgueres artesanais",
                "maozinhaburger@pratofacil.com", "burger123", "maozinha-burger.jpg");
        salvarPratos(burger,
                prato("Smash Burger", "Pão, carne, queijo e molho da casa", 22.00, Categoria.LANCHES),
                prato("X-Burger", "", 25.00, Categoria.LANCHES),
                prato("X-Salada", "", 27.00, Categoria.LANCHES),
                prato("Bacon Burger", "", 31.00, Categoria.LANCHES),
                prato("Duplo Bacon Burger", "", 38.00, Categoria.LANCHES),
                prato("Chicken Burger", "", 28.00, Categoria.LANCHES),
                prato("Batata frita pequena", "", 12.00, Categoria.PORCOES),
                prato("Batata frita grande", "", 20.00, Categoria.PORCOES),
                prato("Batata com cheddar e bacon", "", 26.00, Categoria.PORCOES),
                prato("Milk-shake (300ml)", "", 16.00, Categoria.SOBREMESAS),
                prato("Sundae", "", 12.00, Categoria.SOBREMESAS),
                prato("Refrigerante lata", "", 6.00, Categoria.BEBIDAS),
                prato("Água mineral", "", 4.00, Categoria.BEBIDAS),
                prato("Suco natural", "", 8.00, Categoria.BEBIDAS));

        // 3) Forno Italiano  (login: fornoitaliano@pratofacil.com / pizza123)
        Loja pizza = criarLoja("Forno Italiano", "Pizzas na pedra",
                "fornoitaliano@pratofacil.com", "pizza123", "forno-italiano.jpg");
        salvarPratos(pizza,
                prato("Pizza Pequena Mussarela", "4 fatias", 25.00, Categoria.PIZZAS),
                prato("Pizza Pequena Calabresa", "4 fatias", 27.00, Categoria.PIZZAS),
                prato("Pizza Pequena Frango c/ catupiry", "4 fatias", 30.00, Categoria.PIZZAS),
                prato("Pizza Média Mussarela", "6 fatias", 40.00, Categoria.PIZZAS),
                prato("Pizza Média Calabresa", "6 fatias", 43.00, Categoria.PIZZAS),
                prato("Pizza Média Portuguesa", "6 fatias", 46.00, Categoria.PIZZAS),
                prato("Pizza Média Frango c/ catupiry", "6 fatias", 48.00, Categoria.PIZZAS),
                prato("Pizza Grande Mussarela", "8 fatias", 55.00, Categoria.PIZZAS),
                prato("Pizza Grande Calabresa", "8 fatias", 58.00, Categoria.PIZZAS),
                prato("Pizza Grande Portuguesa", "8 fatias", 62.00, Categoria.PIZZAS),
                prato("Pizza Grande Frango c/ catupiry", "8 fatias", 65.00, Categoria.PIZZAS),
                prato("Pizza Doce Chocolate", "", 55.00, Categoria.PIZZAS),
                prato("Pizza Doce Romeu e Julieta", "", 58.00, Categoria.PIZZAS),
                prato("Refrigerante 2L", "", 14.00, Categoria.BEBIDAS),
                prato("Refrigerante lata", "", 6.00, Categoria.BEBIDAS),
                prato("Água mineral", "", 4.00, Categoria.BEBIDAS));

        // 4) Imperio do Acai  (login: imperiodoacai@pratofacil.com / acai123)
        Loja acai = criarLoja("Império do Açaí", "Açaí e sorvetes",
                "imperiodoacai@pratofacil.com", "acai123", "imperio-do-acai.jpg");
        String complementos = "Acompanha até 3 complementos grátis: granola, leite condensado, paçoca, banana, morango, leite em pó";
        salvarPratos(acai,
                prato("Açaí 300ml", complementos, 14.00, Categoria.ACAI),
                prato("Açaí 500ml", complementos, 20.00, Categoria.ACAI),
                prato("Açaí 700ml", complementos, 27.00, Categoria.ACAI),
                prato("Complemento adicional", "Extra", 2.00, Categoria.OUTROS),
                prato("Sorvete (1 bola)", "", 6.00, Categoria.SOBREMESAS));
    }

    private Loja criarLoja(String nome, String descricao, String emailAdmin, String senha, String logoArquivo) {
        Loja loja = new Loja(nome, descricao);
        try {
            byte[] original = new ClassPathResource("static/img/lojas/" + logoArquivo).getInputStream().readAllBytes();
            loja.setLogo(ImagemUtil.paraQuadrado400(original));
            loja.setLogoTipo("image/png");
        } catch (Exception e) {
            // se a logo nao puder ser carregada, segue sem ela
        }
        loja = lojaRepository.save(loja);
        if (!usuarioRepository.existsByEmail(emailAdmin)) {
            Usuario admin = new Usuario();
            admin.setNome(nome);
            admin.setEmail(emailAdmin);
            admin.setSenha(passwordEncoder.encode(senha));
            admin.setRole(Role.ADMIN);
            admin.setLoja(loja);
            usuarioRepository.save(admin);
        }
        return loja;
    }

    private void salvarPratos(Loja loja, Cardapio... pratos) {
        for (Cardapio p : pratos) {
            p.setLoja(loja);
            cardapioRepository.save(p);
        }
    }

    private Cardapio prato(String nome, String descricao, double preco, Categoria categoria) {
        Cardapio c = new Cardapio();
        c.setNome(nome);
        c.setDescricao(descricao);
        c.setPreco(preco);
        c.setCategoria(categoria);
        return c;
    }
}
