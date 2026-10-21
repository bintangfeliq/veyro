package com.example.veyro.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.veyro.model.Transaksi;
import com.example.veyro.service.TransaksiService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class LaporanController {

    private final TransaksiService transaksiService;

    public LaporanController(TransaksiService transaksiService) {
        this.transaksiService = transaksiService;
    }

    @GetMapping("/laporan")
    public String laporan(HttpSession session, Model model) {
        if (!"admin".equals(session.getAttribute("role"))) {
            return "redirect:/login";
        }
        List<Transaksi> transaksiList = transaksiService.semuaTransaksi();
        BigDecimal totalPendapatan = BigDecimal.ZERO;
        for (Transaksi t : transaksiList) {
            if (t.getTotal() != null) {
                totalPendapatan = totalPendapatan.add(t.getTotal());
            }
        }
        model.addAttribute("semuaTransaksi", transaksiList);
        model.addAttribute("totalPendapatan", totalPendapatan);
        model.addAttribute("totalTransaksi", transaksiList.size());
        model.addAttribute("currentUser", session.getAttribute("user"));
        model.addAttribute("activeMenu", "laporan");
        return "admin/laporan";
    }
}
