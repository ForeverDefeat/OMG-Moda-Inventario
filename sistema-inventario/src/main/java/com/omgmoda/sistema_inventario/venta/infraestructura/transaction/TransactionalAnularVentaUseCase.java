package com.omgmoda.sistema_inventario.venta.infraestructura.transaction;

import com.omgmoda.sistema_inventario.venta.aplicacion.dto.VentaResponseDTO;
import com.omgmoda.sistema_inventario.venta.aplicacion.ports.IAnularVentaUseCase;
import org.springframework.transaction.annotation.Transactional;

public class TransactionalAnularVentaUseCase implements IAnularVentaUseCase {

    private final IAnularVentaUseCase delegate;

    public TransactionalAnularVentaUseCase(IAnularVentaUseCase delegate) {
        this.delegate = delegate;
    }

    @Override
    @Transactional
    public VentaResponseDTO anular(Long idVenta, Long idUsuario) {
        return delegate.anular(idVenta, idUsuario);
    }
}
