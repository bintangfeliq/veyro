package com.example.veyro.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.veyro.service.TransaksiService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminTransaksiController {

    private final TransaksiService transaksiService;

    public AdminTransaksiController(TransaksiService transaksiService) {
        this.transaksiService = transaksiService;
    }

    @GetMapping("/transaksi")
    public String transaksi(HttpSession session, Model model) {
        if (!"admin".equals(session.getAttribute("role"))) {
            return "redirect:/login";
        }
        model.addAttribute("semuaTransaksi", transaksiService.semuaTransaksi());
        model.addAttribute("currentUser", session.getAttribute("user"));
        model.addAttribute("activeMenu", "transaksi");
        return "admin/transaksi";
    }
}
