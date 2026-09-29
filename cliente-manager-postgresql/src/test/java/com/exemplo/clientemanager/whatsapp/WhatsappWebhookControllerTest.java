package com.exemplo.clientemanager.whatsapp;

import com.exemplo.clientemanager.config.WhatsappProperties;
import com.exemplo.clientemanager.whatsapp.controller.WhatsappWebhookController;
import com.exemplo.clientemanager.whatsapp.service.ConversaService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class WhatsappWebhookControllerTest {

    private WhatsappProperties propriedadesHabilitadas() {
        WhatsappProperties props = new WhatsappProperties();
        props.setHabilitado(true);
        props.setWebhookVerifyToken("token-secreto-de-teste");
        props.setAppSecret("segredo-do-app-de-teste");
        props.setAccessToken("token-de-acesso-ficticio");
        props.setPhoneNumberId("123456");
        return props;
    }

    private String assinar(String corpo, String segredo) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(segredo.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] hash = mac.doFinal(corpo.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder("sha256=");
        for (byte b : hash) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    @Test
    void verificacaoRetornaChallengeQuandoTokenCorreto() {
        WhatsappWebhookController controller = new WhatsappWebhookController(propriedadesHabilitadas(), mock(ConversaService.class));
        ResponseEntity<String> resposta = controller.verificar("subscribe", "token-secreto-de-teste", "abc123");
        assertEquals(200, resposta.getStatusCode().value());
        assertEquals("abc123", resposta.getBody());
    }

    @Test
    void verificacaoRejeitaTokenIncorreto() {
        WhatsappWebhookController controller = new WhatsappWebhookController(propriedadesHabilitadas(), mock(ConversaService.class));
        ResponseEntity<String> resposta = controller.verificar("subscribe", "token-errado", "abc123");
        assertEquals(403, resposta.getStatusCode().value());
    }

    @Test
    void recebimentoRejeitaAssinaturaInvalida() {
        WhatsappWebhookController controller = new WhatsappWebhookController(propriedadesHabilitadas(), mock(ConversaService.class));
        ResponseEntity<Void> resposta = controller.receber("{\"entry\":[]}", "sha256=assinatura-invalida");
        assertEquals(401, resposta.getStatusCode().value());
    }

    @Test
    void recebimentoAceitaAssinaturaValida() throws Exception {
        WhatsappProperties props = propriedadesHabilitadas();
        String corpo = "{\"entry\":[]}";
        String assinatura = assinar(corpo, props.getAppSecret());

        WhatsappWebhookController controller = new WhatsappWebhookController(props, mock(ConversaService.class));
        ResponseEntity<Void> resposta = controller.receber(corpo, assinatura);
        assertEquals(200, resposta.getStatusCode().value());
    }

    @Test
    void recebimentoRetorna404QuandoIntegracaoDesabilitada() {
        WhatsappProperties props = propriedadesHabilitadas();
        props.setHabilitado(false);
        WhatsappWebhookController controller = new WhatsappWebhookController(props, mock(ConversaService.class));
        ResponseEntity<Void> resposta = controller.receber("{}", "sha256=qualquer");
        assertEquals(404, resposta.getStatusCode().value());
    }
}
