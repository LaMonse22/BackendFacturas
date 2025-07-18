package com.facturacion.facturacion.dto;

import java.util.List;

public class PrevisualizacionInicialDTO {
    private List<FacturaAgrupadaDTO> facturasAgrupadas;
    private List<CapacityAgrupadaDTO> capacities;

    // Constructor
    public PrevisualizacionInicialDTO(List<FacturaAgrupadaDTO> facturasAgrupadas, List<CapacityAgrupadaDTO> capacities) {
        this.facturasAgrupadas = facturasAgrupadas;
        this.capacities = capacities;
    }

    // Getters
    public List<FacturaAgrupadaDTO> getFacturasAgrupadas() {
        return facturasAgrupadas;
    }

    public List<CapacityAgrupadaDTO> getCapacities() {
        return capacities;
    }

    // Setters (opcional, pero útil para frameworks como Spring si se deserializa)
    public void setFacturasAgrupadas(List<FacturaAgrupadaDTO> facturasAgrupadas) {
        this.facturasAgrupadas = facturasAgrupadas;
    }

    public void setCapacities(List<CapacityAgrupadaDTO> capacities) {
        this.capacities = capacities;
    }
}