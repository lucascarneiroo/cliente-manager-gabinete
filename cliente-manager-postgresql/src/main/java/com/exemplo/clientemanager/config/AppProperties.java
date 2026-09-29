package com.exemplo.clientemanager.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuracao centralizada de identidade visual e textos do sistema.
 * Nenhum nome de gabinete, vereador, cor ou logotipo deve ser escrito
 * diretamente nos templates: tudo vem daqui, e cada cliente personaliza
 * apenas estas propriedades (ver PERSONALIZACAO.md).
 */
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    /** Nome interno do produto (usado em titulos genericos). */
    private String nomeSistema = "Gestao de Gabinete";

    /** Nome exibido do gabinete/cliente. */
    private String nomeGabinete = "Gabinete Parlamentar";

    /** Nome do vereador/mandato, opcional. Deixe em branco se nao aplicavel. */
    private String nomeVereador = "";

    private String subtitulo = "Gestao de atendimentos";

    private String descricao = "Sistema de organizacao de cidadaos, atendimentos e compromissos do gabinete";

    /** Caminho do logotipo. Se vazio, a interface usa um monograma generico. */
    private String logo = "/images/logo-placeholder.svg";

    private String corPrimaria = "#173B67";

    private String corSecundaria = "#2563A6";

    private String rodape = "Sistema de Gestao de Gabinete";

    /** Quando true, exibe a faixa "Ambiente de demonstracao" no topo. */
    private boolean ambienteDemonstracao = false;

    private final Admin admin = new Admin();

    private final Demo demo = new Demo();

    public String getNomeSistema() { return nomeSistema; }
    public void setNomeSistema(String nomeSistema) { this.nomeSistema = nomeSistema; }

    public String getNomeGabinete() { return nomeGabinete; }
    public void setNomeGabinete(String nomeGabinete) { this.nomeGabinete = nomeGabinete; }

    public String getNomeVereador() { return nomeVereador; }
    public void setNomeVereador(String nomeVereador) { this.nomeVereador = nomeVereador; }

    public String getSubtitulo() { return subtitulo; }
    public void setSubtitulo(String subtitulo) { this.subtitulo = subtitulo; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getLogo() { return logo; }
    public void setLogo(String logo) { this.logo = logo; }

    public String getCorPrimaria() { return corPrimaria; }
    public void setCorPrimaria(String corPrimaria) { this.corPrimaria = corPrimaria; }

    public String getCorSecundaria() { return corSecundaria; }
    public void setCorSecundaria(String corSecundaria) { this.corSecundaria = corSecundaria; }

    public String getRodape() { return rodape; }
    public void setRodape(String rodape) { this.rodape = rodape; }

    public boolean isAmbienteDemonstracao() { return ambienteDemonstracao; }
    public void setAmbienteDemonstracao(boolean ambienteDemonstracao) { this.ambienteDemonstracao = ambienteDemonstracao; }

    public boolean isLogoConfigurado() {
        return logo != null && !logo.isBlank();
    }

    /** Titulo exibido no topo: nome do gabinete, e nome do vereador quando informado. */
    public String getTituloExibicao() {
        if (nomeVereador != null && !nomeVereador.isBlank()) {
            return nomeGabinete + " - " + nomeVereador;
        }
        return nomeGabinete;
    }

    public Admin getAdmin() { return admin; }
    public Demo getDemo() { return demo; }

    /** Variaveis usadas para criar o primeiro administrador (usuarios vazio). */
    public static class Admin {
        private String nome = "";
        private String usuario = "";
        private String senha = "";

        public String getNome() { return nome; }
        public void setNome(String nome) { this.nome = nome; }
        public String getUsuario() { return usuario; }
        public void setUsuario(String usuario) { this.usuario = usuario; }
        public String getSenha() { return senha; }
        public void setSenha(String senha) { this.senha = senha; }
    }

    /** Variaveis do usuario de demonstracao (perfil "demo"). */
    public static class Demo {
        private String usuario = "";
        private String senha = "";
        private boolean carregarDadosFicticios = true;

        public String getUsuario() { return usuario; }
        public void setUsuario(String usuario) { this.usuario = usuario; }
        public String getSenha() { return senha; }
        public void setSenha(String senha) { this.senha = senha; }
        public boolean isCarregarDadosFicticios() { return carregarDadosFicticios; }
        public void setCarregarDadosFicticios(boolean carregarDadosFicticios) { this.carregarDadosFicticios = carregarDadosFicticios; }
    }
}
