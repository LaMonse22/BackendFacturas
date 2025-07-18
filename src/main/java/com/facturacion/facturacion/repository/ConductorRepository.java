package com.facturacion.facturacion.repository;

import com.facturacion.facturacion.model.Conductor;
import com.facturacion.facturacion.model.ConductorCompania;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface ConductorRepository extends JpaRepository<Conductor, Long> {

    Optional<Conductor> findByNombre(String nombre);
    Optional<Conductor> findById(Long id);
    List<Conductor> findAll();

}
