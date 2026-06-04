package com.example.vendeFacil.config;

import com.example.vendeFacil.model.Cardapio;
import com.example.vendeFacil.model.Role;
import com.example.vendeFacil.model.Usuario;
import com.example.vendeFacil.repository.CardapioRepository;
import com.example.vendeFacil.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

// Carga inicial: cria um ADMIN padrao e alguns pratos de exemplo na primeira
// execucao, para que o sistema ja abra com conteudo.
//   login admin: admin@pratofacil.com  senha: admin123  (troque em producao!)
@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final CardapioRepository cardapioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UsuarioRepository usuarioRepository,
                           CardapioRepository cardapioRepository,
                           PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.cardapioRepository = cardapioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Usuario ADMIN padrao
        String adminEmail = "admin@pratofacil.com";
        if (!usuarioRepository.existsByEmail(adminEmail)) {
            Usuario admin = new Usuario();
            admin.setNome("Administrador");
            admin.setEmail(adminEmail);
            admin.setSenha(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            usuarioRepository.save(admin);
        }

        // Pratos de exemplo (somente se o cardapio estiver vazio)
        if (cardapioRepository.count() == 0) {
            cardapioRepository.save(prato("Feijoada Completa",
                    "Feijão preto com carnes, arroz, couve e farofa", 39.90));
            cardapioRepository.save(prato("Strogonoff de Frango",
                    "Com arroz branco e batata palha", 29.50));
            cardapioRepository.save(prato("Lasanha à Bolonhesa",
                    "Massa fresca, molho de carne e queijo gratinado", 34.00));
            cardapioRepository.save(prato("Brigadeiro Gourmet (4 un.)",
                    "Sobremesa artesanal", 12.00));
        }
    }

    private Cardapio prato(String nome, String descricao, double preco) {
        Cardapio c = new Cardapio();
        c.setNome(nome);
        c.setDescricao(descricao);
        c.setPreco(preco);
        return c;
    }
}
