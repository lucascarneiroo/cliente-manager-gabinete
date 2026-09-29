package com.exemplo.clientemanager.controller;

import com.exemplo.clientemanager.model.Compromisso;
import com.exemplo.clientemanager.model.PrioridadeCompromisso;
import com.exemplo.clientemanager.model.SituacaoCompromisso;
import com.exemplo.clientemanager.model.TipoCompromisso;
import com.exemplo.clientemanager.service.ClienteService;
import com.exemplo.clientemanager.service.CompromissoService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Controller
@RequestMapping("/agenda")
public class CompromissoController {

    private final CompromissoService compromissoService;
    private final ClienteService clienteService;

    public CompromissoController(CompromissoService compromissoService, ClienteService clienteService) {
        this.compromissoService = compromissoService;
        this.clienteService = clienteService;
    }

    @GetMapping
    public String agenda(@RequestParam(required = false) String visao,
                          @RequestParam(required = false) String data,
                          @RequestParam(required = false) String q,
                          Model model) {
        String visaoAtual = (visao == null || visao.isBlank()) ? "semana" : visao;
        LocalDate referencia = parseDataOuHoje(data);

        if (q != null && !q.isBlank()) {
            model.addAttribute("resultadosBusca", compromissoService.buscar(q));
        }

        LocalDate inicioPeriodo;
        LocalDate fimPeriodo;

        switch (visaoAtual) {
            case "dia" -> {
                inicioPeriodo = referencia;
                fimPeriodo = referencia;
            }
            case "mes" -> {
                inicioPeriodo = referencia.withDayOfMonth(1);
                fimPeriodo = referencia.withDayOfMonth(referencia.lengthOfMonth());
            }
            default -> {
                visaoAtual = "semana";
                inicioPeriodo = referencia.with(DayOfWeek.MONDAY);
                fimPeriodo = inicioPeriodo.plusDays(6);
            }
        }

        List<Compromisso> compromissos = compromissoService.listarPorPeriodo(inicioPeriodo, fimPeriodo);

        Map<LocalDate, List<Compromisso>> compromissosPorDia = new LinkedHashMap<>();
        for (Compromisso c : compromissos) {
            compromissosPorDia.computeIfAbsent(c.getData(), d -> new java.util.ArrayList<>()).add(c);
        }

        model.addAttribute("compromissos", compromissos);
        model.addAttribute("compromissosPorDia", compromissosPorDia);
        model.addAttribute("visao", visaoAtual);
        model.addAttribute("dataReferencia", referencia);
        model.addAttribute("inicioPeriodo", inicioPeriodo);
        model.addAttribute("fimPeriodo", fimPeriodo);
        model.addAttribute("anterior", periodoAnterior(visaoAtual, referencia));
        model.addAttribute("proximo", periodoProximo(visaoAtual, referencia));
        model.addAttribute("hoje", LocalDate.now());
        model.addAttribute("nomeDoMes", capitalizar(referencia.getMonth().getDisplayName(TextStyle.FULL, new Locale("pt", "BR"))) + " de " + referencia.getYear());
        model.addAttribute("tipos", TipoCompromisso.values());
        model.addAttribute("situacoes", SituacaoCompromisso.values());
        model.addAttribute("prioridades", PrioridadeCompromisso.values());

        return "agenda/lista";
    }

    @GetMapping("/novo")
    public String novoForm(@RequestParam(required = false) String data, Model model) {
        Compromisso compromisso = new Compromisso();
        compromisso.setData(parseDataOuHoje(data));
        prepararFormulario(model, compromisso);
        return "agenda/form";
    }

    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable Long id, Model model) {
        prepararFormulario(model, compromissoService.buscarPorId(id));
        return "agenda/form";
    }

    @GetMapping("/{id}")
    public String detalhes(@PathVariable Long id, Model model) {
        model.addAttribute("compromisso", compromissoService.buscarPorId(id));
        return "agenda/detalhes";
    }

    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute("compromisso") Compromisso compromisso,
                          BindingResult resultado,
                          @RequestParam(value = "cidadaoId", required = false) Long cidadaoId,
                          @RequestParam(value = "confirmarConflito", required = false, defaultValue = "false") boolean confirmarConflito,
                          Authentication authentication,
                          Model model,
                          RedirectAttributes redirectAttributes) {

        if (cidadaoId != null) {
            compromisso.setCidadao(clienteService.buscarPorId(cidadaoId));
        } else {
            compromisso.setCidadao(null);
        }

        if (resultado.hasErrors()) {
            prepararFormulario(model, compromisso);
            return "agenda/form";
        }

        List<Compromisso> conflitos = compromissoService.detectarConflitos(compromisso);
        if (!conflitos.isEmpty() && !confirmarConflito) {
            model.addAttribute("conflitos", conflitos);
            prepararFormulario(model, compromisso);
            return "agenda/form";
        }

        compromissoService.salvar(compromisso, authentication.getName());
        redirectAttributes.addFlashAttribute("sucesso", "Compromisso salvo com sucesso.");
        return "redirect:/agenda";
    }

    @PostMapping("/{id}/situacao")
    public String mudarSituacao(@PathVariable Long id,
                                 @RequestParam SituacaoCompromisso novaSituacao,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        compromissoService.mudarSituacao(id, novaSituacao, authentication.getName());
        redirectAttributes.addFlashAttribute("sucesso", "Situacao do compromisso atualizada.");
        return "redirect:/agenda/" + id;
    }

    @PostMapping("/{id}/cancelar")
    public String cancelar(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        compromissoService.cancelar(id, authentication.getName());
        redirectAttributes.addFlashAttribute("sucesso", "Compromisso cancelado.");
        return "redirect:/agenda/" + id;
    }

    @PostMapping("/{id}/excluir")
    @PreAuthorize("hasRole('ADMIN')")
    public String excluir(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        compromissoService.excluir(id);
        redirectAttributes.addFlashAttribute("sucesso", "Compromisso excluido definitivamente.");
        return "redirect:/agenda";
    }

    private void prepararFormulario(Model model, Compromisso compromisso) {
        model.addAttribute("compromisso", compromisso);
        model.addAttribute("cidadaos", clienteService.listarTodos());
        model.addAttribute("tipos", TipoCompromisso.values());
        model.addAttribute("situacoes", SituacaoCompromisso.values());
        model.addAttribute("prioridades", PrioridadeCompromisso.values());
    }

    private LocalDate parseDataOuHoje(String data) {
        if (data == null || data.isBlank()) {
            return LocalDate.now();
        }
        try {
            return LocalDate.parse(data);
        } catch (Exception e) {
            return LocalDate.now();
        }
    }

    private LocalDate periodoAnterior(String visao, LocalDate referencia) {
        return switch (visao) {
            case "dia" -> referencia.minusDays(1);
            case "mes" -> referencia.minusMonths(1);
            default -> referencia.minusWeeks(1);
        };
    }

    private LocalDate periodoProximo(String visao, LocalDate referencia) {
        return switch (visao) {
            case "dia" -> referencia.plusDays(1);
            case "mes" -> referencia.plusMonths(1);
            default -> referencia.plusWeeks(1);
        };
    }

    private String capitalizar(String texto) {
        if (texto == null || texto.isBlank()) return texto;
        return texto.substring(0, 1).toUpperCase() + texto.substring(1);
    }
}
