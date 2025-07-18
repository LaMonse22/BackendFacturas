package com.facturacion.facturacion.controller;

import com.facturacion.facturacion.dto.CapacityAgrupadaDTO;
import com.facturacion.facturacion.dto.FacturaAgrupadaDTO;
import com.facturacion.facturacion.dto.FacturaConsolidadaDTO;
import com.facturacion.facturacion.dto.FacturacionConsolidadaResponse;
import com.facturacion.facturacion.dto.PrevisualizacionInicialDTO; // Necesario para el retorno de ExcelProcessingService
import com.facturacion.facturacion.dto.ViajeAgrupadoDTO; // Puede ser útil si el service lo expone directamente
import com.facturacion.facturacion.model.Factura;
import com.facturacion.facturacion.model.LoteFacturacion;
import com.facturacion.facturacion.service.ExcelProcessingService;
import com.facturacion.facturacion.service.FacturacionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map; // Necesario si ExcelProcessingService devuelve List<Map<String, String>>
import java.util.Optional;

@RestController
@RequestMapping("/api/facturacion")
public class FacturacionController {

    private final ExcelProcessingService excelProcessingService;
    private final FacturacionService facturacionService;

    @Autowired
    public FacturacionController(ExcelProcessingService excelProcessingService, FacturacionService facturacionService) {
        this.excelProcessingService = excelProcessingService;
        this.facturacionService = facturacionService;
    }

    /**
     * Endpoint para previsualizar la facturación consolidada a partir de archivos Excel.
     * Recibe los archivos de viajes y facturas, junto con un rango de fechas.
     * Genera un ID de LoteFacturacion en el servicio y devuelve una previsualización
     * de las facturas consolidadas, capacities y facturas sin viaje asociado.
     *
     * @param fileViajes Archivo Excel con la información de los viajes.
     * @param fileFacturas Archivo Excel con la información de las facturas y capacities.
     * @param dateFrom Fecha de inicio para el filtro (formato YYYY-MM-DD).
     * @param dateTo Fecha de fin para el filtro (formato YYYY-MM-DD).
     * @return ResponseEntity con FacturacionConsolidadaResponse si es exitoso,
     * o un código de error si hay un problema.
     */
    @PostMapping("/previsualizar-consolidado")
    public ResponseEntity<FacturacionConsolidadaResponse> previsualizarConsolidado(
            @RequestParam("fileViajes") MultipartFile fileViajes,
            @RequestParam("fileFacturas") MultipartFile fileFacturas,
            @RequestParam("dateFrom") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam("dateTo") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        try {
            System.out.println("Iniciando previsualización consolidada...");

            // 1. Procesar archivo de Viajes
            List<Map<String, String>> rawViajesData = excelProcessingService.processExcelFile(fileViajes);
            List<ViajeAgrupadoDTO> viajesAgrupados = excelProcessingService.agruparViajes(rawViajesData);

            // 2. Procesar archivo de Facturas (incluye agrupamiento de facturas y identificación de capacities)
            PrevisualizacionInicialDTO facturasAndCapacities = excelProcessingService.processFacturasExcelFile(
                    fileFacturas);
            List<FacturaAgrupadaDTO> facturasAgrupadas = facturasAndCapacities.getFacturasAgrupadas();
            List<CapacityAgrupadaDTO> capacities = facturasAndCapacities.getCapacities();

            // 3. Consolidar y obtener la previsualización llamando al servicio de facturación
            // El servicio se encarga de crear el LoteFacturacion y agrupar los datos.
            FacturacionConsolidadaResponse response = facturacionService.consolidarFacturacion(
                    facturasAgrupadas,
                    capacities,
                    viajesAgrupados,
                    dateFrom,
                    dateTo
            );

            System.out.println("Previsualización consolidada finalizada.");
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            // Manejo de errores de entrada/salida (ej. problemas al leer los archivos)
            System.err.println("Error de IO al previsualizar consolidado: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        } catch (Exception e) {
            // Manejo de otros errores generales (ej. problemas de formato, lógica, etc.)
            System.err.println("Error inesperado al previsualizar consolidado: " + e.getMessage());
            e.printStackTrace(); // Imprime la traza completa para depuración
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    /**
     * Endpoint para guardar definitivamente los datos de la previsualización consolidada en la base de datos.
     * Recibe los datos de la previsualización (incluyendo el ID del lote generado previamente).
     *
     * @param consolidadoResponse Objeto FacturacionConsolidadaResponse obtenido de la previsualización.
     * @return ResponseEntity con FacturacionConsolidadaResponse guardada si es exitoso,
     * o un código de error si hay un problema.
     */
    @PostMapping("/guardar-consolidado")
    public ResponseEntity<FacturacionConsolidadaResponse> guardarConsolidado(
           @Valid @RequestBody FacturacionConsolidadaResponse consolidadoResponse) {
        try {
            System.out.println("Iniciando guardado de consolidado para Lote ID: "
                    + consolidadoResponse.getIdLoteFacturacion());
            // Delega la persistencia al servicio de facturación
            FacturacionConsolidadaResponse savedResponse = facturacionService.guardarFacturacion(consolidadoResponse);
            System.out.println("Consolidado guardado exitosamente.");
            return ResponseEntity.ok(savedResponse);
        } catch (Exception e) {
            // Manejo de errores durante la persistencia
            System.err.println("Error al guardar consolidado: " + e.getMessage());
            e.printStackTrace(); // Imprime la traza completa para depuración
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/lotes/{idLote}")
    public ResponseEntity<LoteFacturacion> getLoteFacturacion(@PathVariable Long idLote) {
        Optional<LoteFacturacion> lote = facturacionService.findLoteFacturacionById(idLote);
        return lote.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/lotes/{idLote}/facturas")
    public ResponseEntity<List<Factura>> getFacturasByLote(@PathVariable Long idLote) {
        List<Factura> facturas = facturacionService.findFacturasByLoteFacturacionId(idLote);
        if (facturas.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(facturas);
    }


    /**
     * Endpoint para descargar el archivo Excel de facturas de un lote específico.
     *
     * @param idLote El ID del lote de facturación.
     * @return ResponseEntity con el archivo Excel como byte[].
     */
    @GetMapping("/descargar-facturas/{idLote}")
    public ResponseEntity<byte[]> descargarExcelFacturas(@PathVariable Long idLote) {
        try {
            byte[] excelBytes = facturacionService.generarExcelFacturas(idLote);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM); // Tipo de contenido genérico para binarios
            // O más específico para Excel:
            // headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));

            headers.setContentDispositionFormData("attachment", "facturas_lote_" + idLote + ".xlsx");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);

        } catch (IllegalArgumentException e) {
            // Si no se encuentran facturas para el lote
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage().getBytes()); // O un mensaje de error más estructurado
        } catch (IOException e) {
            // Error al generar el archivo Excel
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al generar el archivo Excel de facturas.".getBytes());
        }
    }

    /**
     * Endpoint para descargar el archivo Excel de capacities de un lote específico.
     *
     * @param idLote El ID del lote de facturación.
     * @return ResponseEntity con el archivo Excel como byte[].
     */
    @GetMapping("/descargar-capacities/{idLote}")
    public ResponseEntity<byte[]> descargarExcelCapacities(@PathVariable Long idLote) {
        try {
            byte[] excelBytes = facturacionService.generarExcelCapacities(idLote);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM); // Tipo de contenido genérico para binarios
            // O más específico para Excel:
            // headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));

            headers.setContentDispositionFormData("attachment", "capacities_lote_" + idLote + ".xlsx");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);

        } catch (IllegalArgumentException e) {
            // Si no se encuentran capacities para el lote
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage().getBytes());
        } catch (IOException e) {
            // Error al generar el archivo Excel
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    "Error al generar el archivo Excel de capacities.".getBytes());
        }
    }

    /**
     * Endpoint para obtener la lista de todos los lotes de facturación guardados.
     * @return Una lista de LoteFacturacion.
     */
    @GetMapping("/lotes")
    public ResponseEntity<List<LoteFacturacion>> getAllLotesFacturacion() {
        List<LoteFacturacion> lotes = facturacionService.findAllLotesFacturacion();
        if (lotes.isEmpty()) {
            return ResponseEntity.noContent().build(); // Retorna 204 No Content si no hay lotes
        }
        return ResponseEntity.ok(lotes);
    }

    /**
     * Endpoint para obtener la FacturacionConsolidadaResponse completa de un lote específico.
     * Esto reconstruye la respuesta desde los datos guardados en la DB (Facturas y Capacities).
     * @param idLote El ID del lote a recuperar.
     * @return FacturacionConsolidadaResponse completa.
     */
    @GetMapping("/lotes/{idLote}/consolidado")
    public ResponseEntity<FacturacionConsolidadaResponse> getConsolidatedResponseForLote(@PathVariable Long idLote) {
        try {
            FacturacionConsolidadaResponse response = facturacionService.getConsolidatedResponseForLote(idLote);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build(); // 404 si el lote no existe
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    @GetMapping("/facturasbycontractid/{contractId}")
    public ResponseEntity<List<FacturaConsolidadaDTO>> getFacturasByContractid(@PathVariable String contractId) {
        try {
            List<FacturaConsolidadaDTO> facturaConsolidadaDTOS = new ArrayList<>();
            List<Factura> facturas = facturacionService.getFacturasByContractid(contractId);
            for (Factura factura : facturas) {
                FacturaConsolidadaDTO facturaDTO = new FacturaConsolidadaDTO();
                facturaDTO.setId(factura.getId());
                facturaDTO.setIdViaje(factura.getIdViaje());
                facturaDTO.setDriverName(factura.getDriverName());
                facturaDTO.setDestino(factura.getDestino());
                facturaDTO.setFechaDestino(factura.getFechaDestino());
                facturaDTO.setOrigen(factura.getOrigen());
                facturaDTO.setDestino(factura.getDestino());
                facturaDTO.setFechaDestino(factura.getFechaDestino());
                facturaDTO.setIdCompania(factura.getIdCompania());
                facturaDTO.setNombreCompania(factura.getNombreCompania());
                facturaDTO.setContractId(factura.getContractId());
                facturaDTO.setEstado(factura.getEstado());
                facturaDTO.setPay(factura.getPay());
                facturaDTO.setIdLoteFacturacion(factura.getIdLoteFacturacion());
                facturaConsolidadaDTOS.add(facturaDTO);
            }
            return ResponseEntity.ok(facturaConsolidadaDTOS);


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}