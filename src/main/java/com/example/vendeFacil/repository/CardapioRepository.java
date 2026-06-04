package com.example.vendeFacil.repository;

import com.example.vendeFacil.model.Cardapio;
import com.example.vendeFacil.model.Loja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardapioRepository extends JpaRepository<Cardapio, Long> {

    // Pratos de uma loja específica.
    List<Cardapio> findByLoja(Loja loja);

    long countByLoja(Loja loja);
}
