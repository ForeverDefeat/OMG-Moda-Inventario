package com.omgmoda.sistema_inventario.venta.infraestructura.transaction;

import com.omgmoda.sistema_inventario.venta.aplicacion.dto.CrearVentaDTO;
import com.omgmoda.sistema_inventario.venta.aplicacion.dto.VentaResponseDTO;
import com.omgmoda.sistema_inventario.venta.aplicacion.ports.IRegistrarVentaUseCase;
import org.springframework.transaction.annotation.Transactional;

public class TransactionalRegistrarVentaUseCase implements IRegistrarVentaUseCase {

    private final IRegistrarVentaUseCase delegate;

    public TransactionalRegistrarVentaUseCase(IRegistrarVentaUseCase delegate) {
        this.delegate = delegate;
    }

    @Override
    @Transactional
    public VentaResponseDTO registrar(CrearVentaDTO dto, Long idUsuario) {
        return delegate.registrar(dto, idUsuario);
    }
}
