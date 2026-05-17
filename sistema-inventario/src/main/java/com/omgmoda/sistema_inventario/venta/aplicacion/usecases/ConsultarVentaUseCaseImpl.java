package com.omgmoda.sistema_inventario.venta.aplicacion.usecases;

import com.omgmoda.sistema_inventario.shared.dominio.exception.NotFoundException;
import com.omgmoda.sistema_inventario.venta.aplicacion.dto.VentaResponseDTO;
import com.omgmoda.sistema_inventario.venta.aplicacion.ports.IConsultarVentaUseCase;
import com.omgmoda.sistema_inventario.venta.dominio.EstadoVenta;
import com.omgmoda.sistema_inventario.venta.dominio.ports.IVentaRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementación del caso de uso: Consultar Ventas (RF06).
 * Delega en IVentaRepository y mapea a DTOs de respuesta.
 */
public class ConsultarVentaUseCaseImpl implements IConsultarVentaUseCase {

    private final IVentaRepository ventaRepository;

    public ConsultarVentaUseCaseImpl(IVentaRepository ventaRepository) {
        this.ventaRepository = ventaRepository;
    }

    @Override
    public VentaResponseDTO buscarPorId(Long idVenta) {
        return ventaRepository
                .findById(idVenta)
                .map(RegistrarVentaUseCaseImpl::toDTO)
                .orElseThrow(() -> new NotFoundException(
                        "Venta no encontrada con id: " + idVenta
                ));
    }

    @Override
    public List<VentaResponseDTO> buscarPorUsuario(Long idUsuario) {
        return ventaRepository.findByUsuarioId(idUsuario)
                .stream()
                .map(RegistrarVentaUseCaseImpl::toDTO)
                .toList();
    }

    @Override
    public List<VentaResponseDTO> buscarPorEstado(EstadoVenta estado) {
        return ventaRepository.findByEstado(estado)
                .stream()
                .map(RegistrarVentaUseCaseImpl::toDTO)
                .toList();
    }

    @Override
    public List<VentaResponseDTO> buscarPorFechas(LocalDateTime desde, LocalDateTime hasta) {
        return ventaRepository.findByFechaEntre(desde, hasta)
                .stream()
                .map(RegistrarVentaUseCaseImpl::toDTO)
                .toList();
    }
}
