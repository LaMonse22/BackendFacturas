package com.facturacion.facturacion.dto;

import java.math.BigDecimal;
import java.util.List;

public class FacturacionConsolidadaResponse {
    private List<FacturaConsolidadaDTO> facturasConsolidadas;
    private BigDecimal totalFacturas;
    private List<CapacityAgrupadaDTO> capacities;
    private BigDecimal totalCapacities;
    private Long idLoteFacturacion;
    private List<FacturaAgrupadaDTO> facturasSinViajeAsociado; // <-- NUEVO CAMPO

    // Constructor completo actualizado
    public FacturacionConsolidadaResponse(
            List<FacturaConsolidadaDTO> facturasConsolidadas,
            BigDecimal totalFacturas,
            List<CapacityAgrupadaDTO> capacities,
            BigDecimal totalCapacities,
            Long idLoteFacturacion,
            List<FacturaAgrupadaDTO> facturasSinViajeAsociado) { // <-- NUEVO PARÁMETRO
        this.facturasConsolidadas = facturasConsolidadas;
        this.totalFacturas = totalFacturas;
        this.capacities = capacities;
        this.totalCapacities = totalCapacities;
        this.idLoteFacturacion = idLoteFacturacion;
        this.facturasSinViajeAsociado = facturasSinViajeAsociado; // <-- ASIGNACIÓN
    }

    // --- Getters ---
    public List<FacturaConsolidadaDTO> getFacturasConsolidadas() {
        return facturasConsolidadas;
    }

    public BigDecimal getTotalFacturas() {
        return totalFacturas;
    }

    public List<CapacityAgrupadaDTO> getCapacities() {
        return capacities;
    }

    public BigDecimal getTotalCapacities() {
        return totalCapacities;
    }

    public Long getIdLoteFacturacion() {
        return idLoteFacturacion;
    }

    public List<FacturaAgrupadaDTO> getFacturasSinViajeAsociado() { // <-- NUEVO GETTER
        return facturasSinViajeAsociado;
    }

    // --- Setters (puedes agregarlos si son necesarios para la deserialización o modificación) ---
    public void setFacturasConsolidadas(List<FacturaConsolidadaDTO> facturasConsolidadas) {
        this.facturasConsolidadas = facturasConsolidadas;
    }

    public void setTotalFacturas(BigDecimal totalFacturas) {
        this.totalFacturas = totalFacturas;
    }

    public void setCapacities(List<CapacityAgrupadaDTO> capacities) {
        this.capacities = capacities;
    }

    public void setTotalCapacities(BigDecimal totalCapacities) {
        this.totalCapacities = totalCapacities;
    }

    public void setIdLoteFacturacion(Long idLoteFacturacion) {
        this.idLoteFacturacion = idLoteFacturacion;
    }

    public void setFacturasSinViajeAsociado(List<FacturaAgrupadaDTO> facturasSinViajeAsociado) { // <-- NUEVO SETTER
        this.facturasSinViajeAsociado = facturasSinViajeAsociado;
    }
}