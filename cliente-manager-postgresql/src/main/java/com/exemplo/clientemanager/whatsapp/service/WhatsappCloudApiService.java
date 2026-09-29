package com.exemplo.clientemanager.whatsapp.service;

import com.exemplo.clientemanager.config.WhatsappProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Envia mensagens usando exclusivamente a WhatsApp Business Cloud API
 * oficial da Meta (https://developers.facebook.com/docs/whatsapp/cloud-api).
 * So e registrada como bean quando app.whatsapp.habilitado=true.
 *
 * Nao usa automacao de navegador, QR code ou bibliotecas nao oficiais.
 * Esta classe nao pode ser validada de ponta a ponta sem uma conta comercial
 * da Meta aprovada; a estrutura da chamada segue a documentacao oficial e
 * deve ser conferida/testada pelo cliente com sua propria conta antes do uso
 * em producao (ver GUIA_WHATSAPP.md).
 */
@Service
@ConditionalOnProperty(name = "app.whatsapp.habilitado", havingValue = "true")
public class WhatsappCloudApiService implements WhatsappService {

    private static final Logger log = LoggerFactory.getLogger(WhatsappCloudApiService.class);

    private final WhatsappProperties properties;
    private final RestTemplate restTemplate = new RestTemplate();

    public WhatsappCloudApiService(WhatsappProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean isDisponivel() {
        return properties.isConfiguracaoCompleta();
    }

    @Override
    public ResultadoEnvio enviarMensagemLivre(String telefoneE164, String texto) {
        if (!isDisponivel()) {
            return ResultadoEnvio.falha("Integracao com o WhatsApp incompleta: verifique as variaveis de ambiente.");
        }
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("messaging_product", "whatsapp");
        corpo.put("to", telefoneE164);
        corpo.put("type", "text");
        corpo.put("text", Map.of("body", texto));
        return enviar(corpo);
    }

    @Override
    public ResultadoEnvio enviarModeloAprovado(String telefoneE164, String nomeModelo, String... parametros) {
        if (!isDisponivel()) {
            return ResultadoEnvio.falha("Integracao com o WhatsApp incompleta: verifique as variaveis de ambiente.");
        }
        Map<String, Object> template = new LinkedHashMap<>();
        template.put("name", nomeModelo);
        template.put("language", Map.of("code", "pt_BR"));
        if (parametros != null && parametros.length > 0) {
            List<Map<String, String>> parametrosCorpo = List.of(parametros).stream()
                    .map(p -> Map.of("type", "text", "text", p))
                    .toList();
            template.put("components", List.of(Map.of("type", "body", "parameters", parametrosCorpo)));
        }

        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("messaging_product", "whatsapp");
        corpo.put("to", telefoneE164);
        corpo.put("type", "template");
        corpo.put("template", template);
        return enviar(corpo);
    }

    private ResultadoEnvio enviar(Map<String, Object> corpo) {
        String url = "https://graph.facebook.com/" + properties.getApiVersion() + "/"
                + properties.getPhoneNumberId() + "/messages";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(properties.getAccessToken());

        try {
            restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(corpo, headers), String.class);
            return ResultadoEnvio.ok();
        } catch (RestClientException e) {
            // Nunca registrar o token no log, somente a falha.
            log.warn("Falha ao enviar mensagem via WhatsApp Cloud API: {}", e.getMessage());
            return ResultadoEnvio.falha("Nao foi possivel enviar a mensagem. Verifique a configuracao da conta da Meta.");
        }
    }
}
