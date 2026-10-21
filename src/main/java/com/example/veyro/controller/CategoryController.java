package com.example.veyro.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.veyro.model.Category;
import com.example.veyro.service.CategoryService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/category")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public String halamanCategory(HttpSession session, Model model) {
        if (!"admin".equals(session.getAttribute("role"))) {
            return "redirect:/login";
        }
        model.addAttribute("semuaCategory", categoryService.semuaCategory());
        model.addAttribute("currentUser", session.getAttribute("user"));
        model.addAttribute("activeMenu", "kategori");
        return "admin/category";
    }

    @PostMapping("/simpan")
    public String simpanCategory(@RequestParam(required = false) String name,
            @RequestParam(required = false) String icon, RedirectAttributes redirectAttributes, HttpSession session) {
        if (!"admin".equals(session.getAttribute("role"))) {
            return "redirect:/login";
        }
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
        category.setIcon(icon == null || icon.isBlank() ? "📦" : icon);
        categoryService.simpanCategory(category);
        redirectAttributes.addFlashAttribute("pesanSukses", "Kategori berhasil ditambahkan.");
        return "redirect:/category";
    }

    @GetMapping("/edit/{id}")
    public String halamanEdit(@PathVariable Long id, Model model, HttpSession session) {
        if (!"admin".equals(session.getAttribute("role"))) {
            return "redirect:/login";
        }
        Category category = categoryService.cariById(id).orElse(null);
        if (category == null) {
            return "redirect:/category";
        }
        model.addAttribute("category", category);
        model.addAttribute("currentUser", session.getAttribute("user"));
        model.addAttribute("activeMenu", "kategori");
        return "admin/category";
    }

    @PostMapping("/update")
    public String updateCategory(Category data, RedirectAttributes redirectAttributes, HttpSession session) {
        if (!"admin".equals(session.getAttribute("role"))) {
            return "redirect:/login";
        }
        if (data.getId() == null || data.getName() == null || data.getName().isBlank()) {
            redirectAttributes.addFlashAttribute("pesanError", "Data kategori tidak valid.");
            return "redirect:/category";
        }
        Category category = categoryService.cariById(data.getId()).orElse(null);
        if (category == null) {
            redirectAttributes.addFlashAttribute("pesanError", "Kategori tidak ditemukan.");
            return "redirect:/category";
        }
        String name = data.getName().trim();
        if (categoryService.cariNamaCategory(name, data.getId()).isPresent()) {
            redirectAttributes.addFlashAttribute("pesanError", "Nama kategori sudah digunakan.");
            return "redirect:/category";
        }
        category.setName(name);
        if (data.getIcon() != null && !data.getIcon().isBlank()) {
            category.setIcon(data.getIcon());
        }
        categoryService.simpanCategory(category);
        redirectAttributes.addFlashAttribute("pesanSukses", "Kategori berhasil diperbarui.");
        return "redirect:/category";
    }

    @PostMapping("/hapus/{id}")
    public String hapusCategory(@PathVariable Long id, RedirectAttributes redirectAttributes, HttpSession session) {
        if (!"admin".equals(session.getAttribute("role"))) {
            return "redirect:/login";
        }
        if (categoryService.cariById(id).isEmpty()) {
            redirectAttributes.addFlashAttribute("pesanError", "Kategori tidak ditemukan.");
            return "redirect:/category";
        }
        boolean berhasil = categoryService.hapusCategory(id);
        if (!berhasil) {
            redirectAttributes.addFlashAttribute("pesanError",
                    "Kategori tidak bisa dihapus karena masih digunakan oleh produk.");
            return "redirect:/category";
        }
        redirectAttributes.addFlashAttribute("pesanSukses", "Kategori berhasil dihapus.");
        return "redirect:/category";
    }
}
