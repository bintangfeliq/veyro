package com.example.veyro.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.veyro.model.Transaksi;

public interface TransaksiRepository extends JpaRepository<Transaksi, Long>{
    
}
