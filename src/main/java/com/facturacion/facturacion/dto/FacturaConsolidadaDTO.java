package com.facturacion.facturacion.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FacturaConsolidadaDTO {
    private Long id; // ID de la factura si ya existe en DB, o null si es nueva
    private Long idLoteFacturacion;
    private String idViaje;
    private String driverName; // Para ordenar y asociar compañías
    private String origen;
    private LocalDateTime fechaOrigen;
    private String destino;
    private LocalDateTime fechaDestino;
    private String contractId;
    private BigDecimal pay;
    private String estado; // "ACTIVO" o "PENDIENTE"
    private Long idCompania; // El ID de la compañía que se puede editar
    private String nombreCompania;
    private float porcentaje;// El nombre de la compañía para mostrar
}
