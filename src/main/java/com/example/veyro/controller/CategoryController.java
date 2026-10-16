package com.example.veyro.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.veyro.model.Category;
import com.example.veyro.service.CategoryService;

@Controller
@RequestMapping("/category")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public String halamanCategory(Model model) {
        model.addAttribute("semuaCategory", categoryService.semuaCategory());
        return "category";
    }

    @PostMapping("/simpan")
    public String simpanCategory(@RequestParam(required = false) String name, RedirectAttributes redirectAttributes) {
        if (name == null || name.isBlank()) {
            redirectAttributes.addFlashAttribute("pesanError", "Nama kategori wajib diisi.");
            return "redirect:/category";
        }

        name = name.trim();
        if (categoryService.cariNamaCategory(name).isPresent()) {
            redirectAttributes.addFlashAttribute("pesanError", "Nama kategori sudah tersedia.");
            return "redirect:/category";
        }

        Category category = new Category();
        category.setName(name);
        categoryService.simpanCategory(category);
        redirectAttributes.addFlashAttribute("pesanSukses", "Kategori berhasil ditambahkan.");
        return "redirect:/category";
    }

    @GetMapping("/edit/{id}")
    public String halamanEdit(@PathVariable Long id, Model model) {
        Category category = categoryService.cariById(id).orElse(null);
        if (category == null) {
            return "redirect:/category";
        }
        model.addAttribute("category", category);
        return "edit-category";
    }

    @PostMapping("/update")
    public String updateCategory(Category category, RedirectAttributes redirectAttributes) {
        if (category.getId() == null || category.getName() == null || category.getName().isBlank() || categoryService.cariById(category.getId()).isEmpty()) {
            redirectAttributes.addFlashAttribute("pesanError", "Data kategori tidak valid.");
            return "redirect:/category";
        }

        category.setName(category.getName().trim());
        if (categoryService.cariNamaCategory(category.getName(), category.getId()).isPresent()) {
            redirectAttributes.addFlashAttribute("pesanError", "Nama kategori sudah digunakan.");
            return "redirect:/category";
        }

        categoryService.simpanCategory(category);
        redirectAttributes.addFlashAttribute("pesanSukses", "Kategori berhasil diperbarui.");
        return "redirect:/category";
    }

    @PostMapping("/hapus/{id}")
    public String hapusCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (categoryService.cariById(id).isEmpty()) {
            redirectAttributes.addFlashAttribute("pesanError", "Kategori tidak ditemukan.");
            return "redirect:/category";
        }
        categoryService.hapusCategory(id);
        redirectAttributes.addFlashAttribute("pesanSukses", "Kategori berhasil dihapus.");
        return "redirect:/category";
    }
}
