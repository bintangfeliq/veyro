
package com.example.veyro.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.veyro.model.Category;
import com.example.veyro.repository.CategoryRepository;
import com.example.veyro.repository.ProductRepository;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CategoryService(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    public Category simpanCategory(Category category) {
        return categoryRepository.save(category);
    }

    public List<Category> semuaCategory() {
        return categoryRepository.findAll();
    }

    public Optional<Category> cariById(Long id) {
        return categoryRepository.findById(id);
    }

    public Optional<Category> cariNamaCategory(String name) {
        return categoryRepository.findByNameIgnoreCase(name);
    }

    public Optional<Category> cariNamaCategory(String name, Long id) {
        return categoryRepository.findByNameIgnoreCaseAndIdNot(name, id);
    }

    public boolean hapusCategory(Long id) {
        if (productRepository.existsByCategoryId(id)) {
            return false;
        }
        categoryRepository.deleteById(id);
        return true;
    }
}
