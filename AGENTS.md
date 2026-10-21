# Veyro Workspace Rules & Design System

Seluruh agen AI yang bekerja pada repository **Veyro** WAJIB mematuhi panduan dan aturan berikut tanpa pengecualian.

## 1. Konsep Utama
Veyro adalah aplikasi kasir / Point of Sale (POS) berbasis web:
- **Konsep:** Modern POS × Minimal Premium × Clean Dashboard
- **Formula:** BLACK + MINT + CLEAN + MINIMAL + PREMIUM + FUNCTIONAL
- **Hindari:** Tampilan template AI murah, gradasi berlebihan, border tebal, warna-warni ramai, background gambar di halaman dashboard, animasi lambat.

## 2. Color Tokens Standar
```css
:root {
    --veyro-black: #0b0b0b;
    --veyro-dark: #111111;
    --veyro-dark-2: #171717;
    --veyro-card: #1a1a1a;

    --veyro-mint: #65E6AD;
    --veyro-mint-dark: #5de0ae;

    --veyro-white: #ffffff;
    --veyro-text: #f5f5f5;
    --veyro-muted: #a3a3a3;
    --veyro-border: #2a2a2a;

    --veyro-success: #55d68a;
    --veyro-danger: #ff5c5c;
    --veyro-warning: #f5c451;
}
```

## 3. Aturan Komponen & Layout
1. **Background:** `var(--veyro-black)` untuk body, `var(--veyro-card)` untuk kartu.
2. **Accent Mint (`#65E6AD`):** Hanya untuk tombol utama, link aktif, highlight, fokus state, dan status terpilih.
3. **Typography:** `Inter`, `system-ui`, `-apple-system`, `sans-serif`.
4. **Border Radius:**
   - Button & Input: `8-10px`
   - Card: `12-16px`
   - Modal: `14-18px`
   - Badge: `6-8px`
5. **Icon:** Utamakan Bootstrap Icons (`bi bi-*`).
6. **Form:** Label selalu di atas input (bukan sekadar placeholder).
7. **Role & Akses:**
   - `admin`: Mengelola produk, kategori, kasir, dan laporan.
   - `kasir`: Transaksi POS, keranjang belanja, checkout/bayar.
   - Form tambah kasir otomatis ber-role `kasir` (tanpa opsi dropdown admin).
8. **Integrasi Backend:**
   - Pertahankan struktur Thymeleaf yang sudah ada (`th:each`, `th:if`, `th:text`, `th:action`, dll.).
   - UI selalu mengikuti backend yang sudah ada, jangan mengubah nama variabel/controller/route hanya demi styling.

## 4. Aturan Penulisan Kode

### A. Jangan Menggunakan Komentar Kode
- Jangan menambahkan komentar di dalam kode (HTML, CSS, JavaScript, maupun Java).
- Jangan menggunakan komentar satu baris (`//`) maupun banyak baris (`/* ... */`, `<!-- ... -->`).
- Hapus komentar lama yang tidak diperlukan ketika mengedit kode.
- Jangan menghapus komentar dokumentasi/lisensi yang diwajibkan oleh library.

### B. Jangan Membuat Kode yang Rumit (Tingkat Pemula / Siswa SMK RPL)
- Gunakan logika yang sederhana, pendek, dan mudah dipahami.
- Gunakan nama variabel dan method yang jelas.
- Hindari method yang terlalu panjang, nested if bertingkat, dan ternary bertingkat.
- Hindari Stream API / lambda jika for-loop biasa lebih mudah dipahami.
- Hindari abstraksi, class, interface, service, atau helper tambahan yang tidak diperlukan.
- Jangan menambahkan dependency atau library baru tanpa alasan kuat.

### C. Jangan Mengubah Struktur Tanpa Kebutuhan
- Pertahankan controller, service, repository, model, template, dan route yang sudah ada.
- Jangan membuat file baru jika bisa dikerjakan pada file yang ada.
- Jangan melakukan refactor besar saat diminta perubahan kecil.

### D. Aturan Output
- Sebutkan lokasi file yang harus diubah.
- Berikan kode lengkap jika diminta implementasi.
- Tanpa komentar dalam kode.
- Penjelasan alur kode disampaikan di luar blok kode.

## 5. Aturan Emoji Kategori Produk

- Kategori produk Veyro menggunakan **emoji berwarna**, bukan nama ikon garis Lucide (seperti `utensils`, `package`, atau `cup-soda`).
- Emoji disimpan langsung dalam kolom `icon` pada entity `Category`.
- Gunakan emoji Unicode, bukan nama teks ikon.
- Jangan menambahkan library ikon hanya untuk menampilkan emoji.
- Emoji harus bisa disimpan, diedit, dan ditampilkan kembali dari database.
- Jangan mengganti emoji pilihan admin secara otomatis.
- Saat membuat fitur tambah atau edit kategori, sediakan pilihan emoji visual agar admin tidak mengetik kode emoji secara manual.
- Saat menampilkan kategori, gunakan emoji yang tersimpan di database. Jika `icon` kosong, gunakan emoji cadangan `📦`.

### 8 Kategori Default & Emoji
| Nama Kategori | Emoji |
|---|---|
| Makanan | 🍜 |
| Minuman | 🥤 |
| Permen & Cokelat | 🍬 |
| Perawatan Diri | 🧴 |
| Kebutuhan Rumah | 🧹 |
| Sayur & Buah | 🥕 |
| Bahan Pokok | 🌾 |
| Lainnya | 📦 |

Kategori default hanya ditambahkan jika belum tersedia. Jangan membuat data duplikat ketika aplikasi dijalankan ulang. Jika kategori sudah tersimpan, jangan menimpa perubahan nama atau emoji yang dilakukan admin tanpa instruksi.
