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

import java.time.LocalDateTime; // Para el campo "más reciente"

@Entity
@Table(name = "conductor_compania")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConductorCompania {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // ID autoincremental de la asociación

    @Column(name = "id_compania", nullable = false)
    private Long idCompania; // ID de la compañía asociada

    @Column(name = "id_conductor", nullable = false)
    private Long idConductor; // ID del conductor asociado

    @Column(name = "estado", nullable = false)
    private String estado; // "Activo" o "Inactivo" de esta asociación

    @Column(name = "fecha_asociacion", nullable = false)
    private LocalDateTime fechaAsociacion; // Para determinar la asociación "más reciente"

    // Constructor personalizado para facilitar la creación inicial
    public ConductorCompania(Long idCompania, Long idConductor, String estado) {
        this.idCompania = idCompania;
        this.idConductor = idConductor;
        this.estado = estado;
        this.fechaAsociacion = LocalDateTime.now(); // Se establece al momento de crear
    }
}
