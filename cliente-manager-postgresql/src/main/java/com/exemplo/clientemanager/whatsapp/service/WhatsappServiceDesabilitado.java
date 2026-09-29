package com.exemplo.clientemanager.whatsapp.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Implementacao usada quando app.whatsapp.habilitado=false (padrao).
 * Nao tenta se conectar com a Meta e nunca apresenta erro de conexao:
 * apenas informa que a integracao nao esta configurada.
 */
@Service
@ConditionalOnProperty(name = "app.whatsapp.habilitado", havingValue = "false", matchIfMissing = true)
public class WhatsappServiceDesabilitado implements WhatsappService {

    @Override
    public boolean isDisponivel() {
        return false;
    }

    @Override
    public ResultadoEnvio enviarMensagemLivre(String telefoneE164, String texto) {
        return ResultadoEnvio.falha("A integracao com o WhatsApp nao esta configurada nesta instalacao.");
    }

    @Override
    public ResultadoEnvio enviarModeloAprovado(String telefoneE164, String nomeModelo, String... parametros) {
        return ResultadoEnvio.falha("A integracao com o WhatsApp nao esta configurada nesta instalacao.");
    }
}
