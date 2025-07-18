package com.facturacion.facturacion.repository;

import com.facturacion.facturacion.model.Capacity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CapacityRepository extends JpaRepository<Capacity, Long> {

    // Método para encontrar registros de capacidad por el ID del lote
    List<Capacity> findByIdLoteFacturacion(Long idLoteFacturacion);
}