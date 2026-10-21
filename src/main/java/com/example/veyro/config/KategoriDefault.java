package com.example.veyro.config;

import java.util.Optional;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.veyro.model.Category;
import com.example.veyro.repository.CategoryRepository;

@Configuration
public class KategoriDefault {

    @Bean
    CommandLineRunner isiKategoriDefault(CategoryRepository repository) {
        return args -> {
            aturKategori(repository, "Makanan", "🍜");
            aturKategori(repository, "Minuman", "🥤");
            aturKategori(repository, "Permen & Cokelat", "🍬");
            aturKategori(repository, "Perawatan Diri", "🧴");
            aturKategori(repository, "Kebutuhan Rumah", "🧹");
            aturKategori(repository, "Sayur & Buah", "🥕");
            aturKategori(repository, "Bahan Pokok", "🌾");
            aturKategori(repository, "Lainnya", "📦");
        };
    }

    private void aturKategori(CategoryRepository repository, String nama, String ikon) {
        Optional<Category> opt = repository.findByNameIgnoreCase(nama);
        if (opt.isEmpty()) {
            Category kategori = new Category();
            kategori.setName(nama);
            kategori.setIcon(ikon);
            repository.save(kategori);
        } else {
            Category kategori = opt.get();
            String iconSaatIni = kategori.getIcon();
            if (iconSaatIni == null || iconSaatIni.isBlank() || isIkonLama(iconSaatIni)) {
                kategori.setIcon(ikon);
                repository.save(kategori);
            }
        }
    }

    private boolean isIkonLama(String icon) {
        return icon.equals("??") || icon.equals("utensils") || icon.equals("cup-soda") 
                || icon.equals("candy") || icon.equals("bath") || icon.equals("spray-can") 
                || icon.equals("carrot") || icon.equals("cooking-pot") || icon.equals("package");
    }
}
