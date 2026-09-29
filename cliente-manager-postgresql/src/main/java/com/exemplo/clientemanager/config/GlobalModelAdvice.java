package com.exemplo.clientemanager.config;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Injeta a configuracao centralizada (nome, logotipo, cores, etc.) em todas
 * as paginas, para que nenhum template precise repetir esses valores.
 */
@ControllerAdvice
public class GlobalModelAdvice {

    private final AppProperties appProperties;
    private final WhatsappProperties whatsappProperties;

    public GlobalModelAdvice(AppProperties appProperties, WhatsappProperties whatsappProperties) {
        this.appProperties = appProperties;
        this.whatsappProperties = whatsappProperties;
    }

    @ModelAttribute("appConfig")
    public AppProperties appConfig() {
        return appProperties;
    }

    @ModelAttribute("whatsappHabilitado")
    public boolean whatsappHabilitado() {
        return whatsappProperties.isHabilitado();
    }

    @ModelAttribute("mensagensDisponivel")
    public boolean mensagensDisponivel(Model model) {
        return whatsappProperties.isHabilitado() || appProperties.isAmbienteDemonstracao();
    }
}
