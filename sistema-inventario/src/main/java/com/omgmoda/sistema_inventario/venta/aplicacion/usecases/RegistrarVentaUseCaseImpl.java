package com.omgmoda.sistema_inventario.venta.aplicacion.usecases;

import com.omgmoda.sistema_inventario.producto.dominio.VarianteProducto;
import com.omgmoda.sistema_inventario.producto.dominio.ports.IVarianteRepository;
import com.omgmoda.sistema_inventario.shared.aplicacion.utils.TextNormalizer;
import com.omgmoda.sistema_inventario.shared.dominio.exception.DomainException;
import com.omgmoda.sistema_inventario.shared.dominio.exception.NotFoundException;
import com.omgmoda.sistema_inventario.venta.aplicacion.dto.CrearVentaDTO;
import com.omgmoda.sistema_inventario.venta.aplicacion.dto.VentaResponseDTO;
import com.omgmoda.sistema_inventario.venta.aplicacion.ports.IRegistrarVentaUseCase;
import com.omgmoda.sistema_inventario.venta.dominio.DetalleVenta;
import com.omgmoda.sistema_inventario.venta.dominio.Venta;
import com.omgmoda.sistema_inventario.venta.dominio.ports.IVentaRepository;

import java.util.List;

/**
 * Implementación del caso de uso: Registrar Venta (RF04, CU-07).
 *
 * Flujo:
 * 1. Valida disponibilidad de stock para todos los ítems antes de operar.
 * 2. Crea el Aggregate Root Venta en estado PENDIENTE.
 * 3. Por cada ítem: carga la variante, agrega el detalle al agregado
 *    y descuenta el stock mediante registrarSalida().
 * 4. Persiste las variantes actualizadas.
 * 5. Llama a venta.completar() para cerrar la venta.
 * 6. Persiste la venta completa con sus detalles (cascade).
 * 7. Retorna VentaResponseDTO con total calculado.
 *
 * IMPORTANTE: Este caso de uso debe ejecutarse dentro de una transacción
 * gestionada por VentaModuleConfig para garantizar atomicidad.
 */
public class RegistrarVentaUseCaseImpl implements IRegistrarVentaUseCase {

    private final IVentaRepository ventaRepository;
    private final IVarianteRepository varianteRepository;

    public RegistrarVentaUseCaseImpl(IVentaRepository ventaRepository,
                                     IVarianteRepository varianteRepository) {
        this.ventaRepository = ventaRepository;
        this.varianteRepository = varianteRepository;
    }

    @Override
    public VentaResponseDTO registrar(CrearVentaDTO dto, Long idUsuario) {

        // 1. Validar stock disponible para todos los ítems antes de modificar nada
        List<VarianteProducto> variantes = dto.items().stream()
                .map(item -> {
                    VarianteProducto variante = varianteRepository
                            .findById(item.idVariante())
                            .orElseThrow(() -> new NotFoundException(
                                    "Variante no encontrada con id: " + item.idVariante()
                            ));
                    if (variante.getStockActual() < item.cantidad())
                        throw new DomainException(
                                "Stock insuficiente para variante id " + item.idVariante() +
                                ". Disponible: " + variante.getStockActual() +
                                ", solicitado: " + item.cantidad()
                        );
                    return variante;
                })
                .toList();

        // 2. Crear el Aggregate Root Venta en estado PENDIENTE
        Venta venta = new Venta(idUsuario, TextNormalizer.normalizeRequired(dto.metodoPago(), "El metodo de pago"));

        // 3. Agregar detalles y descontar stock por cada ítem
        for (int i = 0; i < dto.items().size(); i++) {
            CrearVentaDTO.ItemVentaDTO item = dto.items().get(i);
            VarianteProducto variante = variantes.get(i);

            venta.agregarDetalle(
                    item.idVariante(),
                    item.cantidad(),
                    item.precioUnitario()
            );

            variante.registrarSalida(item.cantidad());
        }

        // 4. Persistir variantes con stock actualizado
        variantes.forEach(varianteRepository::save);

        // 5. Completar la venta en el dominio
        venta.completar();

        // 6. Persistir la venta con sus detalles
        Venta persistida = ventaRepository.save(venta);

        // 7. Retornar DTO con total calculado
        return toDTO(persistida);
    }

    // ── Mapper interno ─────────────────────────────────────────────────────────

    static VentaResponseDTO toDTO(Venta venta) {
        List<VentaResponseDTO.DetalleVentaResponseDTO> detallesDTO = venta.getDetalles()
                .stream()
                .map(d -> new VentaResponseDTO.DetalleVentaResponseDTO(
                        d.getId(),
                        d.getIdVariante(),
                        d.getCantidad(),
                        d.getPrecioUnitario(),
                        d.calcularSubtotal()
                ))
                .toList();

        return new VentaResponseDTO(
                venta.getId(),
                venta.getIdUsuario(),
                venta.getEstado(),
                venta.getMetodoPago(),
                venta.getFecha(),
                detallesDTO,
                venta.calcularTotal()
        );
    }
}
