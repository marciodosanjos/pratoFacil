package com.example.vendeFacil.service;

import com.example.vendeFacil.exception.RegraNegocioException;
import com.example.vendeFacil.model.Role;
import com.example.vendeFacil.model.Usuario;
import com.example.vendeFacil.repository.UsuarioRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

// Regras de negocio de usuarios + integracao com o Spring Security.
// Implementa UserDetailsService para que o login (por e-mail) seja autenticado
// contra o banco, e oferece o cadastro de novos clientes.
@Service
public class UsuarioService implements UserDetailsService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    // Usado pelo Spring Security durante o login.
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario u = repository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));
        return new User(u.getEmail(), u.getSenha(),
                List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole().name())));
    }

    // Cadastro de um novo cliente (sempre com papel CLIENTE).
    public Usuario registrarCliente(String nome, String email, String senhaPura) {
        if (nome == null || nome.isBlank()) {
            throw new RegraNegocioException("O nome é obrigatório");
        }
        if (email == null || email.isBlank()) {
            throw new RegraNegocioException("O e-mail é obrigatório");
        }
        if (senhaPura == null || senhaPura.length() < 4) {
            throw new RegraNegocioException("A senha deve ter ao menos 4 caracteres");
        }
        if (repository.existsByEmail(email)) {
            throw new RegraNegocioException("Já existe uma conta com este e-mail");
        }

        Usuario u = new Usuario();
        u.setNome(nome);
        u.setEmail(email);
        u.setSenha(passwordEncoder.encode(senhaPura));
        u.setRole(Role.CLIENTE);
        return repository.save(u);
    }

    public Usuario buscarPorEmail(String email) {
        return repository.findByEmail(email)
                .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado: " + email));
    }

    // Quantidade de clientes cadastrados (para o dashboard).
    public long contarClientes() {
        return repository.countByRole(Role.CLIENTE);
    }
}
