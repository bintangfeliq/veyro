package com.example.veyro.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.veyro.model.DetailTransaksi;
import com.example.veyro.model.Product;
import com.example.veyro.model.Transaksi;
import com.example.veyro.service.TransaksiService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
@SuppressWarnings("null")
public class LaporanController {

    private final TransaksiService transaksiService;

    public LaporanController(TransaksiService transaksiService) {
        this.transaksiService = transaksiService;
    }

    public static class KategoriStat {
        private String nama;
        private String icon;
        private int totalItem;
        private BigDecimal totalNominal;
        private int persentase;

        public KategoriStat(String nama, String icon, int totalItem, BigDecimal totalNominal, int persentase) {
            this.nama = nama;
            this.icon = icon;
            this.totalItem = totalItem;
            this.totalNominal = totalNominal;
            this.persentase = persentase;
        }

        public String getNama() {
            return nama;
        }

        public String getIcon() {
            return icon;
        }

        public int getTotalItem() {
            return totalItem;
        }

        public BigDecimal getTotalNominal() {
            return totalNominal;
        }

        public int getPersentase() {
            return persentase;
        }
    }

    @GetMapping("/laporan")
    public String laporan(@RequestParam(defaultValue = "1") int page, @RequestParam(required = false) String tipeFilter,
            @RequestParam(required = false) String bulan, @RequestParam(required = false) String tanggalMulai,
            @RequestParam(required = false) String tanggalSelesai, HttpSession session, Model model) {
        if (!"admin".equalsIgnoreCase((String) session.getAttribute("role"))) {
            return "redirect:/login";
        }

        if (tipeFilter == null || tipeFilter.isBlank()) {
            if (bulan != null && !bulan.isBlank()) {
                tipeFilter = "bulan";
            } else if (tanggalMulai != null && !tanggalMulai.isBlank()) {
                tipeFilter = "custom";
            } else {
                tipeFilter = "semua";
            }
        }

        LocalDateTime start = null;
        LocalDateTime end = null;
        String periodeLabel = "Semua Waktu";

        if ("bulan".equalsIgnoreCase(tipeFilter)) {
            if (bulan == null || bulan.isBlank()) {
                bulan = LocalDate.now().toString().substring(0, 7);
            }
            try {
                String[] parts = bulan.split("-");
                int thn = Integer.parseInt(parts[0]);
                int bln = Integer.parseInt(parts[1]);
                YearMonth ym = YearMonth.of(thn, bln);
                start = ym.atDay(1).atStartOfDay();
                end = ym.atEndOfMonth().atTime(23, 59, 59, 999999999);
                periodeLabel = "Bulan " + ym.getMonth().getDisplayName(TextStyle.FULL, Locale.forLanguageTag("id-ID"))
                        + " " + thn;
            } catch (Exception e) {
                tipeFilter = "semua";
                start = null;
                end = null;
                periodeLabel = "Semua Waktu";
            }
        } else if ("custom".equalsIgnoreCase(tipeFilter)) {
            if (tanggalMulai == null || tanggalMulai.isBlank()) {
                tanggalMulai = LocalDate.now().withDayOfMonth(1).toString();
            }
            if (tanggalSelesai == null || tanggalSelesai.isBlank()) {
                tanggalSelesai = LocalDate.now().toString();
            }
            try {
                LocalDate tglMulai = LocalDate.parse(tanggalMulai);
                LocalDate tglSelesai = LocalDate.parse(tanggalSelesai);
                if (tglMulai.isAfter(tglSelesai)) {
                    LocalDate temp = tglMulai;
                    tglMulai = tglSelesai;
                    tglSelesai = temp;
                    tanggalMulai = tglMulai.toString();
                    tanggalSelesai = tglSelesai.toString();
                }
                start = tglMulai.atStartOfDay();
                end = tglSelesai.atTime(23, 59, 59, 999999999);
                periodeLabel = tanggalMulai + " s/d " + tanggalSelesai;
            } catch (Exception e) {
                tipeFilter = "semua";
                start = null;
                end = null;
                periodeLabel = "Semua Waktu";
            }
        }

        List<Transaksi> semuaFilter;
        Page<Transaksi> transaksiPage;
        int pageIndex = page > 0 ? page - 1 : 0;
        Pageable pageable = PageRequest.of(pageIndex, 15, Sort.by("id").descending());

        if (start != null && end != null) {
            semuaFilter = transaksiService.cariTransaksiFilterList(start, end);
            transaksiPage = transaksiService.cariTransaksiFilterPage(start, end, pageable);
        } else {
            semuaFilter = transaksiService.cariSemuaTransaksiTerbaru();
            transaksiPage = transaksiService.cariSemuaTransaksiPage(pageable);
        }

        BigDecimal totalUangMasuk = BigDecimal.ZERO;
        BigDecimal totalCash = BigDecimal.ZERO;
        BigDecimal totalQrisTransfer = BigDecimal.ZERO;

        for (Transaksi t : semuaFilter) {
            if (t.getTotal() != null) {
                totalUangMasuk = totalUangMasuk.add(t.getTotal());
                String metode = t.getMetodePembayaran();
                if (metode != null) {
                    metode = metode.trim().toLowerCase();
                    if (metode.contains("tunai") || metode.contains("cash")) {
                        totalCash = totalCash.add(t.getTotal());
                    } else if (metode.contains("qris") || metode.contains("transfer")) {
                        totalQrisTransfer = totalQrisTransfer.add(t.getTotal());
                    }
                }
            }
        }

        List<DetailTransaksi> detailSemua = transaksiService.cariDetailByTransaksiIn(semuaFilter);
        Map<String, Integer> mapJumlah = new LinkedHashMap<>();
        Map<String, BigDecimal> mapNominal = new LinkedHashMap<>();
        Map<String, String> mapIcon = new LinkedHashMap<>();

        BigDecimal totalSemuaKategori = BigDecimal.ZERO;

        for (DetailTransaksi dt : detailSemua) {
            Product p = dt.getProduk();
            String namaKategori = "Lainnya";
            String iconKategori = "📦";

            if (p != null && p.getCategory() != null) {
                if (p.getCategory().getName() != null && !p.getCategory().getName().isBlank()) {
                    namaKategori = p.getCategory().getName();
                }
                if (p.getCategory().getIcon() != null && !p.getCategory().getIcon().isBlank()) {
                    iconKategori = p.getCategory().getIcon();
                }
            }

            int jml = (dt.getJumlah() != null) ? dt.getJumlah() : 0;
            BigDecimal sub = (dt.getSubtotal() != null) ? dt.getSubtotal() : BigDecimal.ZERO;

            mapJumlah.put(namaKategori, mapJumlah.getOrDefault(namaKategori, 0) + jml);
            mapNominal.put(namaKategori, mapNominal.getOrDefault(namaKategori, BigDecimal.ZERO).add(sub));
            mapIcon.put(namaKategori, iconKategori);

            totalSemuaKategori = totalSemuaKategori.add(sub);
        }

        List<KategoriStat> kategoriStats = new ArrayList<>();
        List<String> chartLabels = new ArrayList<>();
        List<BigDecimal> chartDataNominal = new ArrayList<>();
        List<Integer> chartDataJumlah = new ArrayList<>();

        for (String kat : mapJumlah.keySet()) {
            int jml = mapJumlah.get(kat);
            BigDecimal nom = mapNominal.get(kat);
            String ic = mapIcon.get(kat);
            int persen = 0;
            if (totalSemuaKategori.compareTo(BigDecimal.ZERO) > 0) {
                persen = nom.multiply(BigDecimal.valueOf(100))
                        .divide(totalSemuaKategori, 0, java.math.RoundingMode.HALF_UP).intValue();
            }
            kategoriStats.add(new KategoriStat(kat, ic, jml, nom, persen));
            chartLabels.add(ic + " " + kat);
            chartDataNominal.add(nom);
            chartDataJumlah.add(jml);
        }

        StringBuilder sbLabels = new StringBuilder("[");
        StringBuilder sbNominal = new StringBuilder("[");
        StringBuilder sbJumlah = new StringBuilder("[");

        for (int i = 0; i < chartLabels.size(); i++) {
            if (i > 0) {
                sbLabels.append(",");
                sbNominal.append(",");
                sbJumlah.append(",");
            }
            sbLabels.append("\"").append(chartLabels.get(i).replace("\"", "\\\"")).append("\"");
            sbNominal.append(chartDataNominal.get(i));
            sbJumlah.append(chartDataJumlah.get(i));
        }

        sbLabels.append("]");
        sbNominal.append("]");
        sbJumlah.append("]");

        String chartLabelsJson = sbLabels.toString();
        String chartDataNominalJson = sbNominal.toString();
        String chartDataJumlahJson = sbJumlah.toString();

        List<Transaksi> daftarTransaksi = transaksiPage.getContent();
        Map<Long, List<DetailTransaksi>> detailsPerTransaksi = transaksiService.kelompokkanDetail(daftarTransaksi);

        LocalDate hariIni = LocalDate.now();
        List<Transaksi> semuaTransaksi = transaksiService.semuaTransaksi();
        List<Transaksi> transaksiHariIniList = new ArrayList<>();
        for (Transaksi t : semuaTransaksi) {
            if (t.getTanggalTransaksi() != null && t.getTanggalTransaksi().toLocalDate().isEqual(hariIni)) {
                transaksiHariIniList.add(t);
            }
        }
        int totalProdukTerjualHariIni = 0;
        List<DetailTransaksi> detailHariIni = transaksiService.cariDetailByTransaksiIn(transaksiHariIniList);
        for (DetailTransaksi d : detailHariIni) {
            if (d.getJumlah() != null) {
                totalProdukTerjualHariIni += d.getJumlah();
            }
        }

        model.addAttribute("totalUangMasuk", totalUangMasuk);
        model.addAttribute("totalCash", totalCash);
        model.addAttribute("totalQrisTransfer", totalQrisTransfer);
        model.addAttribute("totalUangMasukRingkas", formatRingkas(totalUangMasuk));
        model.addAttribute("totalCashRingkas", formatRingkas(totalCash));
        model.addAttribute("totalQrisTransferRingkas", formatRingkas(totalQrisTransfer));
        model.addAttribute("totalTransaksi", semuaFilter.size());
        model.addAttribute("totalProdukTerjualHariIni", totalProdukTerjualHariIni);

        model.addAttribute("daftarTransaksi", daftarTransaksi);
        model.addAttribute("detailsPerTransaksi", detailsPerTransaksi);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", transaksiPage.getTotalPages());

        model.addAttribute("tipeFilter", tipeFilter);
        model.addAttribute("bulan", bulan);
        model.addAttribute("tanggalMulai", tanggalMulai);
        model.addAttribute("tanggalSelesai", tanggalSelesai);
        model.addAttribute("periodeLabel", periodeLabel);

        model.addAttribute("kategoriStats", kategoriStats);
        model.addAttribute("chartLabelsJson", chartLabelsJson);
        model.addAttribute("chartDataNominalJson", chartDataNominalJson);
        model.addAttribute("chartDataJumlahJson", chartDataJumlahJson);

        model.addAttribute("currentUser", session.getAttribute("user"));
        model.addAttribute("activeMenu", "laporan");

        return "admin/laporan";
    }

    private String formatRingkas(BigDecimal nominal) {
        if (nominal == null) {
            return "Rp 0";
        }
        long nilai = nominal.longValue();
        if (nilai >= 1000000000L) {
            double hasil = (double) nilai / 1000000000.0;
            return "Rp " + formatDesimalRingkas(hasil) + "M";
        }
        if (nilai >= 1000000L) {
            double hasil = (double) nilai / 1000000.0;
            return "Rp " + formatDesimalRingkas(hasil) + "JT";
        }
        if (nilai >= 100000L) {
            double hasil = (double) nilai / 1000.0;
            return "Rp " + formatDesimalRingkas(hasil) + "K";
        }
        return "Rp " + String.format(Locale.forLanguageTag("id-ID"), "%,d", nilai);
    }

    private String formatDesimalRingkas(double angka) {
        long bulat = (long) angka;
        if (angka == bulat) {
            return String.valueOf(bulat);
        }
        String teks = String.format(Locale.forLanguageTag("id-ID"), "%.1f", angka);
        if (teks.endsWith(",0")) {
            return teks.substring(0, teks.length() - 2);
        }
        return teks;
    }
}
