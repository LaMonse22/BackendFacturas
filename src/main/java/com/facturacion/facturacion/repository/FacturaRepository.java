package com.facturacion.facturacion.repository;

import com.facturacion.facturacion.model.Factura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FacturaRepository extends JpaRepository<Factura, Long> {

    // Método para encontrar facturas por el ID del lote
    List<Factura> findByIdLoteFacturacion(Long idLoteFacturacion);

    // Método para encontrar una factura por idViaje (para la lógica de actualización de "PENDIENTE")
    Optional<Factura> findByIdViaje(String idViaje);

    //Método para buscar facturas por estado
    List<Factura> findByEstado(String estado);
    List<Factura> findByContractId(String contractId);

    // Método para encontrar facturas por idLoteFacturacion, ordenadas por nombre de compañía y drivername
    // Necesitaremos que Spring Data JPA nos ayude con esto, o una query personalizada.
    // Por ahora, solo ordenamos por idLoteFacturacion. La ordenación por nombre de compañía
    // y drivername es más compleja ya que implica JOINs, que veremos más adelante.
    // Podemos empezar con la ordenación en memoria en el backend si es necesario,
    // o construir una query más avanzada.
    // List<Factura> findByIdLoteFacturacionOrderByCompaniaNombreAscDriverNombreAsc(Long idLoteFacturacion);
    // Dejaremos la ordenación compleja para cuando introduzcamos DTOs y lógica de negocio en servicios.
    // Por ahora, un simple findByIdLoteFacturacion será suficiente.
}
