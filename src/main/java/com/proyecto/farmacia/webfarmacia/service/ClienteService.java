package com.proyecto.farmacia.webfarmacia.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.proyecto.farmacia.webfarmacia.model.Cliente;
import com.proyecto.farmacia.webfarmacia.repository.ClienteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

@Service
public class ClienteService {
    @Autowired
    private ClienteRepository clienteRepository;

    public List<Cliente> getAllClientes() {
        return clienteRepository.findAll();
    }

    public Page<Cliente> getClientesPage(Pageable pageable) {
        return clienteRepository.findAll(pageable);
    }

    public Cliente saveCliente(Cliente cliente) {
        return clienteRepository.save(cliente);
    }

    public Cliente getClienteById(Long id) {
        return clienteRepository.findById(id).orElse(null);
    }

    public void deleteCliente(Long id) {
        clienteRepository.deleteById(id);
    }
} 