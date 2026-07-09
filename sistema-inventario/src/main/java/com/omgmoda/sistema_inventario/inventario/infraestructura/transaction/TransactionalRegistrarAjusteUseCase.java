package com.omgmoda.sistema_inventario.inventario.infraestructura.transaction;

import com.omgmoda.sistema_inventario.inventario.aplicacion.dto.MovimientoResponseDTO;
import com.omgmoda.sistema_inventario.inventario.aplicacion.dto.RegistrarAjusteDTO;
import com.omgmoda.sistema_inventario.inventario.aplicacion.ports.IRegistrarAjusteUseCase;
import org.springframework.transaction.annotation.Transactional;

/**
 * Decorador transaccional que aplica limites de transaccion al caso de uso delegado.
 */
public class TransactionalRegistrarAjusteUseCase implements IRegistrarAjusteUseCase {

    private final IRegistrarAjusteUseCase delegate;

    public TransactionalRegistrarAjusteUseCase(IRegistrarAjusteUseCase delegate) {
        this.delegate = delegate;
    }

    @Override
    @Transactional
    public MovimientoResponseDTO ajustar(RegistrarAjusteDTO dto, Long idUsuario) {
        return delegate.ajustar(dto, idUsuario);
    }
}
