package com.exemplo.clientemanager.config;

import com.exemplo.clientemanager.model.*;
import com.exemplo.clientemanager.repository.ClienteRepository;
import com.exemplo.clientemanager.service.ClienteService;
import com.exemplo.clientemanager.service.CompromissoService;
import com.exemplo.clientemanager.service.ServicoService;
import com.exemplo.clientemanager.service.UsuarioService;
import com.exemplo.clientemanager.whatsapp.model.*;
import com.exemplo.clientemanager.whatsapp.repository.ConversaRepository;
import com.exemplo.clientemanager.whatsapp.repository.MensagemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Cria cidadaos, atendimentos, compromissos, usuarios e conversas simuladas de
 * WhatsApp ficticios, exclusivamente no perfil "demo" e somente quando o
 * banco ainda esta vazio. Nunca roda em producao (o perfil "demo" so fica
 * ativo com SPRING_PROFILES_ACTIVE=demo).
 *
 * Todos os nomes, CPFs, telefones e enderecos sao inventados: os CPFs sao
 * gerados aleatoriamente (apenas com digitos verificadores validos) e nao
 * correspondem a pessoas reais. O volume de dados e pensado para que as telas
 * fiquem "cheias" em apresentacoes e capturas de tela.
 */
@Component
@Profile("demo")
@Order(2)
public class DemoDataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataLoader.class);

    /** Semente fixa: os mesmos dados ficticios sao gerados a cada instalacao de demonstracao. */
    private final Random random = new Random(20241117L);

    private final ClienteRepository clienteRepository;
    private final ClienteService clienteService;
    private final ServicoService servicoService;
    private final CompromissoService compromissoService;
    private final UsuarioService usuarioService;
    private final ConversaRepository conversaRepository;
    private final MensagemRepository mensagemRepository;
    private final AppProperties appProperties;

    public DemoDataLoader(ClienteRepository clienteRepository, ClienteService clienteService,
                           ServicoService servicoService, CompromissoService compromissoService,
                           UsuarioService usuarioService, ConversaRepository conversaRepository,
                           MensagemRepository mensagemRepository, AppProperties appProperties) {
        this.clienteRepository = clienteRepository;
        this.clienteService = clienteService;
        this.servicoService = servicoService;
        this.compromissoService = compromissoService;
        this.usuarioService = usuarioService;
        this.conversaRepository = conversaRepository;
        this.mensagemRepository = mensagemRepository;
        this.appProperties = appProperties;
    }

    @Override
    public void run(String... args) {
        if (!appProperties.getDemo().isCarregarDadosFicticios()) {
            return;
        }
        criarUsuarioDemoSeConfigurado();

        if (!clienteRepository.findAll().isEmpty()) {
            log.info("Perfil demo: ja existem cidadaos cadastrados, dados ficticios nao serao recriados.");
            return;
        }

        criarEquipeFicticia();
        List<Cliente> cidadaos = criarCidadaos();
        criarAtendimentos(cidadaos);
        criarAgenda(cidadaos);
        criarConversasWhatsapp(cidadaos);

        log.info("Perfil demo: dados ficticios carregados com sucesso ({} cidadaos).", cidadaos.size());
    }

    // ------------------------------------------------------------------ usuarios

    private void criarUsuarioDemoSeConfigurado() {
        String usuario = appProperties.getDemo().getUsuario();
        String senha = appProperties.getDemo().getSenha();
        if (usuario == null || usuario.isBlank() || senha == null || senha.isBlank()) {
            log.warn("Perfil demo ativo, mas APP_DEMO_USUARIO/APP_DEMO_SENHA nao foram definidos: "
                    + "nenhum usuario de demonstracao sera criado (use o administrador para acessar).");
            return;
        }
        try {
            // Perfil ATENDENTE: o usuario de demonstracao nunca tem acesso ao gerenciamento de usuarios.
            usuarioService.criar("Usuario de Demonstracao", usuario, senha, PerfilUsuario.ATENDENTE);
            log.info("Usuario de demonstracao criado (usuario: {}).", usuario);
        } catch (IllegalArgumentException e) {
            // Ja existe (reinicio da aplicacao) - segue normalmente.
        }
    }

    /** Membros ficticios da equipe, apenas para a tela de usuarios nao ficar vazia. */
    private void criarEquipeFicticia() {
        String[][] equipe = {
                {"Mariana Albuquerque", "mariana.albuquerque", "ADMIN"},
                {"Rafael Cavalcanti", "rafael.cavalcanti", "ATENDENTE"},
                {"Juliana Moraes", "juliana.moraes", "ATENDENTE"},
                {"Thiago Barreto", "thiago.barreto", "ATENDENTE"},
        };
        for (String[] membro : equipe) {
            try {
                // Senha aleatoria e descartada: essas contas existem so para exibicao.
                String senha = "Demo-" + Long.toHexString(random.nextLong()) + "!";
                usuarioService.criar(membro[0], membro[1], senha, PerfilUsuario.valueOf(membro[2]));
            } catch (IllegalArgumentException e) {
                // Ja existe - segue normalmente.
            }
        }
    }

    // ------------------------------------------------------------------ cidadaos

    private static final String[] NOMES = {
            "Ana Beatriz Lacerda", "Bruno Henrique Tavares", "Carla Regina Monteiro", "Daniel Augusto Ferraz",
            "Eduarda Siqueira Lins", "Fernando Gusmao Neto", "Gabriela Rocha Pimentel", "Heitor Vasconcelos",
            "Isabela Cordeiro Maia", "Joao Pedro Arruda", "Karina Medeiros Falcao", "Leonardo Bezerra Costa",
            "Maria Clara Wanderley", "Nicolas Amaral Queiroz", "Olivia Prado Dantas", "Paulo Sergio Brandao",
            "Rebeca Novaes Teixeira", "Samuel Coutinho Leal", "Tatiane Freire Andrade", "Vinicius Batista Sales",
            "Wesley Nogueira Pontes", "Yasmin Carvalho Duarte", "Antonio Marcos Galvao", "Benedita Souza Ramos",
            "Claudio Ribeiro Farias", "Denise Moura Holanda", "Elias Cardoso Viana", "Francisca Melo Barros",
            "Gilberto Paiva Correia", "Helena Torres Mendonca", "Igor Santana Rezende", "Josefa Alencar Pinto",
            "Luciana Fontes Aragao", "Marcelo Diniz Peixoto", "Natalia Guerra Leitao", "Otavio Bastos Moreira",
    };

    private static final String[] RUAS = {
            "Rua das Acacias", "Av. Beira Rio", "Rua Sao Sebastiao", "Rua Jose de Alencar", "Travessa do Mercado",
            "Rua Nova Esperanca", "Av. Central", "Rua dos Ipes", "Rua Padre Cicero", "Rua Sete de Setembro",
            "Av. das Palmeiras", "Rua Dom Pedro II", "Rua do Sol", "Travessa Santa Luzia", "Rua Projetada 12",
    };

    private static final String[] BAIRROS = {
            "Centro", "Vila Nova", "Jardim Primavera", "Boa Vista", "Alto do Cruzeiro", "Sao Jose",
            "Parque das Flores", "Santa Rita", "Bela Vista", "Conjunto Esperanca",
    };

    private static final String[] PERFIS = {
            "Lideranca comunitaria do bairro, participa das reunioes mensais.",
            "Comerciante local, interessado em melhorias na mobilidade urbana.",
            "Aposentado(a), acompanha demandas de saude publica.",
            "Professor(a) da rede municipal.",
            "Presidente da associacao de moradores.",
            "Mae de aluno da escola municipal do bairro.",
            "Agricultor(a) da zona rural.",
            "Primeiro contato via redes sociais.",
            null, null, null,
    };

    private List<Cliente> criarCidadaos() {
        List<Cliente> cidadaos = new ArrayList<>();
        LocalDate hoje = LocalDate.now();
        for (int i = 0; i < NOMES.length; i++) {
            Cliente cliente = new Cliente();
            cliente.setNome(NOMES[i]);
            cliente.setCpf(cpfAleatorio());
            cliente.setTelefone(telefoneAleatorio());
            cliente.setEndereco(RUAS[random.nextInt(RUAS.length)] + ", " + (10 + random.nextInt(990))
                    + " - " + BAIRROS[random.nextInt(BAIRROS.length)]);
            cliente.setDescricao(PERFIS[random.nextInt(PERFIS.length)]);

            LocalDate nascimento = LocalDate.of(1950 + random.nextInt(55), 1 + random.nextInt(12), 1 + random.nextInt(28));
            if (i == 0 || i == 7) {
                // Dois aniversariantes no dia, para o aviso da tela inicial aparecer.
                nascimento = hoje.minusYears(30 + i * 3L);
            }
            cliente.setDataNascimento(nascimento);
            cidadaos.add(clienteService.salvar(cliente));
        }
        return cidadaos;
    }

    // ------------------------------------------------------------------ atendimentos

    private static final String[][] ATENDIMENTOS = {
            {"Solicitacao de troca de lampadas na rua", "Encaminhado a Secretaria de Infraestrutura (protocolo 2026/1187)."},
            {"Pedido de operacao tapa-buraco", "Vistoria agendada pela equipe de obras."},
            {"Agendamento de consulta com especialista", "Encaminhado a Secretaria de Saude, aguardando vaga em cardiologia."},
            {"Vaga em creche municipal", "Orientado sobre a lista de espera e documentos necessarios."},
            {"Limpeza de terreno baldio", "Oficio enviado a Vigilancia Sanitaria."},
            {"Duvida sobre isencao de IPTU", "Orientado a procurar a Secretaria de Financas com laudo medico."},
            {"Poda de arvore com risco de queda", "Defesa Civil acionada."},
            {"Instalacao de faixa de pedestre em frente a escola", "Indicacao protocolada na Camara."},
            {"Acesso a programa habitacional", "Cadastro realizado no CadUnico, aguardando chamamento."},
            {"Coleta de lixo irregular no bairro", "Reclamacao registrada junto a empresa de limpeza urbana."},
            {"Medicamento em falta na UBS", "Contato feito com a coordenacao da farmacia basica."},
            {"Pedido de transporte escolar para zona rural", "Encaminhado a Secretaria de Educacao."},
            {"Vazamento de agua na via publica", "Chamado aberto na companhia de saneamento."},
            {"Apoio para evento comunitario", "Solicitada cessao de tendas e cadeiras."},
            {"Orientacao sobre BPC/LOAS", "Encaminhado ao CRAS de referencia."},
            {"Reforma da quadra poliesportiva", "Incluido na lista de emendas para o proximo exercicio."},
    };

    private void criarAtendimentos(List<Cliente> cidadaos) {
        LocalDate hoje = LocalDate.now();
        for (Cliente cidadao : cidadaos) {
            int quantidade = 1 + random.nextInt(4);
            for (int j = 0; j < quantidade; j++) {
                String[] modelo = ATENDIMENTOS[random.nextInt(ATENDIMENTOS.length)];
                Servico servico = new Servico();
                servico.setDescricao(modelo[0]);
                servico.setObservacoes(modelo[1]);
                int diasAtras = random.nextInt(120);
                servico.setDataServico(hoje.minusDays(diasAtras));
                // Atendimentos mais antigos tendem a ja estar resolvidos.
                servico.setRealizado(diasAtras > 20 ? random.nextInt(10) < 8 : random.nextInt(10) < 3);
                if (modelo[0].startsWith("Apoio para evento")) {
                    servico.setValor(new BigDecimal(150 + random.nextInt(600)));
                }
                servicoService.salvar(cidadao.getId(), servico);
            }
        }
    }

    // ------------------------------------------------------------------ agenda

    private void criarAgenda(List<Cliente> c) {
        LocalDate hoje = LocalDate.now();

        // Hoje
        compromisso("Reuniao com a associacao de moradores da Vila Nova", TipoCompromisso.REUNIAO,
                SituacaoCompromisso.CONFIRMADO, PrioridadeCompromisso.ALTA,
                hoje, h(8, 30), h(9, 30), "Gabinete", c.get(14),
                "Pauta: pavimentacao da Rua Projetada 12 e iluminacao da praca.");
        compromisso("Atendimento - Maria Clara Wanderley", TipoCompromisso.ATENDIMENTO_CIDADAO,
                SituacaoCompromisso.CONFIRMADO, PrioridadeCompromisso.NORMAL,
                hoje, h(10, 0), h(10, 30), "Gabinete", c.get(12),
                "Acompanhamento do pedido de vaga em creche.");
        compromisso("Almoco com secretario de Infraestrutura", TipoCompromisso.REUNIAO,
                SituacaoCompromisso.AGENDADO, PrioridadeCompromisso.NORMAL,
                hoje, h(12, 0), h(13, 30), "Restaurante Sabor da Terra", null,
                "Levar lista de demandas de iluminacao publica.");
        compromisso("Sessao ordinaria da Camara Municipal", TipoCompromisso.SESSAO_LEGISLATIVA,
                SituacaoCompromisso.CONFIRMADO, PrioridadeCompromisso.ALTA,
                hoje, h(15, 0), h(18, 0), "Plenario da Camara Municipal", null,
                "Votacao do projeto de lei sobre arborizacao urbana.");

        // Proximos dias
        compromisso("Visita a UBS do Jardim Primavera", TipoCompromisso.VISITA,
                SituacaoCompromisso.CONFIRMADO, PrioridadeCompromisso.ALTA,
                hoje.plusDays(1), h(8, 0), h(10, 0), "UBS Jardim Primavera", c.get(2),
                "Verificar falta de medicamentos relatada por moradores.");
        compromisso("Reuniao interna da equipe", TipoCompromisso.COMPROMISSO_INTERNO,
                SituacaoCompromisso.AGENDADO, PrioridadeCompromisso.NORMAL,
                hoje.plusDays(1), h(14, 0), h(15, 0), "Gabinete", null,
                "Balanco semanal dos atendimentos e planejamento.");
        compromisso("Audiencia publica sobre mobilidade urbana", TipoCompromisso.AUDIENCIA,
                SituacaoCompromisso.CONFIRMADO, PrioridadeCompromisso.URGENTE,
                hoje.plusDays(2), h(19, 0), h(21, 30), "Auditorio da Camara", null,
                "Participacao de moradores, comerciantes e Secretaria de Transito.");
        compromisso("Atendimento - Paulo Sergio Brandao", TipoCompromisso.ATENDIMENTO_CIDADAO,
                SituacaoCompromisso.AGENDADO, PrioridadeCompromisso.NORMAL,
                hoje.plusDays(2), h(9, 0), h(9, 30), "Gabinete", c.get(15), null);
        compromisso("Inauguracao da praca do Conjunto Esperanca", TipoCompromisso.EVENTO_PUBLICO,
                SituacaoCompromisso.CONFIRMADO, PrioridadeCompromisso.ALTA,
                hoje.plusDays(3), h(17, 0), h(19, 0), "Praca do Conjunto Esperanca", null, null);
        compromisso("Visita a escola municipal Sao Jose", TipoCompromisso.VISITA,
                SituacaoCompromisso.AGENDADO, PrioridadeCompromisso.NORMAL,
                hoje.plusDays(4), h(9, 0), h(11, 0), "E.M. Sao Jose", c.get(3),
                "Avaliar condicoes da quadra e do transporte escolar.");
        compromisso("Sessao ordinaria da Camara Municipal", TipoCompromisso.SESSAO_LEGISLATIVA,
                SituacaoCompromisso.AGENDADO, PrioridadeCompromisso.ALTA,
                hoje.plusDays(7), h(15, 0), h(18, 0), "Plenario da Camara Municipal", null, null);
        compromisso("Encontro regional de vereadores", TipoCompromisso.VIAGEM,
                SituacaoCompromisso.AGENDADO, PrioridadeCompromisso.NORMAL,
                hoje.plusDays(9), null, null, "Capital do estado", null,
                "Saida as 6h. Confirmar hospedagem.");
        compromisso("Reuniao com produtores rurais", TipoCompromisso.REUNIAO,
                SituacaoCompromisso.ADIADO, PrioridadeCompromisso.BAIXA,
                hoje.plusDays(11), h(16, 0), h(17, 30), "Sindicato Rural", c.get(6),
                "Adiado a pedido do sindicato.");
        compromisso("Mutirao de atendimento no bairro Santa Rita", TipoCompromisso.EVENTO_PUBLICO,
                SituacaoCompromisso.AGENDADO, PrioridadeCompromisso.ALTA,
                hoje.plusDays(13), h(8, 0), h(12, 0), "Centro Comunitario Santa Rita", null, null);

        // Passados
        compromisso("Sessao ordinaria da Camara Municipal", TipoCompromisso.SESSAO_LEGISLATIVA,
                SituacaoCompromisso.REALIZADO, PrioridadeCompromisso.ALTA,
                hoje.minusDays(7), h(15, 0), h(18, 0), "Plenario da Camara Municipal", null, null);
        compromisso("Visita ao Alto do Cruzeiro", TipoCompromisso.VISITA,
                SituacaoCompromisso.REALIZADO, PrioridadeCompromisso.NORMAL,
                hoje.minusDays(3), h(9, 0), h(11, 0), "Alto do Cruzeiro", c.get(9),
                "Registradas 6 demandas de iluminacao e drenagem.");
        compromisso("Reuniao com Secretaria de Educacao", TipoCompromisso.REUNIAO,
                SituacaoCompromisso.CANCELADO, PrioridadeCompromisso.NORMAL,
                hoje.minusDays(2), h(10, 0), h(11, 0), "Secretaria de Educacao", null, null);
        compromisso("Atendimento - Rebeca Novaes Teixeira", TipoCompromisso.ATENDIMENTO_CIDADAO,
                SituacaoCompromisso.REALIZADO, PrioridadeCompromisso.NORMAL,
                hoje.minusDays(1), h(14, 0), h(14, 30), "Gabinete", c.get(16), null);
    }

    private static LocalTime h(int hora, int minuto) {
        return LocalTime.of(hora, minuto);
    }

    private void compromisso(String titulo, TipoCompromisso tipo, SituacaoCompromisso situacao,
                              PrioridadeCompromisso prioridade, LocalDate data, LocalTime inicio, LocalTime fim,
                              String local, Cliente cidadao, String descricao) {
        Compromisso compromisso = new Compromisso();
        compromisso.setTitulo(titulo);
        compromisso.setDescricao(descricao);
        compromisso.setTipo(tipo);
        compromisso.setSituacao(situacao);
        compromisso.setPrioridade(prioridade);
        compromisso.setData(data);
        compromisso.setDiaInteiro(inicio == null);
        compromisso.setHoraInicio(inicio);
        compromisso.setHoraFim(fim);
        compromisso.setLocal(local);
        compromisso.setCidadao(cidadao);
        compromissoService.salvar(compromisso, "demo");
    }

    // ------------------------------------------------------------------ whatsapp (simulado)

    private void criarConversasWhatsapp(List<Cliente> c) {
        LocalDateTime agora = LocalDateTime.now();

        conversa(c.get(9), "Joao Pedro", SituacaoConversa.EM_ATENDIMENTO, "rafael.cavalcanti", agora.minusMinutes(95),
                "C:Bom dia! A rua aqui do Alto do Cruzeiro esta sem iluminacao faz uma semana",
                "C:Ja liguei na prefeitura mas ninguem resolveu",
                "G:Bom dia, Joao Pedro! Obrigado por avisar. Pode me informar o nome da rua e um ponto de referencia?",
                "C:Rua Padre Cicero, perto da mercearia do Seu Ze",
                "G:Registramos a solicitacao e encaminhamos para a Secretaria de Infraestrutura. Assim que tivermos retorno avisamos por aqui.",
                "C:Muito obrigado!! 🙏");

        conversa(null, "Lucia Helena", SituacaoConversa.ABERTA, null, agora.minusMinutes(12),
                "C:Ola, boa tarde. Qual o horario de atendimento do gabinete?",
                "C:Queria conversar sobre uma vaga na creche para meu filho");

        conversa(c.get(12), "Maria Clara", SituacaoConversa.EM_ATENDIMENTO, "juliana.moraes", agora.minusHours(3),
                "C:Oi, confirmando meu atendimento de hoje as 10h",
                "G:Oi, Maria Clara! Confirmado sim. Traga RG, comprovante de residencia e a certidao da crianca.",
                "C:Combinado, ate mais tarde");

        conversa(c.get(2), "Carla Monteiro", SituacaoConversa.FINALIZADA, "rafael.cavalcanti", agora.minusDays(1).minusHours(2),
                "C:A UBS do Jardim Primavera esta sem remedio de pressao de novo",
                "G:Ola, Carla. Vamos verificar com a coordenacao da farmacia basica ainda hoje.",
                "G:Atualizacao: o lote chegou e ja esta disponivel na UBS. O vereador fara uma visita a unidade amanha.",
                "C:Que otimo, obrigada pelo retorno rapido!");

        conversa(null, "Associacao Vila Nova", SituacaoConversa.ABERTA, null, agora.minusMinutes(40),
                "C:Boa tarde! Gostariamos de convidar o vereador para a confraternizacao de fim de ano do bairro",
                "C:Sera no dia 13 de dezembro, a partir das 18h, na quadra da associacao");

        conversa(c.get(15), "Paulo Brandao", SituacaoConversa.EM_ATENDIMENTO, "thiago.barreto", agora.minusHours(6),
                "C:Bom dia. Tem um buraco enorme na Av. Central em frente ao numero 450",
                "G:Bom dia, Paulo! Pode nos enviar uma foto do local?",
                "C:[imagem]",
                "G:Recebido. Ja protocolamos o pedido de operacao tapa-buraco e agendamos um atendimento com voce.");

        conversa(c.get(20), "Wesley Pontes", SituacaoConversa.FINALIZADA, "juliana.moraes", agora.minusDays(3),
                "C:Como faco para pedir poda de arvore?",
                "G:Ola, Wesley! Pode nos informar o endereco? Fazemos o encaminhamento para a Defesa Civil.",
                "C:Rua dos Ipes, 210 - Boa Vista",
                "G:Encaminhado! Protocolo 2026/1342.");
    }

    /**
     * Cria uma conversa simulada com historico. Linhas iniciadas com "C:" sao do
     * cidadao e "G:" do gabinete. Sempre marcada como simulada: nunca e enviada.
     */
    private void conversa(Cliente cidadao, String nomeContato, SituacaoConversa situacao, String atribuidoA,
                           LocalDateTime inicio, String... mensagens) {
        Conversa conversa = new Conversa();
        conversa.setTelefone(cidadao != null ? cidadao.getTelefone() : telefoneAleatorio());
        conversa.setNomeContato(nomeContato);
        conversa.setCidadao(cidadao);
        conversa.setSituacao(situacao);
        conversa.setAtribuidoA(atribuidoA);
        conversa.setSimulada(true);
        conversa = conversaRepository.save(conversa);

        LocalDateTime momento = inicio;
        for (String linha : mensagens) {
            boolean doCidadao = linha.startsWith("C:");
            Mensagem mensagem = new Mensagem();
            mensagem.setConversa(conversa);
            mensagem.setRemetente(doCidadao ? RemetenteMensagem.CIDADAO : RemetenteMensagem.GABINETE);
            mensagem.setConteudo(linha.substring(2));
            mensagem.setDataHora(momento);
            mensagem.setSimulada(true);
            mensagem.setStatus(doCidadao ? StatusMensagem.RECEBIDO : StatusMensagem.LIDO);
            if (!doCidadao) {
                mensagem.setUsuarioResposta(atribuidoA);
            }
            mensagemRepository.save(mensagem);
            momento = momento.plusMinutes(2 + random.nextInt(8));
        }
    }

    // ------------------------------------------------------------------ geradores

    private String telefoneAleatorio() {
        return String.format("(81) 9%04d-%04d", 8000 + random.nextInt(2000), random.nextInt(10000));
    }

    /** Gera um CPF aleatorio com digitos verificadores validos (nao pertence a ninguem conhecido). */
    private String cpfAleatorio() {
        String cpf;
        do {
            int[] d = new int[11];
            for (int i = 0; i < 9; i++) {
                d[i] = random.nextInt(10);
            }
            d[9] = digitoVerificador(d, 9);
            d[10] = digitoVerificador(d, 10);
            StringBuilder sb = new StringBuilder();
            for (int digito : d) {
                sb.append(digito);
            }
            cpf = sb.toString();
        } while (cpf.chars().distinct().count() == 1 || clienteRepository.findByCpf(cpf).isPresent());
        return cpf;
    }

    private static int digitoVerificador(int[] d, int tamanho) {
        int soma = 0;
        for (int i = 0; i < tamanho; i++) {
            soma += d[i] * (tamanho + 1 - i);
        }
        int resto = (soma * 10) % 11;
        return resto == 10 ? 0 : resto;
    }
}
