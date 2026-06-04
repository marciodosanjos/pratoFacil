package com.example.vendeFacil.service;

import com.example.vendeFacil.exception.RecursoNaoEncontradoException;
import com.example.vendeFacil.model.Loja;
import com.example.vendeFacil.repository.LojaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

// Regras de negócio das lojas do marketplace.
@Service
public class LojaService {

    private final LojaRepository repository;

    public LojaService(LojaRepository repository) {
        this.repository = repository;
    }

    public List<Loja> listar() {
        return repository.findAll();
    }

    public Loja buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Loja não encontrada: " + id));
    }
}
