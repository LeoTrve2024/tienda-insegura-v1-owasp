package com.tienda.insegura.service;

import com.tienda.insegura.model.Product;
import com.tienda.insegura.repository.ProductRepositoryJdbc;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepositoryJdbc productRepositoryJdbc;

    public ProductService(ProductRepositoryJdbc productRepositoryJdbc) {
        this.productRepositoryJdbc = productRepositoryJdbc;
    }

    public List<Product> listarTodos() {
        return productRepositoryJdbc.listarTodos();
    }

    /**
     * Delega directo al repositorio JDBC vulnerable. Ni aqui ni en el
     * controller se sanea/valida "texto" -> el SQLi llega intacto hasta
     * la base de datos (ver ProductRepositoryJdbc.buscarPorNombre).
     */
    public List<Product> buscar(String texto) {
        return productRepositoryJdbc.buscarPorNombre(texto);
    }

    public Product obtenerPorId(String id) {
        return productRepositoryJdbc.buscarPorId(id);
    }
}
