package com.exemplo.clientemanager.controller;

import com.exemplo.clientemanager.model.PerfilUsuario;
import com.exemplo.clientemanager.model.Usuario;
import com.exemplo.clientemanager.service.UsuarioService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Area exclusiva de administradores. Todas as rotas ja exigem ROLE_ADMIN
 * via SecurityConfig ("/usuarios/**"); o @PreAuthorize aqui e uma segunda
 * camada de garantia, caso a configuracao de rotas mude no futuro.
 */
@Controller
@RequestMapping("/usuarios")
@PreAuthorize("hasRole('ADMIN')")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("usuarios", usuarioService.listarTodos());
        return "usuarios/lista";
    }

    @GetMapping("/novo")
    public String novoForm(Model model) {
        Usuario usuario = new Usuario();
        usuario.setPerfil(PerfilUsuario.ATENDENTE);
        model.addAttribute("usuario", usuario);
        model.addAttribute("perfis", PerfilUsuario.values());
        model.addAttribute("tamanhoMinimoSenha", UsuarioService.TAMANHO_MINIMO_SENHA);
        return "usuarios/form";
    }

    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable Long id, Model model) {
        model.addAttribute("usuario", usuarioService.buscarPorId(id));
        model.addAttribute("perfis", PerfilUsuario.values());
        model.addAttribute("tamanhoMinimoSenha", UsuarioService.TAMANHO_MINIMO_SENHA);
        return "usuarios/form";
    }

    @PostMapping("/novo")
    public String criar(@RequestParam String nomeCompleto,
                         @RequestParam String nomeUsuario,
                         @RequestParam String senha,
                         @RequestParam String confirmacaoSenha,
                         @RequestParam PerfilUsuario perfil,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        try {
            if (!senha.equals(confirmacaoSenha)) {
                throw new IllegalArgumentException("A senha e a confirmacao nao coincidem");
            }
            usuarioService.criar(nomeCompleto, nomeUsuario, senha, perfil);
            redirectAttributes.addFlashAttribute("sucesso", "Usuario criado com sucesso.");
            return "redirect:/usuarios";
        } catch (IllegalArgumentException e) {
            Usuario usuario = new Usuario();
            usuario.setNomeCompleto(nomeCompleto);
            usuario.setNomeUsuario(nomeUsuario);
            usuario.setPerfil(perfil);
            model.addAttribute("usuario", usuario);
            model.addAttribute("perfis", PerfilUsuario.values());
            model.addAttribute("tamanhoMinimoSenha", UsuarioService.TAMANHO_MINIMO_SENHA);
            model.addAttribute("erro", e.getMessage());
            return "usuarios/form";
        }
    }

    @PostMapping("/{id}/editar")
    public String atualizar(@PathVariable Long id,
                             @RequestParam String nomeCompleto,
                             @RequestParam PerfilUsuario perfil,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        try {
            usuarioService.atualizarDados(id, nomeCompleto, perfil);
            redirectAttributes.addFlashAttribute("sucesso", "Usuario atualizado.");
            return "redirect:/usuarios";
        } catch (IllegalArgumentException e) {
            model.addAttribute("usuario", usuarioService.buscarPorId(id));
            model.addAttribute("perfis", PerfilUsuario.values());
            model.addAttribute("tamanhoMinimoSenha", UsuarioService.TAMANHO_MINIMO_SENHA);
            model.addAttribute("erro", e.getMessage());
            return "usuarios/form";
        }
    }

    @PostMapping("/{id}/redefinir-senha")
    public String redefinirSenha(@PathVariable Long id,
                                  @RequestParam String novaSenha,
                                  @RequestParam String confirmacaoSenha,
                                  RedirectAttributes redirectAttributes) {
        try {
            usuarioService.redefinirSenha(id, novaSenha, confirmacaoSenha);
            redirectAttributes.addFlashAttribute("sucesso", "Senha redefinida com sucesso.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/usuarios/" + id + "/editar";
    }

    @PostMapping("/{id}/bloquear")
    public String bloquear(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.bloquear(id, authentication.getName());
            redirectAttributes.addFlashAttribute("sucesso", "Usuario bloqueado.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/usuarios";
    }

    @PostMapping("/{id}/reativar")
    public String reativar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        usuarioService.reativar(id);
        redirectAttributes.addFlashAttribute("sucesso", "Usuario reativado.");
        return "redirect:/usuarios";
    }
}
