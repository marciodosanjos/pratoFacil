package com.example.vendeFacil.repository;

import com.example.vendeFacil.model.Loja;
import com.example.vendeFacil.model.Pedido;
import com.example.vendeFacil.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    // Pedidos de um cliente, do mais recente para o mais antigo.
    List<Pedido> findByClienteOrderByIdDesc(Usuario cliente);

    // Pedidos recebidos por uma loja (visao do empreendedor).
    List<Pedido> findByLojaOrderByIdDesc(Loja loja);

    // Pedido pela cobranca do Asaas (usado pelo webhook).
    Optional<Pedido> findByAsaasPaymentId(String asaasPaymentId);
}
