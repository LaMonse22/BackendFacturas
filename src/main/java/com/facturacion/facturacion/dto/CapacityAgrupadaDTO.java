package com.facturacion.facturacion.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CapacityAgrupadaDTO {
    private String loadId;
    @NotBlank(message = "El Contract id no puede estar vacío.")// Identificador de la carga/viaje asociado a la capacity
    private String contractId;  // ID del contrato de la capacity
    private BigDecimal valorAPagar; // Monto a pagar por la capacity
}