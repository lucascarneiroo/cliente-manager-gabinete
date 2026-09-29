package com.exemplo.clientemanager.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.logout;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityAccessTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void redirecionaParaLoginQuandoNaoAutenticado() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void loginPageEstaAcessivelSemAutenticacao() throws Exception {
        mockMvc.perform(get("/login")).andExpect(status().isOk());
    }

    @Test
    void aceitaLoginComCredenciaisCorretas() throws Exception {
        mockMvc.perform(formLogin().user("admin.teste").password("SenhaDeTeste123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void rejeitaLoginComSenhaIncorreta() throws Exception {
        mockMvc.perform(formLogin().user("admin.teste").password("senhaErrada"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login?erro"));
    }

    @Test
    void logoutInvalidaASessao() throws Exception {
        mockMvc.perform(post("/logout").with(logout()).with(csrf()))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "atendente.teste", roles = "ATENDENTE")
    void atendenteNaoAcessaGerenciamentoDeUsuarios() throws Exception {
        mockMvc.perform(get("/usuarios")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin.teste", roles = "ADMIN")
    void administradorAcessaGerenciamentoDeUsuarios() throws Exception {
        mockMvc.perform(get("/usuarios")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "atendente.teste", roles = "ATENDENTE")
    void requisicaoPostSemTokenCsrfEhRejeitada() throws Exception {
        mockMvc.perform(post("/agenda/1/cancelar"))
                .andExpect(status().isForbidden());
    }

    @Test
    void apiRestExigeAutenticacao() throws Exception {
        mockMvc.perform(get("/api/clientes"))
                .andExpect(status().is3xxRedirection());
    }
}
