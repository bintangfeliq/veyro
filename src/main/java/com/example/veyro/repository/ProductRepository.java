package com.example.veyro.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.veyro.model.Product;

public interface ProductRepository extends JpaRepository<Product, Long>{
    Optional<Product> findByCodeIgnoreCase(String code);
    Optional<Product> findByCodeIgnoreCaseAndIdNot(String code, Long id);
}
