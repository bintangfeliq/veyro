package com.example.veyro.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.veyro.model.Product;
import com.example.veyro.repository.ProductRepository;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public String generateKodeProduct() {
        long nomor = productRepository.count() + 1;
        String kode = "PRD" + String.format("%03d", nomor);
        while (productRepository.findByCodeIgnoreCase(kode).isPresent()) {
            nomor++;
            kode = "PRD" + String.format("%03d", nomor);
        }
        return kode;
    }

    public Product simpanProduct(Product product) {
        if (product.getId() == null && (product.getCode() == null || product.getCode().isBlank())) {
            product.setCode(generateKodeProduct());
        }
        return productRepository.save(product);
    }

    public Page<Product> cariSemuaProduct(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return productRepository.findAll(pageable);
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

    public long hitungPenggunaFoto(String fotoProduk) {
        if (fotoProduk == null || fotoProduk.isBlank()) {
            return 0;
        }
        return productRepository.countByFotoProduk(fotoProduk);
    }
}