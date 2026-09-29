package com.exemplo.clientemanager.controller;

import com.exemplo.clientemanager.model.Compromisso;
import com.exemplo.clientemanager.service.ClienteService;
import com.exemplo.clientemanager.service.CompromissoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.List;

/** Painel inicial: visao geral do dia para quem esta autenticado. */
@Controller
public class HomeController {

    private final CompromissoService compromissoService;
    private final ClienteService clienteService;

    public HomeController(CompromissoService compromissoService, ClienteService clienteService) {
        this.compromissoService = compromissoService;
        this.clienteService = clienteService;
    }

    @GetMapping("/")
    public String inicio(Model model) {
        LocalDate hoje = LocalDate.now();
        List<Compromisso> compromissosHoje = compromissoService.listarDoDia(hoje);
        List<Compromisso> proximos = compromissoService.proximosCompromissos(hoje);

        model.addAttribute("compromissosHoje", compromissosHoje);
        model.addAttribute("proximoCompromisso", proximos.isEmpty() ? null : proximos.get(0));
        model.addAttribute("pendentesConfirmacao", compromissoService.pendentesDeConfirmacao());
        model.addAttribute("aniversariantes", clienteService.aniversariantesDeHoje());
        model.addAttribute("totalCidadaos", clienteService.listarTodos().size());
        return "inicio";
    }
}
