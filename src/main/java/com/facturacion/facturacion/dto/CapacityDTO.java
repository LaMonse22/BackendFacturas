package com.facturacion.facturacion.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CapacityDTO {
    @NotBlank(message = "El Contract id no puede estar vacío.")// Identificador de la carga/viaje asociado a la capacity
    private String contractId;
    private String blockid;
    private String driverName;
    private String compania;
}
