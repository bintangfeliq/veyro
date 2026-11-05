package com.example.veyro.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.veyro.model.DetailTransaksi;
import com.example.veyro.model.Product;
import com.example.veyro.model.Transaksi;
import com.example.veyro.model.User;
import com.example.veyro.repository.DetailTransaksiRepository;
import com.example.veyro.repository.ProductRepository;
import com.example.veyro.repository.TransaksiRepository;

@Service
public class TransaksiService {

    private final TransaksiRepository transaksiRepository;
    private final DetailTransaksiRepository detailTransaksiRepository;
    private final ProductRepository productRepository;

    public TransaksiService(TransaksiRepository transaksiRepository, DetailTransaksiRepository detailTransaksiRepository, ProductRepository productRepository) {
        this.transaksiRepository = transaksiRepository;
        this.detailTransaksiRepository = detailTransaksiRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public Transaksi buatTransaksi(List<Long> productId, List<Integer> jumlah, BigDecimal bayar, String metodePembayaran, User kasir) {
        if (productId == null || jumlah == null || productId.isEmpty() || productId.size() != jumlah.size()) {
            throw new IllegalArgumentException("Produk atau jumlah tidak valid.");
        }
        if (kasir == null) {
            throw new IllegalArgumentException("Akun kasir tidak valid.");
        }
        if (kasir.getRole() == null || !"kasir".equalsIgnoreCase(kasir.getRole().trim())) {
            throw new IllegalArgumentException("Akun kasir tidak valid.");
        }
        if (bayar == null || bayar.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Nominal pembayaran tidak valid.");
        }
        if (!List.of("Tunai", "QRIS", "Transfer").contains(metodePembayaran)) {
            throw new IllegalArgumentException("Metode pembayaran tidak valid.");
        }

        Map<Long, Integer> daftarProduk = new LinkedHashMap<>();
        for (int i = 0; i < productId.size(); i++) {
            Long idProduk = productId.get(i);
            Integer jumlahProduk = jumlah.get(i);
            if (idProduk == null || jumlahProduk == null || jumlahProduk <= 0) {
                throw new IllegalArgumentException("Produk atau jumlah tidak valid.");
            }
            if (daftarProduk.containsKey(idProduk)) {
                daftarProduk.put(idProduk, daftarProduk.get(idProduk) + jumlahProduk);
            } else {
                daftarProduk.put(idProduk, jumlahProduk);
            }
        }

        Map<Product, Integer> produkDibeli = new LinkedHashMap<>();
        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<Long, Integer> item : daftarProduk.entrySet()) {
            Optional<Product> optProduct = productRepository.findById(item.getKey());
            if (optProduct.isEmpty()) {
                throw new IllegalArgumentException("Produk tidak ditemukan.");
            }
            Product product = optProduct.get();
            int jumlahProduk = item.getValue();
            if (product.getStok() == null || product.getStok() < jumlahProduk) {
                throw new IllegalArgumentException("Stok produk " + product.getName() + " tidak mencukupi.");
            }
            if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Harga produk tidak valid.");
            }
            produkDibeli.put(product, jumlahProduk);
            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(jumlahProduk));
            total = total.add(subtotal);
        }

        if (bayar.compareTo(total) < 0) {
            throw new IllegalArgumentException("Uang pembayaran kurang.");
        }

        Transaksi transaksi = new Transaksi();
        transaksi.setKodeTransaksi("TRX" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase());
        transaksi.setTanggalTransaksi(LocalDateTime.now());
        transaksi.setTotal(total);
        transaksi.setBayar(bayar);
        transaksi.setKembalian(bayar.subtract(total));
        transaksi.setMetodePembayaran(metodePembayaran);
        transaksi.setKasir(kasir);
        transaksiRepository.save(transaksi);

        for (Map.Entry<Product, Integer> item : produkDibeli.entrySet()) {
            Product product = item.getKey();
            int jumlahProduk = item.getValue();

            DetailTransaksi detail = new DetailTransaksi();
            detail.setTransaksi(transaksi);
            detail.setProduk(product);
            detail.setJumlah(jumlahProduk);
            detail.setHarga(product.getPrice());
            detail.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(jumlahProduk)));
            detailTransaksiRepository.save(detail);

            product.setStok(product.getStok() - jumlahProduk);
            productRepository.save(product);
        }

        return transaksi;
    }

    public List<Transaksi> semuaTransaksi() {
        return transaksiRepository.findAll();
    }

    public Page<Transaksi> cariSemuaTransaksiPage(Pageable pageable) {
        return transaksiRepository.findAll(pageable);
    }

    public Page<Transaksi> cariTransaksiFilterPage(LocalDateTime start, LocalDateTime end, Pageable pageable) {
        return transaksiRepository.findByTanggalTransaksiBetween(start, end, pageable);
    }

    public List<Transaksi> cariSemuaTransaksiTerbaru() {
        return transaksiRepository.findAll(Sort.by("id").descending());
    }

    public List<Transaksi> cariTransaksiFilterList(LocalDateTime start, LocalDateTime end) {
        return transaksiRepository.findByTanggalTransaksiBetweenOrderByTanggalTransaksiDesc(start, end);
    }

    @Transactional(readOnly = true)
    public List<DetailTransaksi> cariDetailByTransaksiIn(List<Transaksi> transaksiList) {
        if (transaksiList == null || transaksiList.isEmpty()) {
            return new ArrayList<>();
        }
        return detailTransaksiRepository.findByTransaksiIn(transaksiList);
    }

    public Map<Long, List<DetailTransaksi>> kelompokkanDetail(List<Transaksi> transaksiList) {
        Map<Long, List<DetailTransaksi>> hasil = new HashMap<>();
        for (DetailTransaksi detail : cariDetailByTransaksiIn(transaksiList)) {
            if (detail.getTransaksi() == null || detail.getTransaksi().getId() == null) {
                continue;
            }
            Long idTransaksi = detail.getTransaksi().getId();
            if (!hasil.containsKey(idTransaksi)) {
                hasil.put(idTransaksi, new ArrayList<>());
            }
            hasil.get(idTransaksi).add(detail);
        }
        return hasil;
    }

    public List<Transaksi> cariTransaksiByKasir(User kasir) {
        if (kasir == null) {
            return new ArrayList<>();
        }
        return transaksiRepository.findByKasirOrderByTanggalTransaksiDesc(kasir);
    }

    public Page<Transaksi> cariTransaksiByKasirPage(User kasir, Pageable pageable) {
        if (kasir == null) {
            return Page.empty(pageable);
        }
        return transaksiRepository.findByKasir(kasir, pageable);
    }
}
