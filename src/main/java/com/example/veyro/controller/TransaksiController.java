package com.example.veyro.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.veyro.model.Transaksi;
import com.example.veyro.model.User;
import com.example.veyro.service.TransaksiService;
import com.example.veyro.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/transaksi")
public class TransaksiController {

    private final TransaksiService transaksiService;
    private final UserService userService;

    public TransaksiController(TransaksiService transaksiService, UserService userService) {
        this.transaksiService = transaksiService;
        this.userService = userService;
    }

    @PostMapping("/simpan")
    public String simpanTransaksi(@RequestParam List<Long> productId, @RequestParam List<Integer> jumlah, @RequestParam BigDecimal bayar, @RequestParam String metodePembayaran, HttpSession session, RedirectAttributes redirectAttributes) {
        String role = (String) session.getAttribute("role");
        if (role == null || !"kasir".equalsIgnoreCase(role.trim())) {
            return "redirect:/login";
        }
        User kasir = (User) session.getAttribute("user");
        if (kasir == null) {
            redirectAttributes.addFlashAttribute("pesanError", "Sesi login telah berakhir. Silakan login kembali.");
            return "redirect:/login";
        }
        if (kasir.getId() != null) {
            kasir = userService.cariById(kasir.getId()).orElse(kasir);
            session.setAttribute("user", kasir);
        }
        if (kasir.getRole() == null || !"kasir".equalsIgnoreCase(kasir.getRole().trim())) {
            kasir.setRole("kasir");
        }

        try {
            Transaksi transaksi = transaksiService.buatTransaksi(productId, jumlah, bayar, metodePembayaran, kasir);
            redirectAttributes.addFlashAttribute("pesanSukses", "Transaksi berhasil. Kode: " + transaksi.getKodeTransaksi());
            redirectAttributes.addFlashAttribute("transaksiSuksesId", transaksi.getId());
            redirectAttributes.addFlashAttribute("transaksiSuksesKode", transaksi.getKodeTransaksi());
            redirectAttributes.addFlashAttribute("transaksiSuksesTotal", transaksi.getTotal());
            redirectAttributes.addFlashAttribute("transaksiSuksesBayar", transaksi.getPembayaran());
            redirectAttributes.addFlashAttribute("transaksiSuksesKembalian", transaksi.getKembalian());
            redirectAttributes.addFlashAttribute("transaksiSuksesMetode", transaksi.getMetodePembayaran());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("pesanError", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("pesanError", "Gagal memproses transaksi: " + e.getMessage());
        }
        return "redirect:/kasir/dashboard";
    }
}
