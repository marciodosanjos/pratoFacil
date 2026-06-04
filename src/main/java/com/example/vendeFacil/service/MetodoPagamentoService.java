package com.example.vendeFacil.service;

import com.example.vendeFacil.exception.RecursoNaoEncontradoException;
import com.example.vendeFacil.exception.RegraNegocioException;
import com.example.vendeFacil.model.MetodoPagamento;
import com.example.vendeFacil.model.TipoPagamento;
import com.example.vendeFacil.model.Usuario;
import com.example.vendeFacil.repository.MetodoPagamentoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

// Métodos de pagamento salvos pelo cliente (rótulos para a simulação).
@Service
public class MetodoPagamentoService {

    private final MetodoPagamentoRepository repository;

    public MetodoPagamentoService(MetodoPagamentoRepository repository) {
        this.repository = repository;
    }

    public List<MetodoPagamento> listar(Usuario usuario) {
        return repository.findByUsuarioOrderById(usuario);
    }

    public void adicionar(Usuario usuario, TipoPagamento tipo, String descricao) {
        if (tipo == null) {
            throw new RegraNegocioException("Escolha o tipo de pagamento");
        }
        MetodoPagamento m = new MetodoPagamento();
        m.setUsuario(usuario);
        m.setTipo(tipo);
        m.setDescricao((descricao == null || descricao.isBlank()) ? tipo.getDescricao() : descricao.trim());
        repository.save(m);
    }

    // Remove um método garantindo que ele pertence ao usuário informado.
    public void remover(Long id, Usuario usuario) {
        MetodoPagamento m = repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Método não encontrado: " + id));
        if (m.getUsuario() == null || usuario == null
                || !m.getUsuario().getId().equals(usuario.getId())) {
            throw new RecursoNaoEncontradoException("Método não encontrado: " + id);
        }
        repository.delete(m);
    }
}
