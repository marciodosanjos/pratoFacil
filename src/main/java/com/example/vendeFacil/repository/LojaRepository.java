package com.example.vendeFacil.repository;

import com.example.vendeFacil.model.Loja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LojaRepository extends JpaRepository<Loja, Long> {
    boolean existsByNome(String nome);
}
