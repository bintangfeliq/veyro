package com.example.veyro.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.veyro.model.Transaksi;
import com.example.veyro.model.User;
import com.example.veyro.service.CategoryService;
import com.example.veyro.service.ProductService;
import com.example.veyro.service.TransaksiService;
import com.example.veyro.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/kasir")
public class DashboardKasirController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final TransaksiService transaksiService;
    private final UserService userService;

    public DashboardKasirController(ProductService productService, CategoryService categoryService, TransaksiService transaksiService, UserService userService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.transaksiService = transaksiService;
        this.userService = userService;
    }

    @GetMapping({"", "/", "/dashboard"})
    public String dashboardKasir(HttpSession session, Model model) {
        if (bukanKasir(session)) {
            return "redirect:/login";
        }
        model.addAttribute("currentUser", muatUser(session));
        model.addAttribute("semuaProduct", productService.semuaProduct());
        model.addAttribute("semuaCategory", categoryService.semuaCategory());
        model.addAttribute("activeMenu", "kasir");
        return "kasir/dashboard";
    }

    @GetMapping("/riwayat")
    public String riwayatKasir(@RequestParam(defaultValue = "1") int page, HttpSession session, Model model) {
        if (bukanKasir(session)) {
            return "redirect:/login";
        }
        User currentUser = muatUser(session);
        PageRequest pageable = PageRequest.of(Math.max(page - 1, 0), 15, Sort.by("id").descending());
        Page<Transaksi> transaksiPage = transaksiService.cariTransaksiByKasirPage(currentUser, pageable);

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("semuaTransaksi", transaksiService.cariTransaksiByKasir(currentUser));
        model.addAttribute("daftarTransaksi", transaksiPage.getContent());
        model.addAttribute("detailsPerTransaksi", transaksiService.kelompokkanDetail(transaksiPage.getContent()));
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", transaksiPage.getTotalPages());
        model.addAttribute("activeMenu", "riwayat");
        return "kasir/riwayat";
    }

    private boolean bukanKasir(HttpSession session) {
        String role = (String) session.getAttribute("role");
        return role == null || !"kasir".equalsIgnoreCase(role.trim());
    }

    private User muatUser(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user != null && user.getId() != null) {
            user = userService.cariById(user.getId()).orElse(user);
            session.setAttribute("user", user);
        }
        return user;
    }
}
