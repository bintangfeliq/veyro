package com.example.veyro.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity 
@Data 
@NoArgsConstructor 
public class Transaksi {
    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;
    private String kodeTransaksi;
    private LocalDateTime tanggalTransaksi;
    private BigDecimal total;
    private BigDecimal pembayaran;
    private BigDecimal kembalian;
    private String metodePembayaran;

    @ManyToOne 
    private User kasir;

    public void setBayar(BigDecimal bayar) {
        throw new UnsupportedOperationException("Unimplemented method 'setBayar'");
    }
}
