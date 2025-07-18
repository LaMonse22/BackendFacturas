package com.facturacion.facturacion.repository;


import com.facturacion.facturacion.model.LoteFacturacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoteFacturacionRepository extends JpaRepository<LoteFacturacion, Long> {
    // Métodos CRUD básicos ya proporcionados por JpaRepository
}