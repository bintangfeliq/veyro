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
@SuppressWarnings("null")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public String halamanCategory(HttpSession session, Model model) {
        if (bukanAdmin(session)) {
            return "redirect:/login";
        }
        model.addAttribute("semuaCategory", categoryService.semuaCategory());
        model.addAttribute("currentUser", session.getAttribute("user"));
        model.addAttribute("activeMenu", "kategori");
        return "admin/category";
    }

    @PostMapping("/simpan")
    public String simpanCategory(@RequestParam(required = false) String name, @RequestParam(required = false) String icon,
            RedirectAttributes redirect, HttpSession session) {
        if (bukanAdmin(session)) {
            return "redirect:/login";
        }
        if (name == null || name.isBlank()) {
            return kembali(redirect, "pesanError", "Nama kategori wajib diisi.");
        }
        String namaBersih = name.trim();
        if (categoryService.cariNamaCategory(namaBersih).isPresent()) {
            return kembali(redirect, "pesanError", "Nama kategori sudah tersedia.");
        }
        Category category = new Category();
        category.setName(namaBersih);
        category.setIcon(icon == null || icon.isBlank() ? "📦" : icon);
        categoryService.simpanCategory(category);
        return kembali(redirect, "pesanSukses", "Kategori berhasil ditambahkan.");
    }

    @PostMapping("/update")
    public String updateCategory(Category data, RedirectAttributes redirect, HttpSession session) {
        if (bukanAdmin(session)) {
            return "redirect:/login";
        }
        if (data.getId() == null || data.getName() == null || data.getName().isBlank()) {
            return kembali(redirect, "pesanError", "Data kategori tidak valid.");
        }
        Category category = categoryService.cariById(data.getId()).orElse(null);
        if (category == null) {
            return kembali(redirect, "pesanError", "Kategori tidak ditemukan.");
        }
        String namaBersih = data.getName().trim();
        if (categoryService.cariNamaCategory(namaBersih, data.getId()).isPresent()) {
            return kembali(redirect, "pesanError", "Nama kategori sudah digunakan.");
        }
        category.setName(namaBersih);
        if (data.getIcon() != null && !data.getIcon().isBlank()) {
            category.setIcon(data.getIcon());
        }
        categoryService.simpanCategory(category);
        return kembali(redirect, "pesanSukses", "Kategori berhasil diperbarui.");
    }

    @PostMapping("/hapus/{id}")
    public String hapusCategory(@PathVariable Long id, RedirectAttributes redirect, HttpSession session) {
        if (bukanAdmin(session)) {
            return "redirect:/login";
        }
        if (categoryService.cariById(id).isEmpty()) {
            return kembali(redirect, "pesanError", "Kategori tidak ditemukan.");
        }
        if (!categoryService.hapusCategory(id)) {
            return kembali(redirect, "pesanError", "Kategori tidak bisa dihapus karena masih digunakan oleh produk.");
        }
        return kembali(redirect, "pesanSukses", "Kategori berhasil dihapus.");
    }

    private boolean bukanAdmin(HttpSession session) {
        return !"admin".equalsIgnoreCase((String) session.getAttribute("role"));
    }

    private String kembali(RedirectAttributes redirect, String kunci, String pesan) {
        redirect.addFlashAttribute(kunci, pesan);
        return "redirect:/category";
    }
}
