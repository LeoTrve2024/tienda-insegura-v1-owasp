package com.tienda.insegura.repository;

import com.tienda.insegura.model.Product;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.util.List;

/**
 * VULNERABILIDAD A05:2025 - Injection (SQL Injection)
 *
 * Esta clase usa JDBC "puro" a proposito (en vez de JPA/Hibernate +
 * Query Params o @Query con :parametros) para construir sentencias SQL
 * CONCATENANDO strings recibidos directamente del usuario.
 *
 * Vector de explotacion real: GET /api/productos/buscar?q=<payload>
 * Ejemplo de payload para bypass / extraccion de datos con sqlmap:
 *   x%') UNION SELECT id, username, password, 0::numeric, 0, NULL FROM usuarios -- 
 *
 * En la version 2 (saneada) este mismo metodo se reescribe con
 * PreparedStatement / parametros nombrados de JPA.
 */
@Repository
public class ProductRepositoryJdbc {

    private final JdbcTemplate jdbcTemplate;

    public ProductRepositoryJdbc(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Product> productMapper = (ResultSet rs, int rowNum) -> {
        Product p = new Product();
        p.setId(rs.getLong("id"));
        p.setNombre(rs.getString("nombre"));
        p.setDescripcion(rs.getString("descripcion"));
        p.setPrecio(rs.getBigDecimal("precio"));
        p.setStock(rs.getInt("stock"));
        p.setImagenUrl(rs.getString("imagen_url"));
        return p;
    };

    /**
     * Busqueda de productos por nombre.
     * VULNERABLE A PROPOSITO: concatenacion directa de string sin
     * parametrizar -> SQL Injection clasico (comillas simples, UNION-based,
     * boolean-based, time-based, todos funcionan aqui).
     */
    public List<Product> buscarPorNombre(String texto) {
        String sql = "SELECT id, nombre, descripcion, precio, stock, imagen_url "
                + "FROM productos WHERE LOWER(nombre) LIKE LOWER('%" + texto + "%')";

        // Se usa Statement (no PreparedStatement) a proposito para que el
        // vector de SQLi sea directo y facil de automatizar con sqlmap.
        return jdbcTemplate.query(sql, productMapper);
    }

    /**
     * Obtiene un producto por id concatenando el id crudo en el SQL.
     * VULNERABLE A PROPOSITO: aunque el id "deberia" ser numerico, no se
     * castea ni valida, por lo que tambien es explotable
     * (ej: /api/productos/1 OR 1=1).
     */
    public Product buscarPorId(String id) {
        String sql = "SELECT id, nombre, descripcion, precio, stock, imagen_url "
                + "FROM productos WHERE id = " + id;

        List<Product> resultado = jdbcTemplate.query(sql, productMapper);
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    /** Metodo auxiliar usado por ExportController para reportes ad-hoc (ver A05 en ReportService). */
    public List<Product> listarTodos() {
        String sql = "SELECT id, nombre, descripcion, precio, stock, imagen_url FROM productos";
        return jdbcTemplate.query(sql, productMapper);
    }
}
