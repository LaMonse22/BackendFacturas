package com.facturacion.facturacion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FacturaAgrupadaDTO {
    @NotBlank(message = "El ID de viaje no puede estar vacío.")
    private String idViaje;

    @NotBlank(message = "El Contract ID no puede estar vacío.")
    private String contractId;

    @NotNull(message = "El valor a pagar no puede ser nulo.")
    private BigDecimal valorAPagar;
}