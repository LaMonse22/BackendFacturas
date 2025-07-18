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

@Entity
@Table(name = "compania")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Compania {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, unique = true)
    private String nombre; // Nombre de la compañía, debe ser único

    @Column(name = "estado", nullable = false)
    private String estado; // "Activo" o "Inactivo"
   @Column(name = "porcentaje")
    private float porcentaje;
}