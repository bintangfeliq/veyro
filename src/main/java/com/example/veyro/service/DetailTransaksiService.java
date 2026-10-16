package com.example.veyro.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.veyro.model.DetailTransaksi;
import com.example.veyro.repository.DetailTransaksiRepository;

@Service 
public class DetailTransaksiService {
    private final DetailTransaksiRepository detailTransaksiRepository;

    public DetailTransaksiService(DetailTransaksiRepository detailTransaksiRepository) {
        this.detailTransaksiRepository = detailTransaksiRepository;
    }

    public DetailTransaksi simpanDetailTransaksi(DetailTransaksi detailTransaksi) {
        return detailTransaksiRepository.save(detailTransaksi);
    }

    public List<DetailTransaksi> semuaDetailTransaksi() {
        return detailTransaksiRepository.findAll();
    }

    public Optional<DetailTransaksi> cariById(Long id) {
        return detailTransaksiRepository.findById(id);
    }

    public void hapusDetailTransaksi(Long id) {
        detailTransaksiRepository.deleteById(id);
    }
}
