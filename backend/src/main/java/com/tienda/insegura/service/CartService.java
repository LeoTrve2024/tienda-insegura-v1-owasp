package com.tienda.insegura.service;

import com.tienda.insegura.model.Product;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * VULNERABILIDAD A06:2025 - Insecure Design
 *
 * Carrito guardado en memoria del servidor, indexado solo por un
 * "cartId" que viaja como parametro (no por sesion autenticada real),
 * y SIN reglas de negocio:
 *  - Se aceptan cantidades negativas o en cero (podrian usarse para
 *    "restar" dinero del total en el checkout).
 *  - No se valida el stock disponible antes de agregar.
 *  - No hay limite de items ni de monto total.
 *  - El precio unitario se toma del producto en el momento de agregar,
 *    pero nada impide re-enviar un precio distinto desde el cliente si
 *    el endpoint de checkout llegara a confiar en el body (ver
 *    OrderController/OrderService).
 */
@Service
public class CartService {

    // cartId -> (productoId -> cantidad)
    private final Map<String, Map<Long, Integer>> carritos = new ConcurrentHashMap<>();

    public void agregar(String cartId, Long productoId, Integer cantidad) {
        // A06: no se valida cantidad > 0 ni contra el stock real.
        carritos.computeIfAbsent(cartId, k -> new ConcurrentHashMap<>())
                .merge(productoId, cantidad, Integer::sum);
    }

    public Map<Long, Integer> obtenerCarrito(String cartId) {
        return carritos.getOrDefault(cartId, Map.of());
    }

    public BigDecimal calcularTotal(String cartId, List<Product> catalogo) {
        Map<Long, Integer> carrito = obtenerCarrito(cartId);
        BigDecimal total = BigDecimal.ZERO;

        for (Map.Entry<Long, Integer> entry : carrito.entrySet()) {
            Product producto = catalogo.stream()
                    .filter(p -> p.getId().equals(entry.getKey()))
                    .findFirst()
                    .orElse(null);
            if (producto == null) continue;

            // A06: si "entry.getValue()" es negativo, esto RESTA del total.
            total = total.add(producto.getPrecio().multiply(BigDecimal.valueOf(entry.getValue())));
        }
        return total;
    }

    public void vaciar(String cartId) {
        carritos.remove(cartId);
    }
}
