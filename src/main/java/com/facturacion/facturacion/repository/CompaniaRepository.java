package com.facturacion.facturacion.repository;


import com.facturacion.facturacion.model.Compania;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional; // Para manejar casos donde no se encuentra una compañía

@Repository // Indica que esta interfaz es un componente de repositorio de Spring
public interface CompaniaRepository extends JpaRepository<Compania, Long> {
    // JpaRepository<[Nombre de la Entidad], [Tipo de la Clave Primaria]>

    // Método personalizado para buscar una compañía por su nombre.
    // Spring Data JPA lo implementará automáticamente basándose en el nombre del método.
    Optional<Compania> findByNombre(String nombre);
    List<Compania> findByNombreContaining(String nombre);

}