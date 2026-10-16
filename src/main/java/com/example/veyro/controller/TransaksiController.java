package com.example.veyro.controller;

import java.math.BigDecimal;
import java.util.List;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.veyro.model.Transaksi;
import com.example.veyro.model.User;
import com.example.veyro.service.ProductService;
import com.example.veyro.service.TransaksiService;

@Controller
@RequestMapping("/transaksi")
public class TransaksiController {

    private final TransaksiService transaksiService;
    private final ProductService productService;

    public TransaksiController(TransaksiService transaksiService, ProductService productService) {
        this.transaksiService = transaksiService;
        this.productService = productService;
    }

    @GetMapping
    public String halamanTransaksi(HttpSession session, Model model) {
        if (!"kasir".equals(session.getAttribute("role"))) {
            return "redirect:/login";
        }
        model.addAttribute("semuaProduct", productService.semuaProduct());
        return "transaksi";
    }

    @PostMapping("/simpan")
    public String simpanTransaksi(@RequestParam List<Long> productId, @RequestParam List<Integer> jumlah, @RequestParam BigDecimal bayar, @RequestParam String metodePembayaran, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!"kasir".equals(session.getAttribute("role"))) {
            return "redirect:/login";
        }
        User kasir = (User) session.getAttribute("user");
        try {
            Transaksi transaksi = transaksiService.buatTransaksi( productId, jumlah, bayar, metodePembayaran, kasir);
            redirectAttributes.addFlashAttribute("pesanSukses", "Transaksi berhasil. Kode: " + transaksi.getKodeTransaksi());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("pesanError", e.getMessage());
        }
        return "redirect:/transaksi";
    }
}
