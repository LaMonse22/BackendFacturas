package com.facturacion.facturacion.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AsociacionRequest {
    private Long idCompania;
    private List<Long> idsConductores;
}