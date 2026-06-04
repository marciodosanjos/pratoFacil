package com.example.vendeFacil;

import com.example.vendeFacil.model.Cardapio;
import com.example.vendeFacil.model.Loja;
import com.example.vendeFacil.model.Role;
import com.example.vendeFacil.model.Usuario;
import com.example.vendeFacil.repository.CardapioRepository;
import com.example.vendeFacil.repository.LojaRepository;
import com.example.vendeFacil.repository.PedidoRepository;
import com.example.vendeFacil.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Testes de integracao da API REST (camada HTTP -> service -> banco H2),
// cobrindo seguranca, papeis e o tratamento padronizado de erros.
@SpringBootTest
@AutoConfigureMockMvc
class ApiIntegrationTests {

    @Autowired private MockMvc mockMvc;
    @Autowired private CardapioRepository cardapioRepository;
    @Autowired private PedidoRepository pedidoRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private LojaRepository lojaRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setup() {
        pedidoRepository.deleteAll();
        cardapioRepository.deleteAll();
        // Admin de teste com uma loja (o cadastro de prato resolve a loja do admin logado)
        if (!usuarioRepository.existsByEmail("admin@test.com")) {
            Loja loja = lojaRepository.save(new Loja("Loja Teste", "loja de testes"));
            Usuario admin = new Usuario();
            admin.setNome("Admin Teste");
            admin.setEmail("admin@test.com");
            admin.setSenha(passwordEncoder.encode("1234"));
            admin.setRole(Role.ADMIN);
            admin.setLoja(loja);
            usuarioRepository.save(admin);
        }
    }

    @Test
    void listarCardapioEhPublico() throws Exception {
        mockMvc.perform(get("/api/pratos"))
                .andExpect(status().isOk());
    }

    @Test
    void criarPratoSemAutenticacaoRetorna401() throws Exception {
        mockMvc.perform(post("/api/pratos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"X\",\"preco\":1}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    void adminCriaPratoRetorna201() throws Exception {
        mockMvc.perform(post("/api/pratos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Feijoada\",\"descricao\":\"Completa\",\"preco\":39.90}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nome").value("Feijoada"));
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    void pratoSemNomeRetorna400() throws Exception {
        mockMvc.perform(post("/api/pratos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"descricao\":\"sem nome\",\"preco\":10}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void buscarPratoInexistenteRetorna404() throws Exception {
        mockMvc.perform(get("/api/pratos/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @WithMockUser(username = "cliente@test.com", roles = "CLIENTE")
    void clienteCriaPedidoComTotalCalculadoNoServidor() throws Exception {
        // O controller resolve o cliente pelo e-mail do principal autenticado.
        Usuario cliente = new Usuario();
        cliente.setNome("Cliente Teste");
        cliente.setEmail("cliente@test.com");
        cliente.setSenha(passwordEncoder.encode("1234"));
        cliente.setRole(Role.CLIENTE);
        usuarioRepository.save(cliente);

        Loja loja = lojaRepository.save(new Loja("Loja do Pedido", ""));
        Cardapio prato = new Cardapio();
        prato.setNome("Feijoada");
        prato.setPreco(39.90);
        prato.setLoja(loja);
        prato = cardapioRepository.save(prato);

        String body = "{\"cardapios\":[{\"id\":" + prato.getId() + "}]}";
        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.valorTotal").value(39.9))
                .andExpect(jsonPath("$.status").value("EM_PREPARO"));
    }
}
