package com.facturacion.facturacion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ViajeAgrupadoDTO {
    @NotBlank(message = "El ID de viaje no puede estar vacío.")
    private String idViaje; // blockid o tripid
    private String driverName; // Necesario para buscar la compañía
    @NotBlank(message = "El origen no puede estar vacío.")
    private String origen;
    @NotNull(message = "La fecha de origen no puede ser nula.")
    private LocalDateTime fechaOrigen;
    @NotBlank(message = "El destino no puede estar vacío.")
    private String destino;
    @NotNull(message = "La fecha de destino no puede ser nula.")
    private LocalDateTime fechaDestino;
    @NotNull(message = "El estado no puede ser nulo")
    private String estado;
}