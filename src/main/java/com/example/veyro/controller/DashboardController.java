package com.example.veyro.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.veyro.model.Category;
import com.example.veyro.model.Product;
import com.example.veyro.model.Transaksi;
import com.example.veyro.model.User;
import com.example.veyro.service.CategoryService;
import com.example.veyro.service.ProductService;
import com.example.veyro.service.TransaksiService;
import com.example.veyro.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class DashboardController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final UserService userService;
    private final TransaksiService transaksiService;

    public DashboardController(ProductService productService, CategoryService categoryService, UserService userService,
            TransaksiService transaksiService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.userService = userService;
        this.transaksiService = transaksiService;
    }

    @GetMapping({ "/dashboard", "" })
    public String dashboard(HttpSession session, Model model) {
        if (!"admin".equals(session.getAttribute("role"))) {
            return "redirect:/login";
        }
        List<Product> produkList = productService.semuaProduct();
        List<Category> kategoriList = categoryService.semuaCategory();
        List<Transaksi> transaksiList = transaksiService.semuaTransaksi();
        List<User> userList = userService.semuaUser();
        int totalKasir = 0;
        for (User u : userList) {
            if ("kasir".equalsIgnoreCase(u.getRole())) {
                totalKasir++;
            }
        }
        BigDecimal totalPenjualan = BigDecimal.ZERO;
        for (Transaksi t : transaksiList) {
            if (t.getTotal() != null) {
                totalPenjualan = totalPenjualan.add(t.getTotal());
            }
        }
        List<Product> produkStokMenipis = new ArrayList<>();
        for (Product p : produkList) {
            if (p.getStok() != null && p.getStok() <= 5) {
                produkStokMenipis.add(p);
            }
        }
        List<Transaksi> transaksiTerbaru = new ArrayList<>();
        for (int i = transaksiList.size() - 1; i >= 0 && transaksiTerbaru.size() < 5; i--) {
            transaksiTerbaru.add(transaksiList.get(i));
        }
        model.addAttribute("totalProduk", produkList.size());
        model.addAttribute("totalKategori", kategoriList.size());
        model.addAttribute("totalKasir", totalKasir);
        model.addAttribute("totalTransaksi", transaksiList.size());
        model.addAttribute("totalPenjualan", totalPenjualan);
        model.addAttribute("produkStokMenipis", produkStokMenipis);
        model.addAttribute("transaksiTerbaru", transaksiTerbaru);
        model.addAttribute("currentUser", session.getAttribute("user"));
        model.addAttribute("activeMenu", "dashboard");

        return "admin/dashboard";
    }
}
