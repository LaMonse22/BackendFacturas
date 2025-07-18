package com.facturacion.facturacion.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompaniaDTO {
    private String nombre;
    private String porcentaje;
    private String estado;
}
