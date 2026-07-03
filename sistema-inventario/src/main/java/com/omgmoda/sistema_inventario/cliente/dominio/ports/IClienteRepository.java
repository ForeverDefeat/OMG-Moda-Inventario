package com.omgmoda.sistema_inventario.cliente.dominio.ports;

import com.omgmoda.sistema_inventario.cliente.dominio.Cliente;

import java.util.List;

public interface IClienteRepository {

    List<Cliente> findAll();

    Cliente save(Cliente cliente);

    boolean existsByCorreo(String correo);
}
