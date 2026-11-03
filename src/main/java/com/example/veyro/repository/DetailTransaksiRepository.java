package com.example.veyro.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.veyro.model.DetailTransaksi;
import com.example.veyro.model.Transaksi;

public interface DetailTransaksiRepository extends JpaRepository<DetailTransaksi, Long> {
    List<DetailTransaksi> findByTransaksiIn(List<Transaksi> transaksiList);
}
