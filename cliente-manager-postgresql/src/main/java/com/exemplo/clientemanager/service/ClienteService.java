package com.exemplo.clientemanager.service;

import com.exemplo.clientemanager.model.Cliente;
import com.exemplo.clientemanager.repository.ClienteRepository;
import com.exemplo.clientemanager.repository.ServicoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.time.LocalDate;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ServicoRepository servicoRepository;

    public List<Cliente> listarTodos() {
        return clienteRepository.findAll();
    }

    public List<Cliente> buscar(String termo) {
        if (termo == null || termo.isBlank()) {
            return listarTodos();
        }
        return clienteRepository.findByNomeContainingIgnoreCaseOrCpfContainingOrTelefoneContaining(termo, termo, termo);
    }

    public Cliente buscarPorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente nao encontrado: " + id));
    }

    public Cliente salvar(Cliente cliente) {
        // Normaliza o CPF removendo pontuacao antes de salvar
        cliente.setCpf(cliente.getCpf().replaceAll("[^0-9]", ""));
        if (cliente.getTelefone() != null) {
            cliente.setTelefone(cliente.getTelefone().trim());
        }

        clienteRepository.findByCpf(cliente.getCpf()).ifPresent(existente -> {
            if (cliente.getId() == null || !existente.getId().equals(cliente.getId())) {
                throw new IllegalArgumentException("Ja existe um cliente cadastrado com este CPF");
            }
        });

        if (cliente.getId() != null) {
            Cliente existente = buscarPorId(cliente.getId());
            existente.setNome(cliente.getNome());
            existente.setCpf(cliente.getCpf());
            existente.setTelefone(cliente.getTelefone());
            existente.setEndereco(cliente.getEndereco());
            existente.setDescricao(cliente.getDescricao());
            existente.setDataNascimento(cliente.getDataNascimento());
            return clienteRepository.save(existente);
        }

        return clienteRepository.save(cliente);
    }

    public List<Cliente> aniversariantesDeHoje() {
        LocalDate hoje = LocalDate.now();
        return clienteRepository.findAll().stream()
                .filter(cliente -> cliente.getDataNascimento() != null)
                .filter(cliente -> cliente.getDataNascimento().getMonthValue() == hoje.getMonthValue()
                        && cliente.getDataNascimento().getDayOfMonth() == hoje.getDayOfMonth())
                .toList();
    }

    @Transactional
    public void excluir(Long id) {
        servicoRepository.deleteByClienteId(id);
        clienteRepository.deleteById(id);
    }
}
