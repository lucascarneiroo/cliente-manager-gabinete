package com.exemplo.clientemanager.whatsapp.controller;

import com.exemplo.clientemanager.config.WhatsappProperties;
import com.exemplo.clientemanager.whatsapp.service.ConversaService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Iterator;

/**
 * Endpoint tecnico do webhook oficial da WhatsApp Business Platform (Meta).
 * E o unico endpoint publico alem do login e dos recursos estaticos - por
 * exigencia tecnica da propria Meta, que precisa alcancar esta URL sem
 * autenticacao de usuario. Em compensacao:
 *  - a verificacao (GET) exige o token configurado em WHATSAPP_WEBHOOK_VERIFY_TOKEN;
 *  - o recebimento (POST) exige uma assinatura HMAC-SHA256 valida (X-Hub-Signature-256),
 *    calculada com o WHATSAPP_APP_SECRET da conta do gabinete;
 *  - nenhuma pagina, conversa ou dado pessoal e exposto por aqui: o endpoint
 *    so aceita e devolve o minimo exigido pelo protocolo da Meta.
 */
@RestController
@RequestMapping("/webhook/whatsapp")
public class WhatsappWebhookController {

    private static final Logger log = LoggerFactory.getLogger(WhatsappWebhookController.class);

    private final WhatsappProperties properties;
    private final ConversaService conversaService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public WhatsappWebhookController(WhatsappProperties properties, ConversaService conversaService) {
        this.properties = properties;
        this.conversaService = conversaService;
    }

    /** Verificacao inicial exigida pela Meta ao cadastrar o webhook. */
    @GetMapping
    public ResponseEntity<String> verificar(@RequestParam("hub.mode") String modo,
                                             @RequestParam("hub.verify_token") String token,
                                             @RequestParam("hub.challenge") String challenge) {
        boolean valido = properties.isHabilitado()
                && "subscribe".equals(modo)
                && !properties.getWebhookVerifyToken().isBlank()
                && constantTimeEquals(properties.getWebhookVerifyToken(), token);

        if (!valido) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(challenge);
    }

    /** Recebimento de mensagens e atualizacoes de status. */
    @PostMapping
    public ResponseEntity<Void> receber(@RequestBody String corpoBruto,
                                         @RequestHeader(value = "X-Hub-Signature-256", required = false) String assinatura) {
        if (!properties.isHabilitado()) {
            return ResponseEntity.status(404).build();
        }
        if (!assinaturaValida(corpoBruto, assinatura)) {
            log.warn("Webhook do WhatsApp recebeu uma requisicao com assinatura invalida.");
            return ResponseEntity.status(401).build();
        }

        try {
            processarNotificacao(corpoBruto);
        } catch (Exception e) {
            // Nao expor detalhes internos; apenas registrar que houve falha de processamento.
            log.warn("Falha ao processar notificacao do webhook do WhatsApp: {}", e.getMessage());
        }

        // A Meta exige HTTP 200 para nao reenviar a notificacao.
        return ResponseEntity.ok().build();
    }

    private void processarNotificacao(String corpoBruto) throws Exception {
        JsonNode raiz = objectMapper.readTree(corpoBruto);
        JsonNode entradas = raiz.path("entry");
        if (!entradas.isArray()) return;

        for (JsonNode entrada : entradas) {
            JsonNode mudancas = entrada.path("changes");
            if (!mudancas.isArray()) continue;

            for (JsonNode mudanca : mudancas) {
                JsonNode valor = mudanca.path("value");
                JsonNode mensagens = valor.path("messages");
                if (!mensagens.isArray()) continue;

                Iterator<JsonNode> it = mensagens.elements();
                while (it.hasNext()) {
                    JsonNode mensagem = it.next();
                    String telefone = mensagem.path("from").asText(null);
                    String texto = mensagem.path("text").path("body").asText("");
                    if (telefone != null && !telefone.isBlank()) {
                        conversaService.receberMensagem(telefone, telefone, texto, false);
                    }
                }
            }
        }
    }

    private boolean assinaturaValida(String corpo, String cabecalho) {
        if (cabecalho == null || !cabecalho.startsWith("sha256=")) {
            return false;
        }
        if (properties.getAppSecret() == null || properties.getAppSecret().isBlank()) {
            return false;
        }
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(properties.getAppSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(corpo.getBytes(StandardCharsets.UTF_8));
            String calculada = "sha256=" + bytesParaHex(hash);
            return constantTimeEquals(calculada, cabecalho);
        } catch (Exception e) {
            log.warn("Nao foi possivel validar a assinatura do webhook: {}", e.getMessage());
            return false;
        }
    }

    private String bytesParaHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) return false;
        return MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
    }
}
