package com.example.veyro.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.veyro.model.Category;

public interface CategoryRepository extends JpaRepository<Category,Long>{
    Optional<Category> findByNameIgnoreCase(String name);
    Optional<Category> findByNameIgnoreCaseAndIdNot(String name, Long id);
}
