package com.exemplo.clientemanager.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuracao da integracao opcional com a WhatsApp Business Platform
 * (Cloud API oficial da Meta). Cada gabinete/cliente usa sua propria conta,
 * seu proprio numero e suas proprias credenciais - nada e compartilhado
 * entre instalacoes. Ver GUIA_WHATSAPP.md.
 *
 * Quando "habilitado" for false (padrao), nenhuma chamada e feita a API da
 * Meta e o restante do sistema funciona normalmente.
 */
@ConfigurationProperties(prefix = "app.whatsapp")
public class WhatsappProperties {

    private boolean habilitado = false;

    /** Token de acesso da conta do gabinete. Nunca deve ser exibido na interface ou logado. */
    private String accessToken = "";

    private String phoneNumberId = "";

    private String businessAccountId = "";

    /** Token usado para validar a chamada de verificacao do webhook (hub.verify_token). */
    private String webhookVerifyToken = "";

    /** Segredo do app, usado para validar a assinatura HMAC (X-Hub-Signature-256) do webhook. */
    private String appSecret = "";

    private String apiVersion = "v20.0";

    /** Politica de retencao de mensagens, em dias. */
    private int retencaoDiasMensagens = 365;

    /** Somente no perfil "demo": nunca chama a API real, so mostra uma caixa de entrada simulada. */
    private boolean simulacaoDemo = false;

    public boolean isHabilitado() { return habilitado; }
    public void setHabilitado(boolean habilitado) { this.habilitado = habilitado; }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getPhoneNumberId() { return phoneNumberId; }
    public void setPhoneNumberId(String phoneNumberId) { this.phoneNumberId = phoneNumberId; }

    public String getBusinessAccountId() { return businessAccountId; }
    public void setBusinessAccountId(String businessAccountId) { this.businessAccountId = businessAccountId; }

    public String getWebhookVerifyToken() { return webhookVerifyToken; }
    public void setWebhookVerifyToken(String webhookVerifyToken) { this.webhookVerifyToken = webhookVerifyToken; }

    public String getAppSecret() { return appSecret; }
    public void setAppSecret(String appSecret) { this.appSecret = appSecret; }

    public String getApiVersion() { return apiVersion; }
    public void setApiVersion(String apiVersion) { this.apiVersion = apiVersion; }

    public int getRetencaoDiasMensagens() { return retencaoDiasMensagens; }
    public void setRetencaoDiasMensagens(int retencaoDiasMensagens) { this.retencaoDiasMensagens = retencaoDiasMensagens; }

    public boolean isSimulacaoDemo() { return simulacaoDemo; }
    public void setSimulacaoDemo(boolean simulacaoDemo) { this.simulacaoDemo = simulacaoDemo; }

    /** Configuracao minima presente (nao valida junto da Meta, apenas confere se os campos essenciais foram preenchidos). */
    public boolean isConfiguracaoCompleta() {
        return habilitado
                && accessToken != null && !accessToken.isBlank()
                && phoneNumberId != null && !phoneNumberId.isBlank()
                && webhookVerifyToken != null && !webhookVerifyToken.isBlank()
                && appSecret != null && !appSecret.isBlank();
    }
}
