package com.example.veyro.controller;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.veyro.service.TransaksiService;

@Controller
@RequestMapping("/transaksi")
public class DetailTransaksiController {

    private final TransaksiService transaksiService;

    public DetailTransaksiController(TransaksiService transaksiService) {
        this.transaksiService = transaksiService;
    }

    @GetMapping("/detail/{id}")
    public String detailTransaksi(@PathVariable Long id, HttpSession session, Model model) {
        if (!"kasir".equals(session.getAttribute("role"))) {
            return "redirect:/login";
        }
        var transaksi = transaksiService.cariById(id);
        if (transaksi.isEmpty()) {
            return "redirect:/transaksi";
        }
        model.addAttribute("transaksi", transaksi.get());
        return "detail-transaksi";
    }
}