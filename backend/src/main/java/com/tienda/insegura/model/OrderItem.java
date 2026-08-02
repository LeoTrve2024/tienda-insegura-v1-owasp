package com.tienda.insegura.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "pedido_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

 
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "pedido_id", nullable = false)
    private Order pedido;

    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    private Product producto;

    // A06: Insecure Design - no hay @Min(1) ni validacion de negocio;
    // CartService permite cantidades negativas o en cero (ver esa clase).
    @Column(nullable = false)
    private Integer cantidad;

    @Column(nullable = false)
    private BigDecimal precioUnitario;

    public OrderItem() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Order getPedido() { return pedido; }
    public void setPedido(Order pedido) { this.pedido = pedido; }

    public Product getProducto() { return producto; }
    public void setProducto(Product producto) { this.producto = producto; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }
}