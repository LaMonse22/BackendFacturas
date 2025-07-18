package com.facturacion.facturacion.controller;

import com.facturacion.facturacion.dto.CompaniaDTO;
import com.facturacion.facturacion.model.Compania;
import com.facturacion.facturacion.service.CompaniaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus; // Para códigos de estado HTTP
import org.springframework.http.ResponseEntity; // Para construir respuestas HTTP
import org.springframework.web.bind.annotation.*; // Anotaciones REST

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController // Indica que esta clase es un controlador REST
@RequestMapping("/api/companias") // Prefijo de la URL para todos los endpoints en este controlador
public class CompaniaController {

    private final CompaniaService companiaService;

    @Autowired
    public CompaniaController(CompaniaService companiaService) {
        this.companiaService = companiaService;
    }

    // Endpoint para crear una nueva compañía (POST http://localhost:8080/api/companias)
    @PostMapping
    public ResponseEntity<Compania> crearCompania(@RequestBody Compania compania) {
        try {
            Compania nuevaCompania = companiaService.crearCompania(compania);
            return new ResponseEntity<>(nuevaCompania, HttpStatus.CREATED); // Código 201 Created
        } catch (IllegalArgumentException e) {
            // Si hay un error de validación (nombre duplicado, etc.)
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST); // Código 400 Bad Request
            // En un caso real, enviaríamos un DTO de error con el mensaje
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR); // Código 500
        }
    }

    // Endpoint para obtener todas las compañías (GET http://localhost:8080/api/companias)
    @GetMapping
    public ResponseEntity<List<CompaniaDTO>> obtenerTodasLasCompanias() {
        List<Compania> companias = companiaService.obtenerTodasLasCompanias();
        List<CompaniaDTO> companiaDTOS = new ArrayList<>();
        for (Compania compania : companias) {
            CompaniaDTO companiaDTO = new CompaniaDTO();
            companiaDTO.setNombre(compania.getNombre());
            companiaDTO.setPorcentaje(companiaDTO.getPorcentaje());
            companiaDTO.setEstado(compania.getEstado());
        }
        return new ResponseEntity<>(companiaDTOS, HttpStatus.OK); // Código 200 OK
    }

    // Endpoint para buscar una compañía por su nombre (GET http://localhost:8080/api/companias/nombre?nombre=MiEmpresa)
    @GetMapping("/nombre")
    public ResponseEntity<Compania> buscarCompaniaPorNombre(@RequestParam String nombre) {
        Optional<Compania> compania = companiaService.buscarCompaniaPorNombre(nombre);
        return compania.map(c -> new ResponseEntity<>(c, HttpStatus.OK)) // Si la encuentra, 200 OK
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND)); // Si no, 404 Not Found
    }
    @GetMapping("/all/nombre")
    public ResponseEntity<List<Compania>> buscarCompaniasPorNombre(@RequestParam String nombre) {
       List<Compania> companias= companiaService.companiasByNombre(nombre);
        return new ResponseEntity<>(companias, HttpStatus.OK);
    }

    // Endpoint para actualizar una compañía por su ID (PUT http://localhost:8080/api/companias/{id})
    @PutMapping("/{id}")
    public ResponseEntity<Compania> actualizarCompania(@PathVariable Long id, @RequestBody Compania compania) {
        try {
            Compania companiaActualizada = companiaService.actualizarCompania(id, compania);
            return new ResponseEntity<>(companiaActualizada, HttpStatus.OK); // Código 200 OK
        } catch (RuntimeException e) {
            // Si la compañía no se encuentra, o nombre duplicado
            if (e instanceof IllegalArgumentException) {
                return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST); // 400 Bad Request
            }
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND); // 404 Not Found
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR); // 500
        }
    }

    // Endpoint para eliminar una compañía (DELETE http://localhost:8080/api/companias/{id})
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> eliminarCompania(@PathVariable Long id) {
        try {
            companiaService.eliminarCompania(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT); // Código 204 No Content (éxito sin cuerpo de respuesta)
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR); // Código 500
        }
    }
}