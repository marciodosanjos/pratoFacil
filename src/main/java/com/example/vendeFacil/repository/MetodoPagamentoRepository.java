package com.example.vendeFacil.repository;

import com.example.vendeFacil.model.MetodoPagamento;
import com.example.vendeFacil.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MetodoPagamentoRepository extends JpaRepository<MetodoPagamento, Long> {
    List<MetodoPagamento> findByUsuarioOrderById(Usuario usuario);
}
