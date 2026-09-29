package com.exemplo.clientemanager.service;

import com.exemplo.clientemanager.model.Cliente;
import com.exemplo.clientemanager.model.Servico;
import com.exemplo.clientemanager.repository.ServicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ServicoService {

    private final ServicoRepository servicoRepository;
    private final ClienteService clienteService;

    public ServicoService(ServicoRepository servicoRepository, ClienteService clienteService) {
        this.servicoRepository = servicoRepository;
        this.clienteService = clienteService;
    }

    public List<Servico> listarPorCliente(Long clienteId) {
        return servicoRepository.findByClienteIdOrderByDataServicoDescIdDesc(clienteId);
    }

    public Servico salvar(Long clienteId, Servico servico) {
        Cliente cliente = clienteService.buscarPorId(clienteId);
        servico.setId(null);
        servico.setCliente(cliente);
        if (servico.getRealizado() == null) {
            servico.setRealizado(false);
        }
        return servicoRepository.save(servico);
    }

    @Transactional
    public void excluir(Long clienteId, Long servicoId) {
        Servico servico = servicoRepository.findById(servicoId)
                .orElseThrow(() -> new IllegalArgumentException("Servico nao encontrado"));
        if (!servico.getCliente().getId().equals(clienteId)) {
            throw new IllegalArgumentException("Servico nao pertence a este cliente");
        }
        servicoRepository.delete(servico);
    }
}
