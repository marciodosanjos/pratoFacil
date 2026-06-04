package com.example.vendeFacil.service;

import com.example.vendeFacil.exception.RecursoNaoEncontradoException;
import com.example.vendeFacil.exception.RegraNegocioException;
import com.example.vendeFacil.model.Loja;
import com.example.vendeFacil.repository.LojaRepository;
import com.example.vendeFacil.util.ImagemUtil;
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

    // Atualiza o nome e, se enviada, a logo (padronizada para 400x400) da loja.
    public void atualizar(Loja loja, String nome, byte[] novaLogoOriginal) {
        if (nome == null || nome.isBlank()) {
            throw new RegraNegocioException("O nome da loja é obrigatório");
        }
        loja.setNome(nome);
        if (novaLogoOriginal != null && novaLogoOriginal.length > 0) {
            try {
                loja.setLogo(ImagemUtil.paraQuadrado400(novaLogoOriginal));
                loja.setLogoTipo("image/png");
            } catch (IllegalArgumentException e) {
                throw new RegraNegocioException("Logo inválida: envie uma imagem (PNG ou JPG)");
            }
        }
        repository.save(loja);
    }
}
