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
import com.example.veyro.service.TransaksiService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
@SuppressWarnings("null")
public class RiwayatTransaksiAdmin {

    private final TransaksiService transaksiService;

    public RiwayatTransaksiAdmin(TransaksiService transaksiService) {
        this.transaksiService = transaksiService;
    }

    @GetMapping("/transaksi")
    public String transaksi(@RequestParam(defaultValue = "1") int page, HttpSession session, Model model) {
        if (!"admin".equalsIgnoreCase((String) session.getAttribute("role"))) {
            return "redirect:/login";
        }
        PageRequest pageable = PageRequest.of(Math.max(page - 1, 0), 15, Sort.by("id").descending());
        Page<Transaksi> transaksiPage = transaksiService.cariSemuaTransaksiPage(pageable);

        model.addAttribute("semuaTransaksi", transaksiService.cariSemuaTransaksiTerbaru());
        model.addAttribute("daftarTransaksi", transaksiPage.getContent());
        model.addAttribute("detailsPerTransaksi", transaksiService.kelompokkanDetail(transaksiPage.getContent()));
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", transaksiPage.getTotalPages());
        model.addAttribute("currentUser", session.getAttribute("user"));
        model.addAttribute("activeMenu", "transaksi");
        return "admin/transaksi";
    }
}
