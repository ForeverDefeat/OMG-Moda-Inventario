package com.omgmoda.sistema_inventario.inventario.aplicacion.usecases;

import com.omgmoda.sistema_inventario.inventario.aplicacion.dto.MovimientoResponseDTO;
import com.omgmoda.sistema_inventario.inventario.aplicacion.dto.RegistrarEntradaDTO;
import com.omgmoda.sistema_inventario.inventario.aplicacion.ports.IRegistrarEntradaUseCase;
import com.omgmoda.sistema_inventario.inventario.dominio.Movimiento;
import com.omgmoda.sistema_inventario.inventario.dominio.TipoMovimiento;
import com.omgmoda.sistema_inventario.inventario.dominio.ports.IMovimientoRepository;
import com.omgmoda.sistema_inventario.producto.dominio.VarianteProducto;
import com.omgmoda.sistema_inventario.producto.dominio.ports.IVarianteRepository;
import com.omgmoda.sistema_inventario.shared.dominio.exception.NotFoundException;

/**
 * Implementación del caso de uso: Registrar Entrada de Mercadería (RF03, CU-06).
 *
 * Flujo:
 * 1. Carga la variante por ID desde IVarianteRepository.
 * 2. Invoca registrarEntrada(cantidad) en la entidad de dominio.
 * 3. Persiste la variante actualizada.
 * 4. Crea y persiste el Movimiento de tipo ENTRADA.
 * 5. Retorna el DTO de respuesta con el stock resultante.
 */
public class RegistrarEntradaUseCaseImpl implements IRegistrarEntradaUseCase {

    private final IVarianteRepository varianteRepository;
    private final IMovimientoRepository movimientoRepository;

    public RegistrarEntradaUseCaseImpl(IVarianteRepository varianteRepository,
                                       IMovimientoRepository movimientoRepository) {
        this.varianteRepository = varianteRepository;
        this.movimientoRepository = movimientoRepository;
    }

    @Override
    public MovimientoResponseDTO registrar(RegistrarEntradaDTO dto, Long idUsuario) {

        // 1. Cargar la variante — lanza NotFoundException si no existe
        VarianteProducto variante = varianteRepository
                .findById(dto.idVariante())
                .orElseThrow(() -> new NotFoundException(
                        "Variante no encontrada con id: " + dto.idVariante()
                ));

        // 2. Delegar la lógica de incremento al dominio
        variante.registrarEntrada(dto.cantidad());

        // 3. Persistir la variante con el nuevo stock
        varianteRepository.save(variante);

        // 4. Crear y persistir el movimiento de trazabilidad
        Movimiento movimiento = new Movimiento(
                dto.idVariante(),
                idUsuario,
                TipoMovimiento.ENTRADA,
                dto.cantidad(),
                dto.motivo()
        );
        Movimiento persistido = movimientoRepository.save(movimiento);

        // 5. Retornar respuesta con stock actualizado
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
