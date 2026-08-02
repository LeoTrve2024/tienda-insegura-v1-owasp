package com.tienda.insegura.service;

import com.tienda.insegura.model.Order;
import com.tienda.insegura.model.OrderItem;
import com.tienda.insegura.model.Product;
import com.tienda.insegura.model.User;
import com.tienda.insegura.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final CartService cartService;

    public OrderService(OrderRepository orderRepository, ProductService productService, CartService cartService) {
        this.orderRepository = orderRepository;
        this.productService = productService;
        this.cartService = cartService;
    }

    public Order crearPedidoDesdeCarrito(User usuario, String cartId) {
        List<Product> catalogo = productService.listarTodos();
        Map<Long, Integer> carrito = cartService.obtenerCarrito(cartId);

        Order pedido = new Order();
        pedido.setUsuario(usuario);

        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<Long, Integer> entry : carrito.entrySet()) {
            Product producto = catalogo.stream()
                    .filter(p -> p.getId().equals(entry.getKey()))
                    .findFirst()
                    .orElse(null);
            if (producto == null) continue;

            OrderItem item = new OrderItem();
            item.setPedido(pedido);
            item.setProducto(producto);
            // A06: cantidad se acepta tal cual venga del carrito (puede
            // ser negativa, ver CartService).
            item.setCantidad(entry.getValue());
            item.setPrecioUnitario(producto.getPrecio());
            pedido.getItems().add(item);

            total = total.add(producto.getPrecio().multiply(BigDecimal.valueOf(entry.getValue())));

            // A06: tampoco se descuenta/valida el stock real del producto.
        }

        pedido.setTotal(total);
        Order guardado = orderRepository.save(pedido);
        cartService.vaciar(cartId);
        return guardado;
    }

    public List<Order> listarPorUsuario(Long usuarioId) {
        return orderRepository.findByUsuarioId(usuarioId);
    }

    /**
     * VULNERABILIDAD A01:2025 - Broken Access Control (IDOR)
     *
     * Devuelve el pedido por id SIN verificar que pertenezca al usuario
     * autenticado. OrderController expone esto directo en
     * GET /api/pedidos/{id}: cualquier usuario logueado puede iterar ids
     * consecutivos y leer pedidos de otras personas.
     */
    public Optional<Order> obtenerPorIdSinValidarDueno(Long id) {
        return orderRepository.findById(id);
    }

    public List<Order> listarTodos() {
        return orderRepository.findAll();
    }
}
