package com.omgmoda.sistema_inventario.inventario.aplicacion.usecases;

import com.omgmoda.sistema_inventario.inventario.aplicacion.dto.MovimientoResponseDTO;
import com.omgmoda.sistema_inventario.inventario.aplicacion.dto.RegistrarMovimientoDTO;
import com.omgmoda.sistema_inventario.inventario.aplicacion.ports.IRegistrarAjusteUseCase;
import com.omgmoda.sistema_inventario.inventario.dominio.Movimiento;
import com.omgmoda.sistema_inventario.inventario.dominio.TipoMovimiento;
import com.omgmoda.sistema_inventario.inventario.dominio.ports.IMovimientoRepository;
import com.omgmoda.sistema_inventario.producto.dominio.VarianteProducto;
import com.omgmoda.sistema_inventario.producto.dominio.ports.IVarianteRepository;
import com.omgmoda.sistema_inventario.shared.dominio.exception.NotFoundException;

/**
 * Implementación del caso de uso: Ajuste Manual de Stock (CU-08).
 *
 * Flujo:
 * 1. Carga la variante por ID desde IVarianteRepository.
 * 2. Invoca ajustarStock(cantidad, motivo) en la entidad de dominio.
 *    El dominio valida que el motivo no sea vacío.
 * 3. Persiste la variante con el stock corregido.
 * 4. Crea y persiste el Movimiento de tipo AJUSTE para trazabilidad.
 * 5. Retorna el DTO de respuesta con el stock resultante.
 */
public class RegistrarAjusteUseCaseImpl implements IRegistrarAjusteUseCase {

    private final IVarianteRepository varianteRepository;
    private final IMovimientoRepository movimientoRepository;

    public RegistrarAjusteUseCaseImpl(IVarianteRepository varianteRepository,
                                      IMovimientoRepository movimientoRepository) {
        this.varianteRepository = varianteRepository;
        this.movimientoRepository = movimientoRepository;
    }

    @Override
    public MovimientoResponseDTO ajustar(RegistrarMovimientoDTO dto) {

        // 1. Cargar la variante
        VarianteProducto variante = varianteRepository
                .findById(dto.idVariante())
                .orElseThrow(() -> new NotFoundException(
                        "Variante no encontrada con id: " + dto.idVariante()
                ));

        // 2. Delegar la corrección al dominio (valida motivo internamente)
        variante.ajustarStock(dto.cantidad(), dto.motivo());

        // 3. Persistir la variante con el stock ajustado
        varianteRepository.save(variante);

        // 4. Crear y persistir el movimiento de tipo AJUSTE
        Movimiento movimiento = new Movimiento(
                dto.idVariante(),
                dto.idUsuario(),
                TipoMovimiento.AJUSTE,
                dto.cantidad(),
                dto.motivo()
        );
        Movimiento persistido = movimientoRepository.save(movimiento);

        // 5. Retornar respuesta
        return toDTO(persistido, variante.getStockActual());
    }

    private MovimientoResponseDTO toDTO(Movimiento m, int stockResultante) {
        return new MovimientoResponseDTO(
                m.getId(),
                m.getIdVariante(),
                m.getIdUsuario(),
                m.getTipo(),
                m.getCantidad(),
                m.getMotivo(),
                m.getFecha(),
                stockResultante
        );
    }
}
