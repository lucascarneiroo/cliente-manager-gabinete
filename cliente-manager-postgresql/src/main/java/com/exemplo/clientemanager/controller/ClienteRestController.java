package com.exemplo.clientemanager.controller;

import com.exemplo.clientemanager.model.Cliente;
import com.exemplo.clientemanager.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * API REST, opcional. Util caso queira consumir os dados de outro lugar
 * (app mobile, outro sistema, um front-end React separado, etc.)
 * Exemplo: GET http://localhost:8080/api/clientes
 */
@RestController
@RequestMapping("/api/clientes")
public class ClienteRestController {

    @Autowired
    private ClienteService clienteService;

    @GetMapping
    public List<Cliente> listar(@RequestParam(required = false) String q) {
        return clienteService.buscar(q);
    }

    @GetMapping("/{id}")
    public Cliente buscarPorId(@PathVariable Long id) {
        return clienteService.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<?> criar(@Valid @RequestBody Cliente cliente) {
        try {
            Cliente salvo = clienteService.salvar(cliente);
            return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @Valid @RequestBody Cliente cliente) {
        try {
            cliente.setId(id);
            return ResponseEntity.ok(clienteService.salvar(cliente));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        clienteService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
