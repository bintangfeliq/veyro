package com.example.veyro.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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
        if (kasir == null || !"kasir".equals(kasir.getRole())) {
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
            daftarProduk.merge(idProduk, jumlahProduk, (jumlahLama, jumlahBaru) -> jumlahLama + jumlahBaru);
        }

        Map<Product, Integer> produkDibeli = new LinkedHashMap<>();
        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<Long, Integer> item : daftarProduk.entrySet()) {
            Product product = productRepository.findById(item.getKey()).orElseThrow(() -> new IllegalArgumentException("Produk tidak ditemukan."));
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

    public Optional<Transaksi> cariById(Long id) {
        return transaksiRepository.findById(id);
    }

}