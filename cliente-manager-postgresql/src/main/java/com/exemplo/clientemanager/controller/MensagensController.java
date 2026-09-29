package com.exemplo.clientemanager.controller;

import com.exemplo.clientemanager.config.AppProperties;
import com.exemplo.clientemanager.config.WhatsappProperties;
import com.exemplo.clientemanager.whatsapp.model.SituacaoConversa;
import com.exemplo.clientemanager.whatsapp.service.ConversaService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/mensagens")
public class MensagensController {

    private final ConversaService conversaService;
    private final WhatsappProperties whatsappProperties;
    private final AppProperties appProperties;

    public MensagensController(ConversaService conversaService, WhatsappProperties whatsappProperties, AppProperties appProperties) {
        this.conversaService = conversaService;
        this.whatsappProperties = whatsappProperties;
        this.appProperties = appProperties;
    }

    private boolean moduloDisponivel() {
        return whatsappProperties.isHabilitado() || appProperties.isAmbienteDemonstracao();
    }

    @GetMapping
    public String listar(@RequestParam(required = false) String q, Model model) {
        if (!moduloDisponivel()) {
            return "mensagens/nao-configurado";
        }
        model.addAttribute("conversas", conversaService.buscar(q));
        model.addAttribute("termoBusca", q);
        model.addAttribute("modoSimulacao", !whatsappProperties.isHabilitado());
        return "mensagens/lista";
    }

    @GetMapping("/{id}")
    public String conversa(@PathVariable Long id, Model model) {
        if (!moduloDisponivel()) {
            return "mensagens/nao-configurado";
        }
        model.addAttribute("conversa", conversaService.buscarPorId(id));
        model.addAttribute("historico", conversaService.historico(id));
        model.addAttribute("modoSimulacao", !whatsappProperties.isHabilitado());
        return "mensagens/conversa";
    }

    @PostMapping("/{id}/responder")
    public String responder(@PathVariable Long id,
                             @RequestParam String texto,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        boolean simulada = !whatsappProperties.isHabilitado();
        var resultado = conversaService.responderLivre(id, texto, authentication.getName(), simulada);
        if (resultado.sucesso()) {
            redirectAttributes.addFlashAttribute("sucesso", simulada
                    ? "Resposta simulada registrada. Nenhuma mensagem real foi enviada."
                    : "Resposta enviada.");
        } else {
            redirectAttributes.addFlashAttribute("erro", resultado.mensagem());
        }
        return "redirect:/mensagens/" + id;
    }

    @PostMapping("/{id}/atribuir")
    public String atribuir(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        conversaService.atribuir(id, authentication.getName());
        redirectAttributes.addFlashAttribute("sucesso", "Conversa atribuida a voce.");
        return "redirect:/mensagens/" + id;
    }

    @PostMapping("/{id}/situacao")
    public String mudarSituacao(@PathVariable Long id, @RequestParam SituacaoConversa novaSituacao,
                                 RedirectAttributes redirectAttributes) {
        conversaService.mudarSituacao(id, novaSituacao);
        redirectAttributes.addFlashAttribute("sucesso", "Situacao da conversa atualizada.");
        return "redirect:/mensagens/" + id;
    }

    @PostMapping("/{id}/cadastrar-cidadao")
    public String cadastrarCidadao(@PathVariable Long id,
                                    @RequestParam String nome,
                                    @RequestParam String cpf,
                                    @RequestParam String endereco,
                                    RedirectAttributes redirectAttributes) {
        try {
            conversaService.cadastrarCidadaoDaConversa(id, nome, cpf, endereco);
            redirectAttributes.addFlashAttribute("sucesso", "Cidadao cadastrado e associado a conversa.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/mensagens/" + id;
    }

    @PostMapping("/{id}/registrar-atendimento")
    public String registrarAtendimento(@PathVariable Long id,
                                        @RequestParam String descricao,
                                        RedirectAttributes redirectAttributes) {
        try {
            conversaService.registrarComoAtendimento(id, descricao);
            redirectAttributes.addFlashAttribute("sucesso", "Atendimento registrado a partir da conversa.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/mensagens/" + id;
    }
}
