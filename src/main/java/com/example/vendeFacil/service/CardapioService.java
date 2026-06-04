package com.example.vendeFacil.service;

import com.example.vendeFacil.exception.RecursoNaoEncontradoException;
import com.example.vendeFacil.exception.RegraNegocioException;
import com.example.vendeFacil.model.Cardapio;
import com.example.vendeFacil.repository.CardapioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

// Regras de negócio do cardápio. Centraliza validação e acesso ao repositório
// para que os controllers (REST e Web) não dupliquem lógica.
@Service
public class CardapioService {

    private final CardapioRepository repository;

    public CardapioService(CardapioRepository repository) {
        this.repository = repository;
    }

    public List<Cardapio> listar() {
        return repository.findAll();
    }

    public Cardapio buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Prato não encontrado: " + id));
    }

    public Cardapio criar(Cardapio cardapio) {
        validar(cardapio);
        cardapio.setId(null); // garante criação (e não atualização acidental)
        return repository.save(cardapio);
    }

    public Cardapio atualizar(Long id, Cardapio dados) {
        Cardapio prato = buscarPorId(id);
        validar(dados);
        prato.setNome(dados.getNome());
        prato.setDescricao(dados.getDescricao());
        prato.setPreco(dados.getPreco());
        return repository.save(prato);
    }

    public void deletar(Long id) {
        if (!repository.existsById(id)) {
            throw new RecursoNaoEncontradoException("Prato não encontrado: " + id);
        }
        repository.deleteById(id);
    }

    // Validação simples dos dados recebidos -> 400 Bad Request
    private void validar(Cardapio c) {
        if (c.getNome() == null || c.getNome().isBlank()) {
            throw new RegraNegocioException("O nome do prato é obrigatório");
        }
        if (c.getPreco() == null || c.getPreco() < 0) {
            throw new RegraNegocioException("O preço deve ser informado e não pode ser negativo");
        }
    }
}
