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

import java.math.BigDecimal;

@Entity
@Table(name = "capacity")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Capacity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // ID autoincremental de la capacidad

    @Column(name = "id_lote_facturacion", nullable = false)
    private Long idLoteFacturacion; // Vincula a un lote de facturación

    @Column(name = "contract_id", nullable = false)
    private String contractId; // El identificador del contrato

    @Column(name = "valor_a_pagar", nullable = false)
    private BigDecimal valorAPagar; // La suma de gross pay para este contractId
}
