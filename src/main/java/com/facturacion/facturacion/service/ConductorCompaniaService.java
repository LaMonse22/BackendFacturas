package com.facturacion.facturacion.service;

import com.facturacion.facturacion.model.Compania;
import com.facturacion.facturacion.model.Conductor;
import com.facturacion.facturacion.model.ConductorCompania;
import com.facturacion.facturacion.repository.CompaniaRepository;
import com.facturacion.facturacion.repository.ConductorCompaniaRepository;
import com.facturacion.facturacion.repository.ConductorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Importación para @Transactional

import java.util.List;
import java.util.Optional;

@Service
public class ConductorCompaniaService {

    private final ConductorCompaniaRepository conductorCompaniaRepository;
    private final CompaniaRepository companiaRepository;
    private final ConductorRepository conductorRepository;

    @Autowired
    public ConductorCompaniaService(ConductorCompaniaRepository conductorCompaniaRepository,
                                    CompaniaRepository companiaRepository,
                                    ConductorRepository conductorRepository) {
        this.conductorCompaniaRepository = conductorCompaniaRepository;
        this.companiaRepository = companiaRepository;
        this.conductorRepository = conductorRepository;
    }

    /**
     * Asocia uno o varios conductores a una compañía.
     * Desactiva asociaciones previas activas del mismo conductor con cualquier otra compañía.
     *
     * @param idCompania ID de la compañía a asociar.
     * @param idsConductores Lista de IDs de conductores a asociar.
     * @return Lista de las nuevas asociaciones ConductorCompania creadas/actualizadas.
     * @throws IllegalArgumentException Si la compañía o algún conductor no existe.
     */
    @Transactional // Asegura que todas las operaciones dentro del método se ejecuten como una única transacción
    // de base de datos Si algo falla en el medio, todas las operaciones se revierten (rollback), garantizando la integridad de los datos.
    public List<ConductorCompania> asociarConductoresACompania(Long idCompania, List<Long> idsConductores) {
        // 1. Validar que la compañía exista
        Compania compania = companiaRepository.findById(idCompania)
                .orElseThrow(() -> new IllegalArgumentException("Compañía no encontrada con ID: " + idCompania));

        // Lista para almacenar las nuevas asociaciones creadas
        List<ConductorCompania> nuevasAsociaciones = new java.util.ArrayList<>();

        for (Long idConductor : idsConductores) {
            // 2. Validar que el conductor exista
            Conductor conductor = conductorRepository.findById(idConductor)
                    .orElseThrow(() -> new IllegalArgumentException("Conductor no encontrado con ID: " + idConductor));

            // 3. Desactivar asociaciones previas activas para este conductor
            List<ConductorCompania> asociacionesPreviasActivas = conductorCompaniaRepository
                    .findByIdConductorAndEstadoOrderByFechaAsociacionDesc(idConductor, "Activo");

            for (ConductorCompania asociacion : asociacionesPreviasActivas) {
                // Si ya existe una asociación activa con la misma compañía, no hacemos nada con ella,
                // solo si es con OTRA compañía la desactivamos.
                if (!asociacion.getIdCompania().equals(idCompania)) {
                    asociacion.setEstado("Inactivo");
                    conductorCompaniaRepository.save(asociacion);
                } else {
                    // Si la asociación ya existe y está activa con la misma compañía,
                    // simplemente la agregamos a las "nuevas asociaciones" como ya existente y activa
                    // para que el controlador pueda retornarla.
                    nuevasAsociaciones.add(asociacion);
                }
            }

            // 4. Crear o Actualizar la nueva asociación (si no es la que ya existía y estaba activa)
            Optional<ConductorCompania> asociacionExistente = conductorCompaniaRepository
                    .findByIdCompaniaAndIdConductor(idCompania, idConductor);

            if (asociacionExistente.isPresent()) {
                // Si ya existe la asociación para esta compañía y conductor,
                // y estaba inactiva, la reactivamos y actualizamos la fecha
                ConductorCompania existente = asociacionExistente.get();
                if (existente.getEstado().equals("Inactivo")) {
                    existente.setEstado("Activo");
                    existente.setFechaAsociacion(java.time.LocalDateTime.now());
                    nuevasAsociaciones.add(conductorCompaniaRepository.save(existente));
                }
                // Si ya estaba activa y la agregamos arriba, no hacemos nada más.
            } else {
                // Si no existe, creamos una nueva asociación
                ConductorCompania nuevaAsociacion = new ConductorCompania(idCompania, idConductor, "Activo");
                nuevasAsociaciones.add(conductorCompaniaRepository.save(nuevaAsociacion));
            }
        }
        return nuevasAsociaciones;
    }

    /**
     * Busca la compañía más reciente (activa) asociada a un conductor por su nombre.
     * Esto se usará para la lógica de la tabla FACTURA.
     * @param nombreConductor El nombre del conductor.
     * @return Un Optional que contiene la asociación más reciente si se encuentra, o vacío si no.
     */
    public Optional<ConductorCompania> buscarAsociacionMasRecienteActivaPorNombreConductor(String nombreConductor) {
        Optional<Conductor> conductorOpt = conductorRepository.findByNombre(nombreConductor);
        if (conductorOpt.isPresent()) {
            List<ConductorCompania> asociaciones = conductorCompaniaRepository
                    .findByIdConductorAndEstadoOrderByFechaAsociacionDesc(conductorOpt.get().getId(), "Activo");
            // Retorna la primera de la lista, que es la más reciente debido al orden descendente
            return asociaciones.stream().findFirst();
        }
        return Optional.empty(); // No se encontró el conductor
    }

    /**
     * Obtiene todas las asociaciones conductor-compañía.
     * @return Lista de asociaciones ConductorCompania.
     */
    public List<ConductorCompania> obtenerTodasLasAsociaciones() {
        return conductorCompaniaRepository.findAll();
    }

    // Otros métodos de búsqueda o gestión de asociaciones si fueran necesarios.
}