package com.exemplo.clientemanager.whatsapp.service;

/**
 * Abstracao do envio de mensagens de WhatsApp. Existem duas implementacoes:
 *  - {@link WhatsappServiceDesabilitado}: usada quando app.whatsapp.habilitado=false
 *    (padrao). Nunca chama a API da Meta.
 *  - {@link WhatsappCloudApiService}: usada quando a integracao esta habilitada
 *    e configurada, chama a Cloud API oficial da Meta.
 *
 * O restante do sistema depende apenas desta interface, entao funciona
 * normalmente nos dois casos.
 */
public interface WhatsappService {

    boolean isDisponivel();

    /**
     * Envia uma mensagem de texto livre. So deve ser chamada quando a janela
     * de atendimento de 24h estiver aberta; caso contrario a implementacao
     * deve recusar e sinalizar que e necessario usar um modelo aprovado.
     */
    ResultadoEnvio enviarMensagemLivre(String telefoneE164, String texto);

    /** Envia um modelo (template) previamente aprovado pela Meta. */
    ResultadoEnvio enviarModeloAprovado(String telefoneE164, String nomeModelo, String... parametros);

    record ResultadoEnvio(boolean sucesso, String mensagem) {
        public static ResultadoEnvio ok() {
            return new ResultadoEnvio(true, "Mensagem enviada");
        }
        public static ResultadoEnvio falha(String motivo) {
            return new ResultadoEnvio(false, motivo);
        }
    }
}
