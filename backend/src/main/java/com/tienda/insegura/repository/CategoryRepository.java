package com.tienda.insegura.repository;

import com.tienda.insegura.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
