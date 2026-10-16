package com.example.veyro.service;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import com.example.veyro.model.Product;
import com.example.veyro.repository.ProductRepository;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product simpanProduct(Product product) {
    if (product.getId() == null) {
        long nomor = productRepository.count() + 1;
        product.setCode("PRD" + String.format("%03d", nomor));
    }
    return productRepository.save(product);
    }

    public List<Product> semuaProduct() {
        return productRepository.findAll();
    }

    public Optional<Product> cariById(Long id) {
        return productRepository.findById(id);
    }

    public Optional<Product> cariKodeProduct(String code) {
        return productRepository.findByCodeIgnoreCase(code);
    }

    public Optional<Product> cariKodeProduct(String code, Long id) {
        return productRepository.findByCodeIgnoreCaseAndIdNot(code, id);
    }

    public void hapusProduct(Long id) {
        productRepository.deleteById(id);
    }
}