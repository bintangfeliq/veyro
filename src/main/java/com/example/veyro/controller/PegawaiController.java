
package com.example.veyro.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.veyro.model.User;
import com.example.veyro.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class PegawaiController {

    private final UserService userService;
    private final Path folder = Path.of("uploads", "profil").toAbsolutePath().normalize();

    public PegawaiController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/pegawai")
    public String pegawai(HttpSession session, Model model) {
        if (!"admin".equals(session.getAttribute("role"))) {
            return "redirect:/login";
        }
        List<User> kasir = new ArrayList<>();
        for (User user : userService.semuaUser()) {
            if ("kasir".equalsIgnoreCase(user.getRole())) {
                kasir.add(user);
            }
        }

        model.addAttribute("semuaUser", kasir);
        model.addAttribute("semuaKasir", kasir);
        model.addAttribute("currentUser", session.getAttribute("user"));
        model.addAttribute("activeMenu", "pegawai");
        return "admin/pegawai";
    }

    @PostMapping("/pegawai/simpan")
    public String simpanKasir(@RequestParam String name,
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam(value = "foto", required = false) MultipartFile foto,
            HttpSession session,
            RedirectAttributes redirect) {
        if (!"admin".equals(session.getAttribute("role"))) {
            return "redirect:/login";
        }

        if (name == null || name.isBlank() || username == null || username.isBlank() || password == null || password.isBlank()) {
            redirect.addFlashAttribute("pesanError", "Nama, username, dan password wajib diisi.");
            return "redirect:/admin/pegawai";
        }

        String usernameClean = username.trim();
        if (userService.cariByUsername(usernameClean).isPresent()) {
            redirect.addFlashAttribute("pesanError", "Username sudah digunakan oleh akun lain.");
            return "redirect:/admin/pegawai";
        }

        String namaFoto = null;
        if (foto != null && !foto.isEmpty()) {
            if (foto.getSize() > 2 * 1024 * 1024) {
                redirect.addFlashAttribute("pesanError", "Ukuran foto maksimal 2 MB.");
                return "redirect:/admin/pegawai";
            }
            String tipe = foto.getContentType();
            String ekstensi = "image/jpeg".equals(tipe) ? ".jpg" : "image/png".equals(tipe) ? ".png" : null;
            if (ekstensi == null) {
                redirect.addFlashAttribute("pesanError", "Foto harus berformat JPG atau PNG.");
                return "redirect:/admin/pegawai";
            }
            try {
                if (javax.imageio.ImageIO.read(foto.getInputStream()) == null) {
                    redirect.addFlashAttribute("pesanError", "File bukan gambar yang valid.");
                    return "redirect:/admin/pegawai";
                }
                Files.createDirectories(folder);
                namaFoto = UUID.randomUUID() + ekstensi;
                Path tujuan = folder.resolve(namaFoto).normalize();
                Files.copy(foto.getInputStream(), tujuan);
            } catch (IOException e) {
                redirect.addFlashAttribute("pesanError", "Gagal menyimpan foto kasir.");
                return "redirect:/admin/pegawai";
            }
        }

        User baru = new User();
        baru.setName(name.trim());
        baru.setUsername(usernameClean);
        baru.setPassword(password);
        baru.setRole("kasir");
        baru.setFoto(namaFoto);
        userService.simpanUser(baru);

        redirect.addFlashAttribute("pesanSukses", "Akun kasir baru berhasil ditambahkan.");
        return "redirect:/admin/pegawai";
    }

    @PostMapping("/pegawai/update")
    public String updateKasir(@RequestParam Long id,
            @RequestParam String name,
            @RequestParam String username,
            @RequestParam(value = "password", required = false) String password,
            @RequestParam(value = "foto", required = false) MultipartFile foto,
            HttpSession session,
            RedirectAttributes redirect) {
        if (!"admin".equals(session.getAttribute("role"))) {
            return "redirect:/login";
        }

        User kasir = userService.cariById(id).orElse(null);
        if (kasir == null || !"kasir".equalsIgnoreCase(kasir.getRole())) {
            redirect.addFlashAttribute("pesanError", "Akun kasir tidak ditemukan.");
            return "redirect:/admin/pegawai";
        }

        if (name == null || name.isBlank() || username == null || username.isBlank()) {
            redirect.addFlashAttribute("pesanError", "Nama dan username tidak boleh kosong.");
            return "redirect:/admin/pegawai";
        }

        String usernameClean = username.trim();
        java.util.Optional<User> cekUsername = userService.cariByUsername(usernameClean);
        if (cekUsername.isPresent() && !cekUsername.get().getId().equals(id)) {
            redirect.addFlashAttribute("pesanError", "Username sudah digunakan oleh akun lain.");
            return "redirect:/admin/pegawai";
        }

        if (foto != null && !foto.isEmpty()) {
            if (foto.getSize() > 2 * 1024 * 1024) {
                redirect.addFlashAttribute("pesanError", "Ukuran foto maksimal 2 MB.");
                return "redirect:/admin/pegawai";
            }
            String tipe = foto.getContentType();
            String ekstensi = "image/jpeg".equals(tipe) ? ".jpg" : "image/png".equals(tipe) ? ".png" : null;
            if (ekstensi == null) {
                redirect.addFlashAttribute("pesanError", "Foto harus berformat JPG atau PNG.");
                return "redirect:/admin/pegawai";
            }
            try {
                if (javax.imageio.ImageIO.read(foto.getInputStream()) == null) {
                    redirect.addFlashAttribute("pesanError", "File bukan gambar yang valid.");
                    return "redirect:/admin/pegawai";
                }
                Files.createDirectories(folder);
                String fotoBaru = UUID.randomUUID() + ekstensi;
                Path tujuan = folder.resolve(fotoBaru).normalize();

                if (kasir.getFoto() != null && !kasir.getFoto().isBlank()) {
                    try {
                        Files.deleteIfExists(folder.resolve(kasir.getFoto()).normalize());
                    } catch (IOException ignored) {
                    }
                }

                Files.copy(foto.getInputStream(), tujuan);
                userService.updateFoto(id, fotoBaru);
            } catch (IOException e) {
                redirect.addFlashAttribute("pesanError", "Gagal memperbarui foto kasir.");
                return "redirect:/admin/pegawai";
            }
        }

        userService.updateKasir(id, name.trim(), usernameClean, password);
        redirect.addFlashAttribute("pesanSukses", "Data kasir berhasil diperbarui.");
        return "redirect:/admin/pegawai";
    }

    @PostMapping("/pegawai/hapus/{id}")
    public String hapusKasir(@PathVariable Long id, HttpSession session, RedirectAttributes redirect) {
        if (!"admin".equals(session.getAttribute("role"))) {
            return "redirect:/login";
        }

        User kasir = userService.cariById(id).orElse(null);
        if (kasir == null || !"kasir".equalsIgnoreCase(kasir.getRole())) {
            redirect.addFlashAttribute("pesanError", "Akun kasir tidak ditemukan.");
            return "redirect:/admin/pegawai";
        }

        try {
            String namaFoto = kasir.getFoto();
            userService.hapusUser(id);
            if (namaFoto != null && !namaFoto.isBlank()) {
                try {
                    Files.deleteIfExists(folder.resolve(namaFoto).normalize());
                } catch (IOException ignored) {
                }
            }
            redirect.addFlashAttribute("pesanSukses", "Akun kasir berhasil dihapus.");
        } catch (Exception e) {
            redirect.addFlashAttribute("pesanError", "Akun kasir tidak dapat dihapus karena memiliki riwayat transaksi.");
        }
        return "redirect:/admin/pegawai";
    }

    @PostMapping("/profil/foto")
    public String fotoAdmin(@RequestParam("foto") MultipartFile foto,
            @RequestParam(value = "redirectUrl", required = false) String redirectUrl,
            HttpSession session,
            RedirectAttributes redirect) {
        if (!"admin".equals(session.getAttribute("role"))) {
            return "redirect:/login";
        }
        User admin = (User) session.getAttribute("user");
        if (admin == null) {
            return "redirect:/login";
        }
        return uploadFoto(admin.getId(), foto, session, redirect, redirectUrl);
    }

    @PostMapping("/profil/foto/hapus")
    public String hapusFotoAdmin(
            @RequestParam(value = "redirectUrl", required = false) String redirectUrl,
            HttpSession session,
            RedirectAttributes redirect) {
        if (!"admin".equals(session.getAttribute("role"))) {
            return "redirect:/login";
        }
        User admin = (User) session.getAttribute("user");
        if (admin == null) {
            return "redirect:/login";
        }
        return hapusFoto(admin.getId(), session, redirect, redirectUrl);
    }

    @PostMapping("/pegawai/{id}/foto")
    public String fotoKasir(@PathVariable Long id, @RequestParam("foto") MultipartFile foto, HttpSession session,
            RedirectAttributes redirect) {
        if (!"admin".equals(session.getAttribute("role"))) {
            return "redirect:/login";
        }
        User kasir = userService.cariById(id).orElse(null);
        if (kasir == null || !"kasir".equalsIgnoreCase(kasir.getRole())) {
            redirect.addFlashAttribute("pesanError", "Akun Kasir tidak ditemukan.");
            return "redirect:/admin/pegawai";
        }
        return uploadFoto(id, foto, session, redirect, "/admin/pegawai");
    }

    @PostMapping("/pegawai/{id}/foto/hapus")
    public String hapusFotoKasir(@PathVariable Long id, HttpSession session, RedirectAttributes redirect) {
        if (!"admin".equals(session.getAttribute("role"))) {
            return "redirect:/login";
        }
        User kasir = userService.cariById(id).orElse(null);
        if (kasir == null || !"kasir".equalsIgnoreCase(kasir.getRole())) {
            redirect.addFlashAttribute("pesanError", "Akun Kasir tidak ditemukan.");
            return "redirect:/admin/pegawai";
        }
        return hapusFoto(id, session, redirect, "/admin/pegawai");
    }

    private String uploadFoto(Long id, MultipartFile foto, HttpSession session, RedirectAttributes redirect,
            String redirectUrl) {
        String halaman = (redirectUrl != null && !redirectUrl.isBlank()) ? redirectUrl : "/admin/dashboard";
        if (foto == null || foto.isEmpty() || foto.getSize() > 2 * 1024 * 1024) {
            redirect.addFlashAttribute("pesanError", "Foto wajib diisi dan maksimal 2 MB.");
            return "redirect:" + halaman;
        }
        String tipe = foto.getContentType();
        String ekstensi = "image/jpeg".equals(tipe) ? ".jpg" : "image/png".equals(tipe) ? ".png" : null;

        if (ekstensi == null) {
            redirect.addFlashAttribute("pesanError", "Foto harus JPG atau PNG.");
            return "redirect:" + halaman;
        }

        String namaFoto = UUID.randomUUID() + ekstensi;
        Path tujuan = folder.resolve(namaFoto).normalize();
        try {
            if (javax.imageio.ImageIO.read(foto.getInputStream()) == null) {
                redirect.addFlashAttribute("pesanError", "File bukan gambar yang valid.");
                return "redirect:" + halaman;
            }
            Files.createDirectories(folder);

            User userLama = userService.cariById(id).orElse(null);
            if (userLama != null && userLama.getFoto() != null && !userLama.getFoto().isBlank()) {
                try {
                    Files.deleteIfExists(folder.resolve(userLama.getFoto()).normalize());
                } catch (IOException ignored) {
                }
            }

            Files.copy(foto.getInputStream(), tujuan);
            userService.updateFoto(id, namaFoto);

            User user = userService.cariById(id).orElse(null);
            User userSession = (User) session.getAttribute("user");
            if (user != null && userSession != null && user.getId().equals(userSession.getId())) {
                session.setAttribute("user", user);
            }
            redirect.addFlashAttribute("pesanSukses", "Foto profil berhasil diperbarui.");
        } catch (IOException | RuntimeException e) {
            try {
                Files.deleteIfExists(tujuan);
            } catch (IOException ignored) {
            }
            redirect.addFlashAttribute("pesanError", "Foto gagal disimpan.");
        }
        return "redirect:" + halaman;
    }

    private String hapusFoto(Long id, HttpSession session, RedirectAttributes redirect, String redirectUrl) {
        String halaman = (redirectUrl != null && !redirectUrl.isBlank()) ? redirectUrl : "/admin/dashboard";
        User user = userService.cariById(id).orElse(null);
        if (user != null && user.getFoto() != null && !user.getFoto().isBlank()) {
            try {
                Files.deleteIfExists(folder.resolve(user.getFoto()).normalize());
            } catch (IOException ignored) {
            }
            userService.updateFoto(id, null);
            user.setFoto(null);
            User userSession = (User) session.getAttribute("user");
            if (userSession != null && user.getId().equals(userSession.getId())) {
                session.setAttribute("user", user);
            }
            redirect.addFlashAttribute("pesanSukses", "Foto profil berhasil dihapus.");
        } else {
            redirect.addFlashAttribute("pesanError", "Tidak ada foto profil untuk dihapus.");
        }
        return "redirect:" + halaman;
    }
}
