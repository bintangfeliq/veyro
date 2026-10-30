package com.example.veyro;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.example.veyro.model.User;

@SpringBootTest
class VeyroApplicationTests {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void contextLoads() {
    }

    @Test
    void testAdminDashboardBisaDiakses() throws Exception {
        MockHttpSession session = new MockHttpSession();
        User adminUser = new User();
        adminUser.setId(1L);
        adminUser.setUsername("admin.veyro");
        adminUser.setRole("admin");
        session.setAttribute("role", "admin");
        session.setAttribute("user", adminUser);

        mockMvc.perform(get("/admin/dashboard").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"));
    }

    @Test
    void testAdminTransaksiBisaDiakses() throws Exception {
        MockHttpSession session = new MockHttpSession();
        User adminUser = new User();
        adminUser.setId(1L);
        adminUser.setUsername("admin.veyro");
        adminUser.setRole("admin");
        session.setAttribute("role", "admin");
        session.setAttribute("user", adminUser);

        mockMvc.perform(get("/admin/transaksi").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/transaksi"));
    }

    @Test
    void testAdminLaporanBisaDiakses() throws Exception {
        MockHttpSession session = new MockHttpSession();
        User adminUser = new User();
        adminUser.setId(1L);
        adminUser.setUsername("admin.veyro");
        adminUser.setRole("admin");
        session.setAttribute("role", "admin");
        session.setAttribute("user", adminUser);

        mockMvc.perform(get("/admin/laporan").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/laporan"));

        mockMvc.perform(get("/admin/laporan")
                .session(session)
                .param("tipeFilter", "bulan")
                .param("bulan", "2026-10"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/laporan"));

        mockMvc.perform(get("/admin/laporan")
                .session(session)
                .param("tipeFilter", "custom")
                .param("tanggalMulai", "2026-10-01")
                .param("tanggalSelesai", "2026-10-29"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/laporan"));
    }
}


