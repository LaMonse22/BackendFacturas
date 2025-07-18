package com.facturacion.facturacion.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate; // Solo necesitamos la fecha para el rango

@Entity
@Table(name = "lote_facturacion")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoteFacturacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // ID autoincremental del lote

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio; // Fecha de inicio del rango de facturación

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin; // Fecha de fin del rango de facturación


    // Opcional: Podríamos añadir un timestamp de creación para saber cuándo se generó el lote
     @Column(name = "fecha_creacion")
    private LocalDate fechaCreacion;
}