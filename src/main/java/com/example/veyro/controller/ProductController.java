package com.example.veyro.controller;

import java.math.BigDecimal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.veyro.model.Category;
import com.example.veyro.model.Product;
import com.example.veyro.service.CategoryService;
import com.example.veyro.service.ProductService;

@Controller
@RequestMapping("/product")
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;

    public ProductController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public String halamanProduct(Model model) {
        model.addAttribute("semuaProduct", productService.semuaProduct());
        model.addAttribute("semuaCategory", categoryService.semuaCategory());
        return "product";
    }

    @PostMapping("/simpan")
    public String simpanProduct(@RequestParam String name, @RequestParam BigDecimal price, @RequestParam Integer stock, @RequestParam Long categoryId, RedirectAttributes redirectAttributes) {
        if (name.isBlank() || price.compareTo(BigDecimal.ZERO) <= 0 || stock < 0) {
            redirectAttributes.addFlashAttribute("pesanError", "Produk gagal ditambahkan, pastikan semua form diisi dengan benar.");
            return "redirect:/product";
        }
        Category category = categoryService.cariById(categoryId).orElse(null);
        if (category == null) {
            redirectAttributes.addFlashAttribute("pesanError", "Kategori tidak ditemukan.");
            return "redirect:/product";
        }
        Product product = new Product();
        product.setName(name.trim());
        product.setPrice(price);
        product.setStok(stock);
        product.setCategory(category);
        productService.simpanProduct(product);
        redirectAttributes.addFlashAttribute("pesanSukses", "Produk berhasil ditambahkan.");
        return "redirect:/product";
    }

    @GetMapping("/edit/{id}")
    public String halamanEdit(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Product product = productService.cariById(id).orElse(null);
        if (product == null) {
            redirectAttributes.addFlashAttribute("pesanError", "Produk tidak ditemukan.");
            return "redirect:/product";
        }
        model.addAttribute("product", product);
        model.addAttribute("semuaCategory", categoryService.semuaCategory());
        return "edit-product";
    }

    @PostMapping("/update")
    public String updateProduct( @RequestParam Long id, @RequestParam String name, @RequestParam BigDecimal price, @RequestParam Integer stock, @RequestParam Long categoryId, RedirectAttributes redirectAttributes) {
        if (name.isBlank() || price.compareTo(BigDecimal.ZERO) <= 0 || stock < 0) {
            redirectAttributes.addFlashAttribute("pesanError", "Data produk tidak valid.");
            return "redirect:/product";
        }
        Product product = productService.cariById(id).orElse(null);
        if (product == null) {
            redirectAttributes.addFlashAttribute("pesanError", "Produk tidak ditemukan.");
            return "redirect:/product";
        }
        Category category = categoryService.cariById(categoryId).orElse(null);
        if (category == null) {
            redirectAttributes.addFlashAttribute("pesanError", "Kategori tidak ditemukan.");
            return "redirect:/product";
        }
        product.setName(name.trim());
        product.setPrice(price);
        product.setStok(stock);
        product.setCategory(category);
        productService.simpanProduct(product);
        redirectAttributes.addFlashAttribute("pesanSukses", "Produk berhasil diperbarui.");
        return "redirect:/product";
    }

    @PostMapping("/hapus/{id}")
    public String hapusProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (productService.cariById(id).isEmpty()) {
            redirectAttributes.addFlashAttribute("pesanError", "Produk tidak ditemukan.");
            return "redirect:/product";
        }
        productService.hapusProduct(id);
        redirectAttributes.addFlashAttribute("pesanSukses", "Produk berhasil dihapus.");
        return "redirect:/product";
    }
}