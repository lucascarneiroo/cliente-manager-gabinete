package com.exemplo.clientemanager.config;

import com.exemplo.clientemanager.model.*;
import com.exemplo.clientemanager.repository.ClienteRepository;
import com.exemplo.clientemanager.service.ClienteService;
import com.exemplo.clientemanager.service.CompromissoService;
import com.exemplo.clientemanager.service.ServicoService;
import com.exemplo.clientemanager.service.UsuarioService;
import com.exemplo.clientemanager.whatsapp.service.ConversaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Cria cidadaos, atendimentos, compromissos e uma conversa simulada de
 * WhatsApp ficticios, exclusivamente no perfil "demo" e somente quando o
 * banco ainda esta vazio. Nunca roda em producao (o perfil "demo" so fica
 * ativo com SPRING_PROFILES_ACTIVE=demo) e nunca usa dados de pessoas reais.
 */
@Component
@Profile("demo")
@Order(2)
public class DemoDataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataLoader.class);

    private final ClienteRepository clienteRepository;
    private final ClienteService clienteService;
    private final ServicoService servicoService;
    private final CompromissoService compromissoService;
    private final UsuarioService usuarioService;
    private final ConversaService conversaService;
    private final AppProperties appProperties;

    public DemoDataLoader(ClienteRepository clienteRepository, ClienteService clienteService,
                           ServicoService servicoService, CompromissoService compromissoService,
                           UsuarioService usuarioService, ConversaService conversaService,
                           AppProperties appProperties) {
        this.clienteRepository = clienteRepository;
        this.clienteService = clienteService;
        this.servicoService = servicoService;
        this.compromissoService = compromissoService;
        this.usuarioService = usuarioService;
        this.conversaService = conversaService;
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

        Cliente ana = cidadaoFicticio("Ana Ficticia da Silva", "11144477735", "(81) 99999-0001",
                "Rua das Flores, 100 - Bairro Exemplo", LocalDate.now().minusYears(34).withMonth(LocalDate.now().getMonthValue()).withDayOfMonth(LocalDate.now().getDayOfMonth()));
        Cliente bruno = cidadaoFicticio("Bruno Exemplo Santos", "12345678909", "(81) 99999-0002",
                "Av. Modelo, 250 - Bairro Teste", LocalDate.of(1990, 3, 12));
        Cliente carla = cidadaoFicticio("Carla Teste Oliveira", "98765432100", "(81) 99999-0003",
                "Travessa Demonstracao, 45 - Bairro Amostra", LocalDate.of(1985, 11, 2));

        servicoService.salvar(ana.getId(), atendimentoFicticio("Duvida sobre iluminacao publica", true));
        servicoService.salvar(bruno.getId(), atendimentoFicticio("Solicitacao de reparo em via publica", false));
        servicoService.salvar(carla.getId(), atendimentoFicticio("Encaminhamento para atendimento social", true));

        LocalDate hoje = LocalDate.now();

        compromisso("Reuniao com lideranca do bairro Exemplo", TipoCompromisso.REUNIAO,
                SituacaoCompromisso.CONFIRMADO, PrioridadeCompromisso.NORMAL,
                hoje, LocalTime.of(9, 0), LocalTime.of(10, 0), "Sala de reunioes (ficticio)", ana);

        compromisso("Sessao legislativa ordinaria", TipoCompromisso.SESSAO_LEGISLATIVA,
                SituacaoCompromisso.AGENDADO, PrioridadeCompromisso.ALTA,
                hoje, LocalTime.of(14, 0), LocalTime.of(17, 0), "Camara Municipal (ficticia)", null);

        compromisso("Visita a comunidade Exemplo", TipoCompromisso.VISITA,
                SituacaoCompromisso.AGENDADO, PrioridadeCompromisso.NORMAL,
                hoje.plusDays(1), LocalTime.of(8, 30), LocalTime.of(11, 0), "Bairro Amostra (ficticio)", bruno);

        compromisso("Audiencia publica sobre saneamento", TipoCompromisso.AUDIENCIA,
                SituacaoCompromisso.CONFIRMADO, PrioridadeCompromisso.URGENTE,
                hoje.plusDays(2), LocalTime.of(19, 0), LocalTime.of(21, 0), "Auditorio Modelo (ficticio)", null);

        compromisso("Atendimento agendado - Carla Teste", TipoCompromisso.ATENDIMENTO_CIDADAO,
                SituacaoCompromisso.AGENDADO, PrioridadeCompromisso.NORMAL,
                hoje.plusDays(3), LocalTime.of(10, 0), LocalTime.of(10, 30), "Gabinete (ficticio)", carla);

        compromisso("Viagem para evento regional (ficticio)", TipoCompromisso.VIAGEM,
                SituacaoCompromisso.ADIADO, PrioridadeCompromisso.BAIXA,
                hoje.plusDays(5), null, null, "Cidade vizinha (ficticia)", null);

        // Simulacao visual do WhatsApp: sempre marcada como "simulada", nunca conectada a um numero real.
        conversaService.receberMensagem("(81) 90000-0009", "Contato Ficticio Demo",
                "Ola, gostaria de saber sobre o horario de atendimento do gabinete. (mensagem simulada)", true);

        log.info("Perfil demo: dados ficticios carregados com sucesso.");
    }

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

    private Cliente cidadaoFicticio(String nome, String cpf, String telefone, String endereco, LocalDate nascimento) {
        Cliente cliente = new Cliente();
        cliente.setNome(nome);
        cliente.setCpf(cpf);
        cliente.setTelefone(telefone);
        cliente.setEndereco(endereco);
        cliente.setDataNascimento(nascimento);
        cliente.setDescricao("Cidadao ficticio criado pelo perfil de demonstracao.");
        return clienteService.salvar(cliente);
    }

    private Servico atendimentoFicticio(String descricao, boolean realizado) {
        Servico servico = new Servico();
        servico.setDescricao(descricao);
        servico.setObservacoes("Atendimento ficticio (perfil de demonstracao).");
        servico.setRealizado(realizado);
        return servico;
    }

    private void compromisso(String titulo, TipoCompromisso tipo, SituacaoCompromisso situacao,
                              PrioridadeCompromisso prioridade, LocalDate data, LocalTime inicio, LocalTime fim,
                              String local, Cliente cidadao) {
        Compromisso compromisso = new Compromisso();
        compromisso.setTitulo(titulo);
        compromisso.setDescricao("Compromisso ficticio gerado pelo perfil de demonstracao.");
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
}
