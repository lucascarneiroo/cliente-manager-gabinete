package com.exemplo.clientemanager.service;

import com.exemplo.clientemanager.model.Cliente;
import com.exemplo.clientemanager.repository.ClienteRepository;
import com.exemplo.clientemanager.repository.ServicoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ClienteServiceTest {

    private ClienteRepository clienteRepository;
    private ServicoRepository servicoRepository;
    private ClienteService clienteService;

    @BeforeEach
    void configurar() throws Exception {
        clienteRepository = mock(ClienteRepository.class);
        servicoRepository = mock(ServicoRepository.class);
        clienteService = new ClienteService();
        setField(clienteService, "clienteRepository", clienteRepository);
        setField(clienteService, "servicoRepository", servicoRepository);
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private void setField(Object target, String nome, Object valor) throws Exception {
        Field campo = target.getClass().getDeclaredField(nome);
        campo.setAccessible(true);
        campo.set(target, valor);
    }

    private Cliente novoCliente(String cpf) {
        Cliente cliente = new Cliente();
        cliente.setNome("Cidadao de Teste");
        cliente.setCpf(cpf);
        cliente.setEndereco("Rua de Teste, 1");
        return cliente;
    }

    @Test
    void bloqueiaCadastroComCpfJaExistente() {
        Cliente existente = novoCliente("11144477735");
        existente.setId(1L);
        when(clienteRepository.findByCpf("11144477735")).thenReturn(Optional.of(existente));

        Cliente novo = novoCliente("111.444.777-35");

        assertThrows(IllegalArgumentException.class, () -> clienteService.salvar(novo));
    }

    @Test
    void permiteEditarOProprioClienteComOMesmoCpf() {
        Cliente existente = novoCliente("11144477735");
        existente.setId(1L);
        when(clienteRepository.findByCpf("11144477735")).thenReturn(Optional.of(existente));
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(existente));

        Cliente edicao = novoCliente("111.444.777-35");
        edicao.setId(1L);
        edicao.setNome("Nome Atualizado");

        assertDoesNotThrow(() -> clienteService.salvar(edicao));
    }

    @Test
    void normalizaCpfRemovendoPontuacao() {
        when(clienteRepository.findByCpf("11144477735")).thenReturn(Optional.empty());
        Cliente cliente = novoCliente("111.444.777-35");

        Cliente salvo = clienteService.salvar(cliente);

        assertEquals("11144477735", salvo.getCpf());
    }
}
