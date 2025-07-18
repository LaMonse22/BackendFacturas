package com.facturacion.facturacion.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "factura")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Factura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_lote_facturacion", nullable = false)
    private Long idLoteFacturacion;

    @Column(name = "id_viaje", nullable = false)
    private String idViaje;

    @Column(name = "driver_name") // ¡NUEVO CAMPO! Para almacenar el nombre del conductor
    private String driverName;

    @Column(name = "id_compania")
    private Long idCompania;

    private String origen;
    @Column(name = "fecha_origen")
    private LocalDateTime fechaOrigen;

    private String destino;
    @Column(name = "fecha_destino")
    private LocalDateTime fechaDestino;

    @Column(name = "contract_id")
    private String contractId;
    @Column(name = "nombre_compania") // ¡NUEVO CAMPO! Para almacenar el nombre del conductor
    private String nombreCompania;

    private BigDecimal pay;

    private String estado;
}