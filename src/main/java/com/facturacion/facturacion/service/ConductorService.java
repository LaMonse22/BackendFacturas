package com.facturacion.facturacion.service;

import com.facturacion.facturacion.model.Conductor;
import com.facturacion.facturacion.repository.ConductorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ConductorService {

    private final ConductorRepository conductorRepository;

    @Autowired
    public ConductorService(ConductorRepository conductorRepository) {
        this.conductorRepository = conductorRepository;
    }

    /**
     * Obtiene todos los conductores.
     * @return Lista de todos los conductores.
     */
    public List<Conductor> obtenerTodosLosConductores() {
        return conductorRepository.findAll();
    }

    /**
     * Busca un conductor por su ID.
     * @param id El ID del conductor.
     * @return Un Optional que contiene el conductor si se encuentra, o vacío si no.
     */
    public Optional<Conductor> buscarConductorPorId(Long id) {
        return conductorRepository.findById(id);
    }

    /**
     * Busca un conductor por su nombre.
     * @param nombre El nombre del conductor.
     * @return Un Optional que contiene el conductor si se encuentra, o vacío si no.
     */
    public Optional<Conductor> buscarConductorPorNombre(String nombre) {
        return conductorRepository.findByNombre(nombre);
    }

    // No incluimos métodos de crear/actualizar/eliminar aquí ya que se asume que la tabla es pre-cargada.
}