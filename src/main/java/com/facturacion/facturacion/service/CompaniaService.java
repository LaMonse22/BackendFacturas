package com.facturacion.facturacion.service;

import com.facturacion.facturacion.model.Compania;
import com.facturacion.facturacion.repository.CompaniaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service // Indica que esta clase es un componente de servicio de Spring
public class CompaniaService {

    private final CompaniaRepository companiaRepository;

    // Inyección de dependencias: Spring automáticamente nos da una instancia de CompaniaRepository
    @Autowired
    public CompaniaService(CompaniaRepository companiaRepository) {
        this.companiaRepository = companiaRepository;
    }

    /**
     * Crea una nueva compañía.
     * @param compania La compañía a crear (el nombre debe estar seteado).
     * @return La compañía creada con su ID y estado "Activo".
     * @throws IllegalArgumentException Si el nombre de la compañía ya existe.
     */
    public Compania crearCompania(Compania compania) {
        // Validar que el nombre no esté vacío
        if (compania.getNombre() == null || compania.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la compañía no puede estar vacío.");
        }
        // Validar si ya existe una compañía con el mismo nombre
        if (companiaRepository.findByNombre(compania.getNombre()).isPresent()) {
            throw new IllegalArgumentException("Ya existe una compañía con el nombre: " + compania.getNombre());
        }
        compania.setEstado("Activo"); // Estado por defecto al crear
        return companiaRepository.save(compania);
    }

    /**
     * Busca una compañía por su ID.
     * @param id El ID de la compañía.
     * @return Un Optional que contiene la compañía si se encuentra, o vacío si no.
     */
    public Optional<Compania> buscarCompaniaPorId(Long id) {
        return companiaRepository.findById(id);
    }

    /**
     * Busca una compañía por su nombre.
     * @param nombre El nombre de la compañía.
     * @return Un Optional que contiene la compañía si se encuentra, o vacío si no.
     */
    public Optional<Compania> buscarCompaniaPorNombre(String nombre) {
        return companiaRepository.findByNombre(nombre);
    }

    /**
     * Obtiene todas las compañías.
     * @return Una lista de todas las compañías.
     */
    public List<Compania> obtenerTodasLasCompanias() {
        return companiaRepository.findAll();
    }

    /**
     * Actualiza una compañía existente.
     * @param id El ID de la compañía a actualizar.
     * @param companiaActualizada Los datos de la compañía con las actualizaciones (nombre y/o estado).
     * @return La compañía actualizada.
     * @throws RuntimeException Si la compañía no se encuentra o el nuevo nombre ya existe.
     */
    public Compania actualizarCompania(Long id, Compania companiaActualizada) throws Exception {

        Compania companiaExistente = companiaRepository.findById(id).get();
        if (companiaRepository.findById(id).isPresent()) {

            if (!companiaExistente.getNombre().equals(companiaActualizada.getNombre()) &&
                    companiaRepository.findByNombre(companiaActualizada.getNombre()).isPresent()) {
                throw new IllegalArgumentException("El nuevo nombre de compañía ya existe: "
                        + companiaActualizada.getNombre());
            }


            companiaExistente.setNombre(companiaActualizada.getNombre());
            companiaExistente.setEstado(companiaActualizada.getEstado());
            companiaExistente.setPorcentaje(companiaActualizada.getPorcentaje());
            return companiaRepository.save(companiaExistente);
        } else {
            throw new Exception("No se encontró compañía con id " + id);
        }

    }

    /**
     * Elimina una compañía por su ID. (Opcional, pero bueno tenerlo)
     * @param id El ID de la compañía a eliminar.
     */
    public void eliminarCompania(Long id) {
        companiaRepository.deleteById(id);
    }

    public List<Compania> companiasByNombre(String nombre) {
        return companiaRepository.findByNombreContaining(nombre);
    }
}