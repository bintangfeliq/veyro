package com.example.veyro.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.veyro.model.DetailTransaksi;
import com.example.veyro.model.Product;
import com.example.veyro.model.Transaksi;
import com.example.veyro.service.ProductService;
import com.example.veyro.service.TransaksiService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
@SuppressWarnings("null")
public class DashboardAdminController {

    private final ProductService productService;
    private final TransaksiService transaksiService;

    public DashboardAdminController(ProductService productService, TransaksiService transaksiService) {
        this.productService = productService;
        this.transaksiService = transaksiService;
    }

    @GetMapping({ "/dashboard", "" })
    public String dashboard(HttpSession session, Model model) {
        if (!"admin".equalsIgnoreCase((String) session.getAttribute("role"))) {
            return "redirect:/login";
        }
        List<Product> produkList = productService.semuaProduct();
        List<Transaksi> transaksiList = transaksiService.semuaTransaksi();

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

        LocalDate hariIni = LocalDate.now();
        BigDecimal totalPenjualan = BigDecimal.ZERO;
        List<Transaksi> transaksiHariIniList = new ArrayList<>();
        for (Transaksi t : transaksiList) {
            if (t.getTanggalTransaksi() != null && t.getTanggalTransaksi().toLocalDate().isEqual(hariIni)) {
                transaksiHariIniList.add(t);
                if (t.getTotal() != null) {
                    totalPenjualan = totalPenjualan.add(t.getTotal());
                }
            }
        }

        int totalProdukTerjualHariIni = 0;
        for (DetailTransaksi d : transaksiService.cariDetailByTransaksiIn(transaksiHariIniList)) {
            if (d.getJumlah() != null) {
                totalProdukTerjualHariIni += d.getJumlah();
            }
        }

        model.addAttribute("totalProduk", produkList.size());
        model.addAttribute("totalTransaksiHariIni", transaksiHariIniList.size());
        model.addAttribute("totalProdukTerjualHariIni", totalProdukTerjualHariIni);
        model.addAttribute("totalPenjualan", totalPenjualan);
        model.addAttribute("produkStokMenipis", produkStokMenipis);
        model.addAttribute("transaksiTerbaru", transaksiTerbaru);
        model.addAttribute("currentUser", session.getAttribute("user"));
        model.addAttribute("activeMenu", "dashboard");
        return "admin/dashboard";
    }
}
