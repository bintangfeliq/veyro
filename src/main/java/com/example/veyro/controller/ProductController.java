package com.example.veyro.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.veyro.model.Category;
import com.example.veyro.model.Product;
import com.example.veyro.service.CategoryService;
import com.example.veyro.service.ProductService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/product")
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final Path folderProduct = Path.of("uploads", "product").toAbsolutePath().normalize();

    public ProductController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public String halamanProduct(@RequestParam(defaultValue = "1") int page, HttpSession session, Model model) {
        if (bukanAdmin(session)) {
            return "redirect:/login";
        }
        Page<Product> produkPage = productService.cariSemuaProduct(Math.max(page - 1, 0), 15);

        model.addAttribute("daftarProduct", produkPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", produkPage.getTotalPages());
        model.addAttribute("semuaCategory", categoryService.semuaCategory());
        model.addAttribute("kodeBaruOtomatis", productService.generateKodeProduct());
        model.addAttribute("currentUser", session.getAttribute("user"));
        model.addAttribute("activeMenu", "produk");
        return "admin/product";
    }

    @GetMapping("/tambah")
    public String halamanTambah(HttpSession session) {
        return bukanAdmin(session) ? "redirect:/login" : "redirect:/product?tambah=true";
    }

    @PostMapping("/simpan")
    public String simpanProduct(@RequestParam(required = false) String code, @RequestParam String name, @RequestParam BigDecimal price, @RequestParam Integer stock, @RequestParam Long categoryId, @RequestParam(value = "foto", required = false) MultipartFile foto, HttpSession session, RedirectAttributes redirect) {
        if (bukanAdmin(session)) {
            return "redirect:/login";
        }
        String error = validasiInput(name, price, categoryId);
        if (error != null) {
            return kembali(redirect, "pesanError", error);
        }
        if (stock == null || stock < 0) {
            return kembali(redirect, "pesanError", "Stok tidak boleh kurang dari 0.");
        }

        String kodeFinal;
        if (code != null && !code.isBlank()) {
            kodeFinal = code.trim().toUpperCase();
            if (productService.cariKodeProduct(kodeFinal).isPresent()) {
                return kembali(redirect, "pesanError", "Kode produk sudah digunakan.");
            }
        } else {
            kodeFinal = productService.generateKodeProduct();
        }

        String namaFoto = prosesUploadFoto(foto, redirect);
        if ("".equals(namaFoto)) {
            return "redirect:/product";
        }

        Category category = categoryService.cariById(categoryId).get();
        Product product = new Product();
        product.setCode(kodeFinal);
        product.setName(name.trim());
        product.setPrice(price);
        product.setStok(stock);
        product.setCategory(category);
        product.setFotoProduk(namaFoto);
        productService.simpanProduct(product);

        redirect.addFlashAttribute("tipeNotif", "tambah");
        return kembali(redirect, "pesanSukses", "Produk berhasil ditambahkan.");
    }

    @PostMapping("/update")
    public String updateProduct(@RequestParam Long id, @RequestParam(required = false) String code, @RequestParam String name, @RequestParam BigDecimal price, @RequestParam Long categoryId, @RequestParam(value = "foto", required = false) MultipartFile foto, HttpSession session, RedirectAttributes redirect) {
        if (bukanAdmin(session)) {
            return "redirect:/login";
        }
        Product product = cariProduk(id);
        if (product == null) {
            return kembali(redirect, "pesanError", "Produk tidak ditemukan.");
        }
        String error = validasiInput(name, price, categoryId);
        if (error != null) {
            return kembali(redirect, "pesanError", error);
        }

        String kodeFinal = (code != null && !code.isBlank()) ? code.trim().toUpperCase() : product.getCode();
        if (productService.cariKodeProduct(kodeFinal, id).isPresent()) {
            return kembali(redirect, "pesanError", "Kode produk sudah digunakan.");
        }

        String fotoBaru = prosesUploadFoto(foto, redirect);
        if ("".equals(fotoBaru)) {
            return "redirect:/product";
        }

        String fotoLama = product.getFotoProduk();
        product.setCode(kodeFinal);
        product.setName(name.trim());
        product.setPrice(price);
        product.setCategory(categoryService.cariById(categoryId).get());
        if (fotoBaru != null) {
            product.setFotoProduk(fotoBaru);
        }
        productService.simpanProduct(product);
        if (fotoBaru != null) {
            hapusFileFoto(fotoLama);
        }

        redirect.addFlashAttribute("tipeNotif", "update");
        return kembali(redirect, "pesanSukses", "Produk berhasil diperbarui.");
    }

    @PostMapping("/tambah-stok")
    public String tambahStok(@RequestParam Long id, @RequestParam Integer jumlah, HttpSession session, RedirectAttributes redirect) {
        if (bukanAdmin(session)) {
            return "redirect:/login";
        }
        Product product = cariProduk(id);
        if (product == null) {
            return kembali(redirect, "pesanError", "Produk tidak ditemukan.");
        }
        if (jumlah == null || jumlah <= 0) {
            return kembali(redirect, "pesanError", "Jumlah stok yang ditambahkan harus lebih dari 0.");
        }

        int stokLama = product.getStok() != null ? product.getStok() : 0;
        product.setStok(stokLama + jumlah);
        productService.simpanProduct(product);

        redirect.addFlashAttribute("tipeNotif", "tambah");
        return kembali(redirect, "pesanSukses", "Stok berhasil ditambahkan.");
    }

    @PostMapping("/hapus/{id}")
    public String hapusProduct(@PathVariable Long id, HttpSession session, RedirectAttributes redirect) {
        if (bukanAdmin(session)) {
            return "redirect:/login";
        }
        Product product = cariProduk(id);
        if (product == null) {
            return kembali(redirect, "pesanError", "Produk tidak ditemukan.");
        }

        String fotoProduk = product.getFotoProduk();
        try {
            productService.hapusProduct(id);
            hapusFileFoto(fotoProduk);
            redirect.addFlashAttribute("tipeNotif", "hapus");
            return kembali(redirect, "pesanSukses", "Produk berhasil dihapus.");
        } catch (Exception e) {
            return kembali(redirect, "pesanError", "Produk tidak bisa dihapus karena sudah memiliki riwayat transaksi.");
        }
    }

    private boolean bukanAdmin(HttpSession session) {
        return !"admin".equalsIgnoreCase((String) session.getAttribute("role"));
    }

    private String kembali(RedirectAttributes redirect, String kunci, String pesan) {
        redirect.addFlashAttribute(kunci, pesan);
        return "redirect:/product";
    }

    private Product cariProduk(Long id) {
        return id == null ? null : productService.cariById(id).orElse(null);
    }

    private String validasiInput(String name, BigDecimal price, Long categoryId) {
        if (name == null || name.isBlank()) {
            return "Nama produk wajib diisi.";
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            return "Harga tidak boleh negatif.";
        }
        if (categoryId == null || categoryService.cariById(categoryId).isEmpty()) {
            return "Kategori tidak ditemukan.";
        }
        return null;
    }

    private String prosesUploadFoto(MultipartFile foto, RedirectAttributes redirect) {
        if (foto == null || foto.isEmpty()) {
            return null;
        }
        if (foto.getSize() > 2 * 1024 * 1024) {
            redirect.addFlashAttribute("pesanError", "Ukuran foto produk maksimal 2 MB.");
            return "";
        }
        String tipe = foto.getContentType();
        String ekstensi = null;
        if ("image/jpeg".equalsIgnoreCase(tipe) || "image/jpg".equalsIgnoreCase(tipe)) {
            ekstensi = ".jpg";
        } else if ("image/png".equalsIgnoreCase(tipe)) {
            ekstensi = ".png";
        }
        if (ekstensi == null) {
            redirect.addFlashAttribute("pesanError", "Foto produk harus berformat JPG, JPEG, atau PNG.");
            return "";
        }
        try {
            if (ImageIO.read(foto.getInputStream()) == null) {
                redirect.addFlashAttribute("pesanError", "File bukan gambar yang valid.");
                return "";
            }
            Files.createDirectories(folderProduct);
            String namaFoto = UUID.randomUUID() + ekstensi;
            Files.copy(foto.getInputStream(), folderProduct.resolve(namaFoto).normalize());
            return namaFoto;
        } catch (IOException e) {
            redirect.addFlashAttribute("pesanError", "Gagal menyimpan foto produk.");
            return "";
        }
    }

    private void hapusFileFoto(String namaFoto) {
        if (namaFoto == null || namaFoto.isBlank() || productService.hitungPenggunaFoto(namaFoto) != 0) {
            return;
        }
        try {
            Files.deleteIfExists(folderProduct.resolve(namaFoto).normalize());
        } catch (IOException ignored) {
        }
    }
}
