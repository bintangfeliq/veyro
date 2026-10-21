# Veyro Development Rules

## 1. Identitas Veyro
- Veyro adalah aplikasi kasir untuk berbagai jenis toko.
- Gunakan desain minimal, modern, dan profesional.
- Warna utama: hitam, putih, dan mint green.
- Warna mint green utama: `#65E6AD`.
- Pertahankan konsistensi desain di semua halaman.
- Jangan mengubah identitas visual tanpa permintaan.

## 2. Aturan Backend
- Gunakan Java Spring Boot, PostgreSQL, dan Thymeleaf.
- Gunakan struktur package dan pola kode yang sudah ada.
- Periksa kode yang sudah ada sebelum membuat atau mengubah file.
- Jangan membuat database baru jika database `veyro` sudah digunakan.
- Jangan membuat tabel atau kolom duplikat.
- Utamakan kode sederhana, mudah dipahami pemula, dan tidak berlebihan.
- Hindari abstraksi, class, dan method tambahan yang tidak diperlukan.
- Jangan menambahkan komentar kode yang tidak diperlukan.
- Gunakan `@GetMapping` dan `@PostMapping` sesuai kebutuhan.
- Jangan membuat API atau endpoint baru jika fitur dapat dibuat melalui form Thymeleaf.
- Jangan mengubah file yang tidak berhubungan dengan tugas.
- Jangan menghapus fitur yang sudah berfungsi.

## 3. Aturan Emoji Kategori
- Kategori produk Veyro menggunakan emoji berwarna, bukan ikon garis Lucide.
- Emoji disimpan langsung dalam kolom `icon` pada entity `Category`.
- Gunakan emoji Unicode, bukan nama ikon seperti `utensils`, `package`, atau `cup-soda`.
- Jangan menambahkan library ikon hanya untuk menampilkan emoji.
- Emoji harus bisa disimpan, diedit, dan ditampilkan kembali dari database.
- Jangan mengganti emoji pilihan admin secara otomatis.
- Saat membuat fitur tambah atau edit kategori, sediakan pilihan emoji visual.
- Admin tidak perlu mengetik kode emoji secara manual.
- Simpan emoji yang dipilih ke kolom `icon`.
- Saat menampilkan kategori, gunakan emoji yang tersimpan di database.
- Jika `icon` kosong, gunakan emoji cadangan `📦`.
- Pastikan emoji tidak diubah menjadi teks nama ikon.

## 4. Emoji Kategori Default
| Nama kategori | Emoji |
|---|---|
| Makanan | 🍜 |
| Minuman | 🥤 |
| Permen & Cokelat | 🍬 |
| Perawatan Diri | 🧴 |
| Kebutuhan Rumah | 🧹 |
| Sayur & Buah | 🥕 |
| Bahan Pokok | 🌾 |
| Lainnya | 📦 |

Kategori default hanya ditambahkan jika belum tersedia. Jangan membuat data duplikat ketika aplikasi dijalankan ulang.
Jika kategori sudah tersimpan, jangan menimpa perubahan nama atau emoji yang dilakukan admin tanpa instruksi.

## 5. Aturan Frontend
- Jangan membuat halaman HTML sebelum diminta.
- Saat halaman dibuat, tampilkan emoji langsung dari data kategori.
- Gunakan emoji berwarna yang konsisten dan mudah dikenali.
- Pertahankan tema hitam dan mint green Veyro.
- Hindari desain yang ramai, terlalu banyak warna, atau berlebihan.
- Gunakan Bootstrap yang sudah ada jika proyek menggunakannya.
- Jangan menambahkan library frontend baru tanpa alasan yang jelas.

## 6. Cara Bekerja
1. Periksa file dan struktur proyek sebelum mengubah kode.
2. Pahami implementasi yang sudah ada.
3. Kerjakan hanya fitur yang diminta.
4. Berikan kode lengkap untuk file yang perlu dibuat atau diubah.
5. Jelaskan lokasi file dengan tepat.
6. Pastikan perubahan tidak merusak fitur sebelumnya.
7. Jangan mengklaim pengujian berhasil jika belum dijalankan.
8. Jika ada informasi penting yang belum diketahui, tanyakan sebelum membuat asumsi berisiko.
