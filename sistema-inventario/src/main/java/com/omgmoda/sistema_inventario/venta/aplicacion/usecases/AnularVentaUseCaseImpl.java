package com.omgmoda.sistema_inventario.venta.aplicacion.usecases;

import com.omgmoda.sistema_inventario.producto.dominio.VarianteProducto;
import com.omgmoda.sistema_inventario.producto.dominio.ports.IVarianteRepository;
import com.omgmoda.sistema_inventario.shared.dominio.exception.NotFoundException;
import com.omgmoda.sistema_inventario.venta.aplicacion.dto.VentaResponseDTO;
import com.omgmoda.sistema_inventario.venta.aplicacion.ports.IAnularVentaUseCase;
import com.omgmoda.sistema_inventario.venta.dominio.DetalleVenta;
import com.omgmoda.sistema_inventario.venta.dominio.Venta;
import com.omgmoda.sistema_inventario.venta.dominio.ports.IVentaRepository;

/**
 * Implementación del caso de uso: Anular Venta.
 *
 * Flujo:
 * 1. Carga la venta por ID.
 * 2. Invoca venta.anular() — el dominio valida que esté COMPLETADA.
 * 3. Por cada detalle, carga la variante y devuelve el stock
 *    mediante registrarEntrada(cantidad).
 * 4. Persiste las variantes con stock revertido.
 * 5. Persiste la venta en estado ANULADA.
 * 6. Retorna el DTO de respuesta.
 *
 * IMPORTANTE: Debe ejecutarse dentro de una transacción.
 */
public class AnularVentaUseCaseImpl implements IAnularVentaUseCase {

    private final IVentaRepository ventaRepository;
    private final IVarianteRepository varianteRepository;

    public AnularVentaUseCaseImpl(IVentaRepository ventaRepository,
                                  IVarianteRepository varianteRepository) {
        this.ventaRepository = ventaRepository;
        this.varianteRepository = varianteRepository;
    }

    @Override
    public VentaResponseDTO anular(Long idVenta, Long idUsuario) {

        // 1. Cargar la venta
        Venta venta = ventaRepository
                .findById(idVenta)
                .orElseThrow(() -> new NotFoundException(
                        "Venta no encontrada con id: " + idVenta
                ));

        // 2. Anular en el dominio — valida que esté COMPLETADA
        venta.anular();

        // 3. Revertir stock: por cada detalle, registrar entrada en la variante
        for (DetalleVenta detalle : venta.getDetalles()) {
            VarianteProducto variante = varianteRepository
                    .findById(detalle.getIdVariante())
                    .orElseThrow(() -> new NotFoundException(
                            "Variante no encontrada al revertir stock: " + detalle.getIdVariante()
                    ));
            variante.registrarEntrada(detalle.getCantidad());
            varianteRepository.save(variante);
        }

        // 4. Persistir la venta anulada
        Venta persistida = ventaRepository.save(venta);

        return RegistrarVentaUseCaseImpl.toDTO(persistida);
    }
}
