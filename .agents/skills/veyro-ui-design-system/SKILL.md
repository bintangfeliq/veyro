---
name: veyro-ui-design-system
description: Standar sistem desain visual UI untuk aplikasi kasir/POS Veyro. Menjaga konsistensi tema dark mode, accent mint (#8fffd2), layout clean, minimal, fungsional, dan kompatibilitas Thymeleaf/Bootstrap.
---

# Veyro UI Design System Skill

## 1. Tujuan
Skill ini digunakan untuk menjaga agar seluruh halaman website **Veyro** memiliki tampilan yang konsisten.
Veyro adalah aplikasi kasir/POS berbasis web dengan konsep:
- Minimal, Premium, Modern, Clean, Professional, Mudah digunakan.
- Tidak terlihat seperti template AI.
- Cocok untuk aplikasi kasir toko/minimarket.

Setiap kali membuat halaman baru, memperbaiki halaman lama, atau menambahkan komponen UI, **WAJIB mengikuti aturan dalam skill ini**.

---

## 2. Identitas Veyro
- **Nama:** Veyro
- **Karakter Visual:** Modern POS × Minimal Premium × Clean Dashboard
- **Hindari:** Terlalu ramai, banyak gradient/animasi/border, template AI generik.

---

## 3. Warna Utama
```css
:root {
    --veyro-black: #0b0b0b;
    --veyro-dark: #111111;
    --veyro-dark-2: #171717;
    --veyro-card: #1a1a1a;

    --veyro-mint: #8fffd2;
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

- **Background:** `var(--veyro-black)`
- **Card:** `var(--veyro-card)`
- **Accent Mint:** `var(--veyro-mint)` untuk tombol utama, link aktif, highlight, status tertentu, focus state.

---

## 4. Typography
```css
font-family: Inter, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
```
- **h1:** 32px, font-weight 700
- **h2:** 24px, font-weight 600
- **h3:** 18px, font-weight 600
- **Text Normal:** 14px, `var(--veyro-text)`
- **Text Muted:** `var(--veyro-muted)`

---

## 5. Komponen UI
- **Card:** `background: var(--veyro-card); border: 1px solid var(--veyro-border); border-radius: 14px;`
- **Button Primary:** `background: var(--veyro-mint); color: var(--veyro-black); border: none; border-radius: 8-10px;`
- **Button Secondary:** `background: var(--veyro-dark-2); color: var(--veyro-text); border: 1px solid var(--veyro-border);`
- **Form Control:** `background: var(--veyro-dark-2); color: var(--veyro-white); border: 1px solid var(--veyro-border); border-radius: 10px;` (Focus border mint).
- **Icons:** Bootstrap Icons (`bi bi-*`).

---

## 6. Role & Hak Akses
- **Admin:** Dashboard admin, kelola produk, kategori, kasir, dan laporan.
- **Kasir:** Dashboard kasir, transaksi POS, keranjang, bayar.
- Form tambah kasir otomatis ber-role `kasir` (tanpa dropdown role admin).

---

## 7. Aturan Penulisan Kode (Section 48)

### A. Jangan Menggunakan Komentar Kode
- Jangan menambahkan komentar di dalam kode (HTML, CSS, JavaScript, maupun Java).
- Jangan menggunakan komentar satu baris (`//`) maupun banyak baris (`/* ... */`, `<!-- ... -->`).
- Hapus komentar lama yang tidak diperlukan ketika mengedit kode.
- Jangan menghapus komentar dokumentasi/lisensi yang diwajibkan oleh library.

### B. Jangan Membuat Kode yang Rumit (Tingkat Pemula / Siswa SMK RPL)
- Gunakan kode yang sederhana, pendek, dan mudah dipahami.
- Gunakan logika yang mudah dibaca dan nama variabel/method yang jelas.
- Hindari method panjang, nested if bertingkat, dan ternary bertingkat.
- Hindari Stream API / lambda jika perulangan biasa lebih mudah dipahami.
- Hindari membuat class, interface, service, atau helper tambahan jika tidak benar-benar dibutuhkan.

### C. Jangan Mengubah Struktur Tanpa Kebutuhan
- Pertahankan controller, service, repository, model, template, dan route yang sudah ada.
- Jangan membuat file baru jika bisa dikerjakan pada file yang ada.
- Jangan mengubah backend jika hanya meminta perubahan UI.

### D. Aturan Output
- Sebutkan lokasi file yang diubah.
- Berikan kode lengkap jika diminta implementasi.
- Tanpa komentar di dalam kode.
- Penjelasan alur kode disampaikan di luar blok kode.
