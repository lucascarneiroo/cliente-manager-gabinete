package com.exemplo.clientemanager.controller;

import com.exemplo.clientemanager.model.Cliente;
import com.exemplo.clientemanager.model.Servico;
import com.exemplo.clientemanager.service.ClienteService;
import com.exemplo.clientemanager.service.ServicoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/clientes")
public class ClienteViewController {

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private ServicoService servicoService;

    // Lista todos os clientes, com busca opcional (?q=termo)
    @GetMapping
    public String listar(@RequestParam(value = "q", required = false) String q, Model model) {
        model.addAttribute("clientes", clienteService.buscar(q));
        model.addAttribute("termoBusca", q);
        model.addAttribute("aniversariantes", clienteService.aniversariantesDeHoje());
        return "lista";
    }

    // Formulario de novo cliente
    @GetMapping("/novo")
    public String novoForm(Model model) {
        model.addAttribute("cliente", new Cliente());
        return "form";
    }

    // Formulario de edicao (preenchido com dados existentes)
    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable Long id, Model model) {
        model.addAttribute("cliente", clienteService.buscarPorId(id));
        return "form";
    }

    @GetMapping("/{id}")
    public String detalhes(@PathVariable Long id, Model model) {
        model.addAttribute("cliente", clienteService.buscarPorId(id));
        model.addAttribute("servicos", servicoService.listarPorCliente(id));
        model.addAttribute("novoServico", new Servico());
        return "detalhes";
    }

    @PostMapping("/{id}/servicos")
    public String adicionarServico(@PathVariable Long id,
                                   @Valid @ModelAttribute("novoServico") Servico servico,
                                   BindingResult resultado,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        if (resultado.hasErrors()) {
            model.addAttribute("cliente", clienteService.buscarPorId(id));
            model.addAttribute("servicos", servicoService.listarPorCliente(id));
            return "detalhes";
        }
        servicoService.salvar(id, servico);
        redirectAttributes.addFlashAttribute("sucesso", "Atendimento adicionado ao historico.");
        return "redirect:/clientes/" + id;
    }

    @PostMapping("/{clienteId}/servicos/{servicoId}/excluir")
    @PreAuthorize("hasRole('ADMIN')")
    public String excluirServico(@PathVariable Long clienteId,
                                 @PathVariable Long servicoId,
                                 RedirectAttributes redirectAttributes) {
        servicoService.excluir(clienteId, servicoId);
        redirectAttributes.addFlashAttribute("sucesso", "Atendimento removido.");
        return "redirect:/clientes/" + clienteId;
    }

    // Salva (criacao ou edicao)
    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute("cliente") Cliente cliente,
                          BindingResult resultado,
                          RedirectAttributes redirectAttributes,
                          Model model) {

        if (resultado.hasErrors()) {
            return "form";
        }

        try {
            clienteService.salvar(cliente);
            redirectAttributes.addFlashAttribute("sucesso", "Cidadao salvo com sucesso!");
            return "redirect:/clientes";
        } catch (IllegalArgumentException e) {
            model.addAttribute("erro", e.getMessage());
            return "form";
        }
    }

    // Exclui um cidadao (somente administradores)
    @PostMapping("/{id}/excluir")
    @PreAuthorize("hasRole('ADMIN')")
    public String excluir(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        clienteService.excluir(id);
        redirectAttributes.addFlashAttribute("sucesso", "Cidadao removido.");
        return "redirect:/clientes";
    }
}
