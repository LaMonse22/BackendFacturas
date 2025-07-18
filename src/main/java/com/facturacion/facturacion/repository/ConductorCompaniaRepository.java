package com.facturacion.facturacion.repository;


import com.facturacion.facturacion.model.ConductorCompania;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConductorCompaniaRepository extends JpaRepository<ConductorCompania, Long> {

    // Método para encontrar todas las asociaciones activas de un conductor.
    // Ordena por fecha de asociación descendente para obtener la más reciente primero.
    List<ConductorCompania> findByIdConductorAndEstadoOrderByFechaAsociacionDesc(Long idConductor, String estado);

    // Opcional: Si quisiéramos buscar por los IDs de ambos
    Optional<ConductorCompania> findByIdCompaniaAndIdConductor(Long idCompania, Long idConductor);
}