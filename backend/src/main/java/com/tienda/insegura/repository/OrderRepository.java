package com.tienda.insegura.repository;

import com.tienda.insegura.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // A01: existe el metodo correcto para filtrar por dueno...
    List<Order> findByUsuarioId(Long usuarioId);

    // ...pero OrderController usa findById() (heredado, sin filtrar por
    // usuario) para el endpoint GET /api/pedidos/{id}, lo que permite
    // que cualquier usuario autenticado consulte pedidos ajenos con solo
    // cambiar el id en la URL (IDOR).
}
