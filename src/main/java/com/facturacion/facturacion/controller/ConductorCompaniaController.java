package com.facturacion.facturacion.controller;

import com.facturacion.facturacion.dto.AsociacionRequest;
import com.facturacion.facturacion.model.ConductorCompania;
import com.facturacion.facturacion.model.Compania; // Para obtener las compañías
import com.facturacion.facturacion.model.Conductor; // Para obtener los conductores
import com.facturacion.facturacion.service.CompaniaService;
import com.facturacion.facturacion.service.ConductorCompaniaService;
import com.facturacion.facturacion.service.ConductorService; // Aún no lo tenemos, lo crearemos en el siguiente paso
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conductor-compania")
@CrossOrigin(origins = "http://localhost:3000")
public class ConductorCompaniaController {

    private final ConductorCompaniaService conductorCompaniaService;
    private final CompaniaService companiaService;
    private final ConductorService conductorService; // Necesitamos un servicio para Conductor

    // @Autowired en el constructor para inyección de dependencias
    @Autowired
    public ConductorCompaniaController(ConductorCompaniaService conductorCompaniaService,
                                       CompaniaService companiaService,
                                       ConductorService conductorService) {
        this.conductorCompaniaService = conductorCompaniaService;
        this.companiaService = companiaService;
        this.conductorService = conductorService;
    }

    // Endpoint para asociar conductores a una compañía
    // POST http://localhost:8080/api/conductor-compania/asociar
    @PostMapping("/asociar")
    public ResponseEntity<List<ConductorCompania>> asociarConductores(@RequestBody AsociacionRequest request) {
        try {
            if (request.getIdCompania() == null || request.getIdsConductores() == null || request.getIdsConductores().isEmpty()) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // Petición inválida si faltan datos
            }
            List<ConductorCompania> asociaciones = conductorCompaniaService
                    .asociarConductoresACompania(request.getIdCompania(), request.getIdsConductores());
            return new ResponseEntity<>(asociaciones, HttpStatus.CREATED); // 201 Created
        } catch (IllegalArgumentException e) {
            // Captura excepciones de validación del servicio
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // 400 Bad Request
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR); // 500 Internal Server Error
        }
    }

    // Endpoint para obtener todas las compañías (para la lista desplegable)
    // GET http://localhost:8080/api/conductor-compania/companias
    @GetMapping("/companias")
    public ResponseEntity<List<Compania>> obtenerCompaniasParaDropdown() {
        List<Compania> companias = companiaService.obtenerTodasLasCompanias();
        return new ResponseEntity<>(companias, HttpStatus.OK);
    }

    // Endpoint para obtener todos los conductores (para la lista desplegable)
    // GET http://localhost:8080/api/conductor-compania/conductores
    @GetMapping("/conductores")
    public ResponseEntity<List<Conductor>> obtenerConductoresParaDropdown() {
        List<Conductor> conductores = conductorService.obtenerTodosLosConductores();
        return new ResponseEntity<>(conductores, HttpStatus.OK);
    }

    // Opcional: Endpoint para obtener todas las asociaciones si se desea visualizar en el frontend
    // GET http://localhost:8080/api/conductor-compania
    @GetMapping
    public ResponseEntity<List<ConductorCompania>> obtenerTodasLasAsociaciones() {
        List<ConductorCompania> asociaciones = conductorCompaniaService.obtenerTodasLasAsociaciones();
        return new ResponseEntity<>(asociaciones, HttpStatus.OK);
    }
}