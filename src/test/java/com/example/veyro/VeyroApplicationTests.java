package com.example.veyro;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.example.veyro.model.Category;
import com.example.veyro.model.Product;
import com.example.veyro.model.Transaksi;
import com.example.veyro.model.User;
import com.example.veyro.repository.CategoryRepository;
import com.example.veyro.repository.ProductRepository;
import com.example.veyro.repository.TransaksiRepository;
import com.example.veyro.repository.UserRepository;

@SpringBootTest
class VeyroApplicationTests {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TransaksiRepository transaksiRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    private MockHttpSession sessionAdmin() {
        MockHttpSession session = new MockHttpSession();
        User admin = userRepository.findByUsername("admin.veyro").orElseThrow();
        session.setAttribute("role", "admin");
        session.setAttribute("user", admin);
        return session;
    }

    private MockHttpSession sessionKasir() {
        MockHttpSession session = new MockHttpSession();
        User kasir = new User();
        kasir.setName("Kasir Test");
        kasir.setUsername("kasir.test.readiness");
        kasir.setPassword("password");
        kasir.setRole("kasir");
        kasir = userRepository.save(kasir);
        session.setAttribute("role", "kasir");
        session.setAttribute("user", kasir);
        return session;
    }

    @Test
    void testKoneksiDatabaseDanSeeder() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            assertTrue(conn.isValid(2));
        }
        assertTrue(userRepository.findByUsername("admin.veyro").isPresent());
        assertTrue(userRepository.findByUsername("bintang.admin").isPresent());
        assertTrue(categoryRepository.count() >= 8);
    }

    @Test
    @Transactional
    void testRenderSemuaHalamanDanAsetStatis() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));

        MockHttpSession adminSession = sessionAdmin();
        mockMvc.perform(get("/admin/dashboard").session(adminSession))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"));
        mockMvc.perform(get("/admin/transaksi").session(adminSession))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/transaksi"));
        mockMvc.perform(get("/admin/transaksi").session(adminSession).param("tipeFilter", "bulan").param("bulan", "2026-11"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/transaksi"));
        mockMvc.perform(get("/admin/laporan").session(adminSession))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/laporan"));
        mockMvc.perform(get("/admin/laporan").session(adminSession).param("tipeFilter", "custom").param("tanggalMulai", "2026-11-01").param("tanggalSelesai", "2026-11-23"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/laporan"));
        mockMvc.perform(get("/category").session(adminSession))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/category"));
        mockMvc.perform(get("/product").session(adminSession))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/product"));
        mockMvc.perform(get("/admin/pegawai").session(adminSession))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/pegawai"));

        MockHttpSession kasirSession = sessionKasir();

        mockMvc.perform(get("/kasir/dashboard").session(kasirSession))
                .andExpect(status().isOk())
                .andExpect(view().name("kasir/dashboard"));
        mockMvc.perform(get("/kasir/riwayat").session(kasirSession))
                .andExpect(status().isOk())
                .andExpect(view().name("kasir/riwayat"));
        mockMvc.perform(get("/kasir/riwayat").session(kasirSession).param("tipeFilter", "bulan").param("bulan", "2026-11"))
                .andExpect(status().isOk())
                .andExpect(view().name("kasir/riwayat"));

        for (String asset : List.of("/css/admin.css", "/css/kasir.css", "/css/login.css", "/images/sideAtas.png", "/images/logoBrow.png", "/images/produkosong.webp", "/images/transaksikosong.webp")) {
            mockMvc.perform(get(asset)).andExpect(status().isOk());
        }
    }

    @Test
    @Transactional
    void testAutentikasiDanProteksiSesi() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        mockMvc.perform(get("/kasir/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        mockMvc.perform(post("/login")
                .param("username", "admin.veyro")
                .param("password", "salah"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attributeExists("pesanError"));

        MvcResult loginRes = mockMvc.perform(post("/login")
                .param("username", "admin.veyro")
                .param("password", "veryo.password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/dashboard"))
                .andReturn();

        assertEquals("admin", loginRes.getRequest().getSession().getAttribute("role"));

        MockHttpSession kasirSession = sessionKasir();
        mockMvc.perform(get("/admin/dashboard").session(kasirSession))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
        mockMvc.perform(get("/category").session(kasirSession))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
        mockMvc.perform(get("/product").session(kasirSession))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        MockHttpSession adminSession = sessionAdmin();
        mockMvc.perform(get("/kasir/dashboard").session(adminSession))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
        mockMvc.perform(post("/admin/profil/foto/hapus").session(adminSession)
                .param("redirectUrl", "https://evil.example"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/dashboard"));
    }

    @Test
    @Transactional
    void testAlurKategoriProdukStokDanTransaksi() throws Exception {
        MockHttpSession adminSession = sessionAdmin();

        mockMvc.perform(post("/category/simpan").session(adminSession)
                .param("name", "Kategori Readiness Test")
                .param("icon", "🧪"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/category"));

        Category kat = categoryRepository.findByNameIgnoreCase("Kategori Readiness Test").orElse(null);
        assertNotNull(kat);

        mockMvc.perform(post("/category/update").session(adminSession)
                .param("id", kat.getId().toString())
                .param("name", "Kategori Readiness Updated")
                .param("icon", "📦"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/category"));

        kat = categoryRepository.findById(kat.getId()).orElseThrow();
        assertEquals("Kategori Readiness Updated", kat.getName());

        mockMvc.perform(post("/product/simpan").session(adminSession)
                .param("code", "RDY001")
                .param("name", "Produk Readiness")
                .param("price", "15000")
                .param("stock", "10")
                .param("categoryId", kat.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/product"));

        Product prod = productRepository.findByCodeIgnoreCase("RDY001").orElse(null);
        assertNotNull(prod);
        assertEquals(10, prod.getStok());

        mockMvc.perform(post("/product/update").session(adminSession)
                .param("id", prod.getId().toString())
                .param("code", "RDY001")
                .param("name", "Produk Readiness Edited")
                .param("price", "15000")
                .param("categoryId", kat.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/product"));

        prod = productRepository.findById(prod.getId()).orElseThrow();
        assertEquals("Produk Readiness Edited", prod.getName());

        mockMvc.perform(post("/product/tambah-stok").session(adminSession)
                .param("id", prod.getId().toString())
                .param("jumlah", "5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/product"));

        prod = productRepository.findById(prod.getId()).orElseThrow();
        assertEquals(15, prod.getStok());

        mockMvc.perform(post("/admin/pegawai/simpan").session(adminSession)
                .param("name", "Kasir Baru Test")
                .param("username", "kasir.baru.test")
                .param("password", "rahasia123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/pegawai"));

        User kasirBaru = userRepository.findByUsername("kasir.baru.test").orElse(null);
        assertNotNull(kasirBaru);
        assertEquals("kasir", kasirBaru.getRole());

        mockMvc.perform(post("/admin/pegawai/hapus/{id}", kasirBaru.getId()).session(adminSession))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/pegawai"));
        assertTrue(userRepository.findById(kasirBaru.getId()).isEmpty());

        MockHttpSession kasirSession = sessionKasir();
        mockMvc.perform(post("/transaksi/simpan").session(kasirSession)
                .param("productId", prod.getId().toString())
                .param("jumlah", "3")
                .param("bayar", "1000")
                .param("metodePembayaran", "Tunai"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/kasir/dashboard"))
                .andExpect(flash().attributeExists("pesanError"));

        prod = productRepository.findById(prod.getId()).orElseThrow();
        assertEquals(15, prod.getStok());

        mockMvc.perform(post("/transaksi/simpan").session(kasirSession)
                .param("productId", prod.getId().toString())
                .param("jumlah", "3")
                .param("bayar", "50000")
                .param("metodePembayaran", "Tunai"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/kasir/dashboard"))
                .andExpect(flash().attributeExists("transaksiSuksesKode"));

        prod = productRepository.findById(prod.getId()).orElseThrow();
        assertEquals(12, prod.getStok());

        List<Transaksi> transaksiKasir = transaksiRepository.findByKasirOrderByTanggalTransaksiDesc((User) kasirSession.getAttribute("user"));
        assertEquals(1, transaksiKasir.size());
        assertEquals(0, new BigDecimal("45000").compareTo(transaksiKasir.get(0).getTotal()));
        assertEquals(0, new BigDecimal("50000").compareTo(transaksiKasir.get(0).getPembayaran()));
        assertEquals(0, new BigDecimal("5000").compareTo(transaksiKasir.get(0).getKembalian()));

        mockMvc.perform(post("/category/simpan").session(adminSession)
                .param("name", "Kategori Hapus Test")
                .param("icon", "🗑️"))
                .andExpect(status().is3xxRedirection());
        Category katHapus = categoryRepository.findByNameIgnoreCase("Kategori Hapus Test").orElseThrow();
        mockMvc.perform(post("/category/hapus/{id}", katHapus.getId()).session(adminSession))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/category"));
        assertTrue(categoryRepository.findById(katHapus.getId()).isEmpty());
    }
}
