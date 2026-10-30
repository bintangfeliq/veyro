package com.example.veyro.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.imageio.ImageIO;

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
        if (bukanAdmin(session)) {
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
        if (bukanAdmin(session)) {
            return "redirect:/login";
        }

        if (name == null || name.isBlank() || username == null || username.isBlank() || password == null || password.isBlank()) {
            redirect.addFlashAttribute("pesanError", "Nama, username, dan password wajib diisi.");
            return "redirect:/admin/pegawai";
        }

        String usernameClean = username.trim();
        if (usernameSudahAda(usernameClean, null)) {
            redirect.addFlashAttribute("pesanError", "Username sudah digunakan oleh akun lain.");
            return "redirect:/admin/pegawai";
        }

        String namaFoto = simpanFileFoto(foto, redirect);
        if ("".equals(namaFoto)) {
            return "redirect:/admin/pegawai";
        }

        User baru = new User();
        baru.setName(name.trim());
        baru.setUsername(usernameClean);
        baru.setPassword(password);
        baru.setRole("kasir");
        baru.setFoto(namaFoto);
        userService.simpanUser(baru);

        redirect.addFlashAttribute("pesanSukses", "Akun kasir baru berhasil ditambahkan.");
        redirect.addFlashAttribute("tipeNotif", "tambah");
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
        if (bukanAdmin(session)) {
            return "redirect:/login";
        }

        User kasir = cariKasir(id);
        if (kasir == null) {
            redirect.addFlashAttribute("pesanError", "Akun kasir tidak ditemukan.");
            return "redirect:/admin/pegawai";
        }

        if (name == null || name.isBlank() || username == null || username.isBlank()) {
            redirect.addFlashAttribute("pesanError", "Nama dan username tidak boleh kosong.");
            return "redirect:/admin/pegawai";
        }

        String usernameClean = username.trim();
        if (usernameSudahAda(usernameClean, id)) {
            redirect.addFlashAttribute("pesanError", "Username sudah digunakan oleh akun lain.");
            return "redirect:/admin/pegawai";
        }

        String fotoBaru = simpanFileFoto(foto, redirect);
        if ("".equals(fotoBaru)) {
            return "redirect:/admin/pegawai";
        }

        if (fotoBaru != null) {
            updateFotoUser(kasir, fotoBaru, session);
        }

        userService.updateKasir(id, name.trim(), usernameClean, password);
        redirect.addFlashAttribute("pesanSukses", "Data kasir berhasil diperbarui.");
        redirect.addFlashAttribute("tipeNotif", "update");
        return "redirect:/admin/pegawai";
    }

    @PostMapping("/pegawai/hapus/{id}")
    public String hapusKasir(@PathVariable Long id, HttpSession session, RedirectAttributes redirect) {
        if (bukanAdmin(session)) {
            return "redirect:/login";
        }

        User kasir = cariKasir(id);
        if (kasir == null) {
            redirect.addFlashAttribute("pesanError", "Akun kasir tidak ditemukan.");
            return "redirect:/admin/pegawai";
        }

        try {
            String namaFoto = kasir.getFoto();
            userService.hapusUser(id);
            hapusFileFoto(namaFoto);
            redirect.addFlashAttribute("pesanSukses", "Akun kasir berhasil dihapus.");
            redirect.addFlashAttribute("tipeNotif", "hapus");
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
        if (bukanAdmin(session)) {
            return "redirect:/login";
        }
        User admin = (User) session.getAttribute("user");
        if (admin == null) {
            return "redirect:/login";
        }

        if (foto == null || foto.isEmpty()) {
            redirect.addFlashAttribute("pesanError", "Foto wajib diisi dan maksimal 2 MB.");
            return targetRedirect(redirectUrl);
        }

        String namaFoto = simpanFileFoto(foto, redirect);
        if ("".equals(namaFoto)) {
            return targetRedirect(redirectUrl);
        }

        updateFotoUser(admin, namaFoto, session);
        redirect.addFlashAttribute("pesanSukses", "Foto profil berhasil diperbarui.");
        redirect.addFlashAttribute("tipeNotif", "update");
        return targetRedirect(redirectUrl);
    }

    @PostMapping("/profil/foto/hapus")
    public String hapusFotoAdmin(
            @RequestParam(value = "redirectUrl", required = false) String redirectUrl,
            HttpSession session,
            RedirectAttributes redirect) {
        if (bukanAdmin(session)) {
            return "redirect:/login";
        }
        User admin = (User) session.getAttribute("user");
        if (admin == null) {
            return "redirect:/login";
        }

        if (admin.getFoto() == null || admin.getFoto().isBlank()) {
            redirect.addFlashAttribute("pesanError", "Tidak ada foto profil untuk dihapus.");
            return targetRedirect(redirectUrl);
        }

        updateFotoUser(admin, null, session);
        redirect.addFlashAttribute("pesanSukses", "Foto profil berhasil dihapus.");
        redirect.addFlashAttribute("tipeNotif", "hapus");
        return targetRedirect(redirectUrl);
    }

    @PostMapping("/pegawai/{id}/foto")
    public String fotoKasir(@PathVariable Long id, @RequestParam("foto") MultipartFile foto, HttpSession session,
            RedirectAttributes redirect) {
        if (bukanAdmin(session)) {
            return "redirect:/login";
        }
        User kasir = cariKasir(id);
        if (kasir == null) {
            redirect.addFlashAttribute("pesanError", "Akun kasir tidak ditemukan.");
            return "redirect:/admin/pegawai";
        }

        if (foto == null || foto.isEmpty()) {
            redirect.addFlashAttribute("pesanError", "Foto wajib diisi dan maksimal 2 MB.");
            return "redirect:/admin/pegawai";
        }

        String namaFoto = simpanFileFoto(foto, redirect);
        if ("".equals(namaFoto)) {
            return "redirect:/admin/pegawai";
        }

        updateFotoUser(kasir, namaFoto, session);
        redirect.addFlashAttribute("pesanSukses", "Foto profil berhasil diperbarui.");
        redirect.addFlashAttribute("tipeNotif", "update");
        return "redirect:/admin/pegawai";
    }

    @PostMapping("/pegawai/{id}/foto/hapus")
    public String hapusFotoKasir(@PathVariable Long id, HttpSession session, RedirectAttributes redirect) {
        if (bukanAdmin(session)) {
            return "redirect:/login";
        }
        User kasir = cariKasir(id);
        if (kasir == null) {
            redirect.addFlashAttribute("pesanError", "Akun kasir tidak ditemukan.");
            return "redirect:/admin/pegawai";
        }

        if (kasir.getFoto() == null || kasir.getFoto().isBlank()) {
            redirect.addFlashAttribute("pesanError", "Tidak ada foto profil untuk dihapus.");
            return "redirect:/admin/pegawai";
        }

        updateFotoUser(kasir, null, session);
        redirect.addFlashAttribute("pesanSukses", "Foto profil berhasil dihapus.");
        redirect.addFlashAttribute("tipeNotif", "hapus");
        return "redirect:/admin/pegawai";
    }

    private boolean bukanAdmin(HttpSession session) {
        return !"admin".equals(session.getAttribute("role"));
    }

    private User cariKasir(Long id) {
        if (id == null) {
            return null;
        }
        User user = userService.cariById(id).orElse(null);
        if (user != null && "kasir".equalsIgnoreCase(user.getRole())) {
            return user;
        }
        return null;
    }

    private boolean usernameSudahAda(String username, Long excludeId) {
        User user = userService.cariByUsername(username).orElse(null);
        if (user == null) {
            return false;
        }
        if (excludeId != null && user.getId().equals(excludeId)) {
            return false;
        }
        return true;
    }

    private String targetRedirect(String redirectUrl) {
        if (redirectUrl != null && !redirectUrl.isBlank()) {
            return "redirect:" + redirectUrl;
        }
        return "redirect:/admin/dashboard";
    }

    private String simpanFileFoto(MultipartFile foto, RedirectAttributes redirect) {
        if (foto == null || foto.isEmpty()) {
            return null;
        }
        if (foto.getSize() > 2 * 1024 * 1024) {
            redirect.addFlashAttribute("pesanError", "Ukuran foto maksimal 2 MB.");
            return "";
        }
        String tipe = foto.getContentType();
        String ekstensi = "image/jpeg".equals(tipe) ? ".jpg" : "image/png".equals(tipe) ? ".png" : null;
        if (ekstensi == null) {
            redirect.addFlashAttribute("pesanError", "Foto harus berformat JPG atau PNG.");
            return "";
        }
        try {
            if (ImageIO.read(foto.getInputStream()) == null) {
                redirect.addFlashAttribute("pesanError", "File bukan gambar yang valid.");
                return "";
            }
            Files.createDirectories(folder);
            String namaFoto = UUID.randomUUID() + ekstensi;
            Files.copy(foto.getInputStream(), folder.resolve(namaFoto).normalize());
            return namaFoto;
        } catch (IOException e) {
            redirect.addFlashAttribute("pesanError", "Gagal menyimpan foto.");
            return "";
        }
    }

    private void hapusFileFoto(String namaFoto) {
        if (namaFoto != null && !namaFoto.isBlank()) {
            try {
                Files.deleteIfExists(folder.resolve(namaFoto).normalize());
            } catch (IOException ignored) {
            }
        }
    }

    private void updateFotoUser(User user, String namaFotoBaru, HttpSession session) {
        hapusFileFoto(user.getFoto());
        userService.updateFoto(user.getId(), namaFotoBaru);
        user.setFoto(namaFotoBaru);
        User userSession = (User) session.getAttribute("user");
        if (userSession != null && user.getId().equals(userSession.getId())) {
            session.setAttribute("user", user);
        }
    }
}
