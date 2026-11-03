package com.example.veyro.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.veyro.model.Transaksi;
import com.example.veyro.model.User;

public interface TransaksiRepository extends JpaRepository<Transaksi, Long> {
    List<Transaksi> findByTanggalTransaksiBetweenOrderByTanggalTransaksiDesc(LocalDateTime start, LocalDateTime end);
    Page<Transaksi> findByTanggalTransaksiBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);
    List<Transaksi> findByKasirOrderByTanggalTransaksiDesc(User kasir);
    Page<Transaksi> findByKasir(User kasir, Pageable pageable);
}
