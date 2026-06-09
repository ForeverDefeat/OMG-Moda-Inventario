package com.omgmoda.sistema_inventario.inventario.infraestructura.transaction;

import com.omgmoda.sistema_inventario.inventario.aplicacion.dto.MovimientoResponseDTO;
import com.omgmoda.sistema_inventario.inventario.aplicacion.dto.RegistrarEntradaDTO;
import com.omgmoda.sistema_inventario.inventario.aplicacion.ports.IRegistrarEntradaUseCase;
import org.springframework.transaction.annotation.Transactional;

public class TransactionalRegistrarEntradaUseCase implements IRegistrarEntradaUseCase {

    private final IRegistrarEntradaUseCase delegate;

    public TransactionalRegistrarEntradaUseCase(IRegistrarEntradaUseCase delegate) {
        this.delegate = delegate;
    }

    @Override
    @Transactional
    public MovimientoResponseDTO registrar(RegistrarEntradaDTO dto, Long idUsuario) {
        return delegate.registrar(dto, idUsuario);
    }
}
