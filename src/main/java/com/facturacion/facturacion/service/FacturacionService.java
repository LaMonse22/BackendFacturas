package com.facturacion.facturacion.service;

import com.facturacion.facturacion.dto.CapacityAgrupadaDTO;
import com.facturacion.facturacion.dto.CapacityDTO;
import com.facturacion.facturacion.dto.FacturaAgrupadaDTO;
import com.facturacion.facturacion.dto.FacturaConsolidadaDTO;
import com.facturacion.facturacion.dto.FacturacionConsolidadaResponse;
import com.facturacion.facturacion.dto.ViajeAgrupadoDTO;
import com.facturacion.facturacion.model.Capacity;
import com.facturacion.facturacion.model.Compania;
import com.facturacion.facturacion.model.Conductor;
import com.facturacion.facturacion.model.ConductorCompania;
import com.facturacion.facturacion.model.Factura;
import com.facturacion.facturacion.repository.CapacityRepository;
import com.facturacion.facturacion.repository.CompaniaRepository;
import com.facturacion.facturacion.repository.ConductorCompaniaRepository;
import com.facturacion.facturacion.repository.ConductorRepository;
import com.facturacion.facturacion.repository.FacturaRepository; // Tu repositorio de Factura
import com.facturacion.facturacion.model.LoteFacturacion;
import com.facturacion.facturacion.repository.LoteFacturacionRepository;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap; // Usaremos HashMap para los viajes y controlar los pendientes
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FacturacionService {

    private final LoteFacturacionRepository loteFacturacionRepository;
    private final FacturaRepository facturaRepository;
    private final ConductorCompaniaRepository conductorCompaniaRepository;
    private final ConductorRepository conductorRepository;
    private final CompaniaRepository companiaRepository;
    private final CapacityRepository capacityRepository;
    private final CompaniaService companiaService;

    public FacturacionService(LoteFacturacionRepository loteFacturacionRepository,
                              FacturaRepository facturaRepository, ConductorCompaniaRepository
                                      conductorCompaniaRepository, ConductorRepository conductorRepository,
                              CompaniaRepository companiaRepository, CapacityRepository capacityRepository,
                              CompaniaService companiaService) {
        this.loteFacturacionRepository = loteFacturacionRepository;
        this.facturaRepository = facturaRepository;
        this.conductorCompaniaRepository = conductorCompaniaRepository;
        this.conductorRepository = conductorRepository;
        this.companiaRepository = companiaRepository;
        this.capacityRepository = capacityRepository;
        this.companiaService = companiaService;

    }

    @Transactional
    public FacturacionConsolidadaResponse consolidarFacturacion(
            List<FacturaAgrupadaDTO> facturasAgrupadas,
            List<CapacityAgrupadaDTO> capacities,
            List<ViajeAgrupadoDTO> viajesAgrupados,
            LocalDate dateFrom,
            LocalDate dateTo) {

        List<FacturaConsolidadaDTO> facturasConsolidadas = new ArrayList<>();
        List<FacturaAgrupadaDTO> facturasSinViajeAsociado = new ArrayList<>();

        // Paso 1: Mapear Viajes por su idViaje.
        // Usaremos un HashMap mutable para poder marcar los viajes como "pagados"
        // (o asociados a una factura) a medida que los encontremos.
        Map<String, ViajeAgrupadoDTO> viajesMap = new HashMap<>(); // Cambiamos a HashMap
        for (ViajeAgrupadoDTO viaje : viajesAgrupados) {
            Optional<Factura> facturaDb = facturaRepository.findByIdViaje(viaje.getIdViaje());
         //Para que no procese viajes que ya han sido pagados en otras facturas
            if (facturaDb.isPresent() && facturaDb.get().getEstado().contains("PAGADO")) {
                continue;
            }

            viajesMap.put(viaje.getIdViaje(), viaje);
        }


        BigDecimal totalFacturasConsolidadas = BigDecimal.ZERO;
        BigDecimal totalCapacities = BigDecimal.ZERO;

        LoteFacturacion nuevoLote = new LoteFacturacion();
        nuevoLote.setFechaCreacion(LocalDate.now());
        nuevoLote.setFechaInicio(dateFrom);
        nuevoLote.setFechaFin(dateTo);
        nuevoLote = loteFacturacionRepository.save(nuevoLote);

        Long idLoteGenerado = nuevoLote.getId();
        List<Factura> facturasPendientes = getFacturasPendientes();

        // Procesar Facturas Agrupadas
        for (FacturaAgrupadaDTO facturaAgrupada : facturasAgrupadas) {
            ViajeAgrupadoDTO viajeAsociado = viajesMap.get(facturaAgrupada.getIdViaje());
            String nombreCompania = null;
            Long idCompania = null;
            float  porcentaje =0;

            if (viajeAsociado != null) {
               Compania companiaAsociada = getNombreCompania(viajeAsociado);
               if (companiaAsociada != null) {
                   nombreCompania = companiaAsociada.getNombre();
                   idCompania = companiaAsociada.getId();
                   porcentaje = companiaAsociada.getPorcentaje();
               }
                String estado = "PAGADO";
                if (!viajeAsociado.getEstado().equals("Completed")) {
                    estado = viajeAsociado.getEstado()+"-PAGADO";
                }
                // Si encontramos un viaje asociado, consolidamos la factura
                FacturaConsolidadaDTO consolidada = new FacturaConsolidadaDTO(
                        null,
                        idLoteGenerado,
                        viajeAsociado.getIdViaje(),
                        viajeAsociado.getDriverName(),
                        viajeAsociado.getOrigen(),
                        viajeAsociado.getFechaOrigen(),
                        viajeAsociado.getDestino(),
                        viajeAsociado.getFechaDestino(),
                        facturaAgrupada.getContractId(),
                        facturaAgrupada.getValorAPagar(),
                        estado, // <-- CAMBIO CLAVE: Viaje asociado a factura = PAGADO
                        idCompania,
                        nombreCompania,
                        porcentaje
                );
                facturasConsolidadas.add(consolidada);
                totalFacturasConsolidadas = totalFacturasConsolidadas.add(facturaAgrupada.getValorAPagar());

                // Opcional: Eliminar el viaje del mapa para identificar más fácilmente los "pendientes"
                // después de este bucle.
                viajesMap.remove(facturaAgrupada.getIdViaje());

            } else {
                // Si NO encontramos un viaje asociado para esta Factura, buscamo en facturas pendientes
                // y si no está la añadimos a 'sin viaje'

                if (facturasPendientes != null) {
                    Factura facturaPendiente = facturasPendientes.stream().filter( factura ->
                            factura.getIdViaje().equals(facturaAgrupada.getIdViaje())).findFirst().orElse(null);
                    if (facturaPendiente != null) {

                        if (facturaPendiente.getIdCompania() != null) {
                            Optional<Compania> compania = companiaRepository.findById(facturaPendiente.getIdCompania());
                            if (compania.isPresent()) {
                                nombreCompania = compania.get().getNombre();
                                idCompania = compania.get().getId();
                            }
                        }

                        String estado = "PAGADO";
                        if (facturaPendiente.getEstado().equals("PENDIENTE")) {
                            estado = facturaPendiente.getEstado()+"-PAGADO";
                        }

                        // Si encontramos una factura pendiente dentro de las nuevas facturas, comsolidamos
                        FacturaConsolidadaDTO consolidada = new FacturaConsolidadaDTO(
                                facturaPendiente.getId(),
                                idLoteGenerado,
                                facturaPendiente.getIdViaje(),
                                facturaPendiente.getDriverName(),
                                facturaPendiente.getOrigen(),
                                facturaPendiente.getFechaOrigen(),
                                facturaPendiente.getDestino(),
                                facturaPendiente.getFechaDestino(),
                                facturaAgrupada.getContractId(),
                                facturaAgrupada.getValorAPagar(),
                                estado, // <-- CAMBIO CLAVE: Viaje asociado a factura = PAGADO
                                idCompania,
                                nombreCompania,
                                0
                        );
                        facturasConsolidadas.add(consolidada);
                        totalFacturasConsolidadas = totalFacturasConsolidadas.add(facturaAgrupada.getValorAPagar());

                        // Opcional: Eliminar el viaje del mapa para identificar más fácilmente los "pendientes"
                        // después de este bucle.
                        viajesMap.remove(facturaAgrupada.getIdViaje());
                    } else {
                        facturasSinViajeAsociado.add(facturaAgrupada);
                        System.err.println("Advertencia: Factura con ID de viaje " + facturaAgrupada.getIdViaje()
                                + " no tiene un viaje asociado en el archivo de Viajes.");
                    }
                }

            }
        }

        // --- Manejo de Viajes No Asociados a Facturas (los PENDIENTES) ---
        // Los viajes que quedaron en 'viajesMap' son aquellos a los que no se les encontró factura.
        // Para reportarlos, podrías querer convertirlos también a un DTO similar a FacturaConsolidadaDTO
        // o a un nuevo DTO para viajes pendientes.
        // Por ahora, solo los listaremos en los logs o si necesitas reportarlos en la respuesta.
        if (!viajesMap.isEmpty()) {
            System.out.println("Los siguientes viajes NO tienen una factura asociada y están PENDIENTES:");
            viajesMap.values().forEach(viajePendiente -> {
                System.out.println(" - Viaje ID: " + viajePendiente.getIdViaje() + ", Origen: " + viajePendiente.getOrigen() + ", Destino: " + viajePendiente.getDestino());
                // Si necesitas que estos viajes pendientes también se muestren en la previsualización
                // en una lista separada o como FacturaConsolidadaDTOs con estado "PENDIENTE",
                // podrías añadirlos a 'facturasConsolidadas' o a una nueva lista en el response DTO.
                // Por ejemplo, creamos un DTO para ellos con estado PENDIENTE:
               Compania compania = getNombreCompania(viajePendiente);
               String nombreCompania = null;
               Long idCompania = null;
               float porcentaje = 0;
               if (compania != null) {
                   nombreCompania = compania.getNombre();
                   idCompania = compania.getId();
                   porcentaje = compania.getPorcentaje();
               }
               String estado = "PENDIENTE";
               if (!viajePendiente.getEstado().equals("Completed")) {
                   estado = viajePendiente.getEstado();
               }
                facturasConsolidadas.add(new FacturaConsolidadaDTO(
                        null, // ID (será asignado al guardar en DB)
                        idLoteGenerado,
                        viajePendiente.getIdViaje(),
                        viajePendiente.getDriverName(),
                        viajePendiente.getOrigen(),
                        viajePendiente.getFechaOrigen(),
                        viajePendiente.getDestino(),
                        viajePendiente.getFechaDestino(),
                        null, // No hay ContractId de factura asociada
                        BigDecimal.ZERO, // O un valor por defecto para viajes pendientes
                        estado, // <-- CAMBIO CLAVE: Viaje sin factura = PENDIENTE
                        idCompania,
                        nombreCompania,
                        porcentaje
                ));
            });
        }


        // Procesar Capacities
        for (CapacityAgrupadaDTO capacity : capacities) {
            totalCapacities = totalCapacities.add(capacity.getValorAPagar());
        }


        if (facturasConsolidadas != null && !facturasConsolidadas.isEmpty()) {
            // --- IMPLEMENTACIÓN DEL COMPARADOR PERSONALIZADO EXPLÍCITO ---
            facturasConsolidadas.sort(new Comparator<FacturaConsolidadaDTO>() {
                @Override
                public int compare(FacturaConsolidadaDTO f1, FacturaConsolidadaDTO f2) {
                    // Paso 1: Comparar por idCompania, manejando nulos y case-insensitivity
                    Long idCompania1 = f1.getIdCompania();
                    Long idCompania2 = f2.getIdCompania();

                    // Manejo de nulos para idCompania (nullsFirst: null va antes que no-null)
                    if (idCompania1 == null && idCompania2 != null) {
                        return -1; // f1 (null) antes que f2 (no-null)
                    }
                    if (idCompania1 != null && idCompania2 == null) {
                        return 1;  // f1 (no-null) después de f2 (null)
                    }
                    // Si ambos son null o ambos no son null, comparamos sus valores.
                    // Si ambos son null, companyCompare será 0, y se pasará a driverName.
                    int companyCompare = (idCompania1 == null && idCompania2 == null) ? 0 : idCompania1.compareTo(idCompania2);

                    if (companyCompare != 0) {
                        return companyCompare; // Si idCompania es diferente, ese es el orden
                    }

                    // Paso 2: Si idCompania es igual (o ambos nulos), comparar por driverName, manejando nulos
                    String driverName1 = f1.getDriverName();
                    String driverName2 = f2.getDriverName();

                    // Manejo de nulos para driverName (nullsFirst: null va antes que no-null)
                    if (driverName1 == null && driverName2 != null) {
                        return -1; // f1 (null) antes que f2 (no-null)
                    }
                    if (driverName1 != null && driverName2 == null) {
                        return 1;  // f1 (no-null) después de f2 (null)
                    }
                    // Si ambos son null o ambos no son null, comparamos sus valores.
                    return (driverName1 == null && driverName2 == null) ? 0 : driverName1.compareToIgnoreCase(driverName2);
                }
            });
        }

        return new FacturacionConsolidadaResponse(
                facturasConsolidadas, // Esta lista ahora contendrá tanto los PAGADOS como los PENDIENTES (viajes sin factura)
                totalFacturasConsolidadas,
                capacities,
                totalCapacities,
                idLoteGenerado,
                facturasSinViajeAsociado // Estas son las facturas que no encontraron un viaje
        );
    }

    @Transactional
    public FacturacionConsolidadaResponse guardarFacturacion(FacturacionConsolidadaResponse consolidadoResponse) {
        if (consolidadoResponse.getIdLoteFacturacion() == null) {
            throw new IllegalArgumentException("El ID del lote de facturación es requerido para guardar.");
        }

        LoteFacturacion loteExistente = loteFacturacionRepository.findById(consolidadoResponse.getIdLoteFacturacion())
                .orElseThrow(() -> new RuntimeException("Lote de facturación no encontrado con ID: " + consolidadoResponse.getIdLoteFacturacion()));

        List<Factura> facturas = new ArrayList<>();
        for (FacturaConsolidadaDTO facturaConsolidadaDTO : consolidadoResponse.getFacturasConsolidadas()) {
            Factura entity = new Factura();
            Optional<Factura> facturaBD = facturaRepository.findByIdViaje(facturaConsolidadaDTO.getIdViaje());
            if (facturaBD.isEmpty() || !facturaBD.get().getEstado().contains("PAGADO")) {
                if (facturaBD.isPresent() && !facturaBD.get().getEstado().contains("PAGADO")) {
                    entity.setId(facturaBD.get().getId());;
                }
                entity.setIdLoteFacturacion(loteExistente.getId());
                entity.setIdViaje(facturaConsolidadaDTO.getIdViaje());
                entity.setDriverName(facturaConsolidadaDTO.getDriverName());
                entity.setOrigen(facturaConsolidadaDTO.getOrigen());
                entity.setFechaOrigen(facturaConsolidadaDTO.getFechaOrigen());
                entity.setDestino(facturaConsolidadaDTO.getDestino());
                entity.setFechaDestino(facturaConsolidadaDTO.getFechaDestino());
                entity.setContractId(facturaConsolidadaDTO.getContractId());
                entity.setPay(facturaConsolidadaDTO.getPay());
                entity.setEstado(facturaConsolidadaDTO.getEstado());
                entity.setIdCompania(facturaConsolidadaDTO.getIdCompania());
                entity.setNombreCompania(facturaConsolidadaDTO.getNombreCompania());
                facturas.add(entity);
            }

        }

        facturaRepository.saveAll(facturas);

        // Devolver la misma respuesta que se recibió. Esto es lo que el controlador espera.
        return consolidadoResponse;
    }
    private Compania getNombreCompania (ViajeAgrupadoDTO viaje) {
        Optional<Conductor> conductor = conductorRepository.findByNombre(viaje.getDriverName());
        if (conductor.isPresent()) {
            List<ConductorCompania> conductorCompanias = conductorCompaniaRepository
                    .findByIdConductorAndEstadoOrderByFechaAsociacionDesc(conductor.get().getId(),
                            "Activo");
            if (!conductorCompanias.isEmpty()) {
                Optional<Compania> compania = companiaRepository.findById(conductorCompanias.get(0).getIdCompania());
                if (compania.isPresent()) {
                    return compania.get();
                }
            }

        }
        return null;
    }

    private List<Factura> getFacturasPendientes() {
        return facturaRepository.findByEstado("PENDIENTE");
    }

    @Transactional(readOnly = true)
    public Optional<LoteFacturacion> findLoteFacturacionById(Long id) {
        return loteFacturacionRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Factura> findFacturasByLoteFacturacionId(Long loteFacturacionId) {
        return facturaRepository.findByIdLoteFacturacion(loteFacturacionId);
    }

    /**
     * Genera un archivo Excel con las facturas de un lote específico.
     *
     * @param idLoteFacturacion El ID del lote de facturación.
     * @return Un arreglo de bytes que representa el archivo Excel.
     * @throws java.io.IOException Si ocurre un error al generar el Excel.
     */
    public byte[] generarExcelFacturas(Long idLoteFacturacion) throws IOException {
        // 1. Recuperar las facturas de la base de datos
        List<Factura> facturas = facturaRepository.findByIdLoteFacturacion(idLoteFacturacion);

        if (facturas.isEmpty()) {
            throw new IllegalArgumentException("No se encontraron facturas para el lote ID: " + idLoteFacturacion);
        }

        // 2. Crear el libro de Excel y la hoja
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Facturas Lote " + idLoteFacturacion);

            // Formato para fechas
            CreationHelper createHelper = workbook.getCreationHelper();
            CellStyle dateCellStyle = workbook.createCellStyle();
            dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-MM-dd HH:mm"));

            // 3. Crear la fila de encabezados
            String[] headers = {
                    "ID Lote", "ID Viaje", "Conductor", "Compañía ID", "Compañía Nombre",
                    "Origen", "Fecha Origen", "Destino", "Fecha Destino",
                    "ID Contrato", "Valor a Pagar", "Estado"
            };
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // 4. Llenar los datos
            int rowNum = 1;
            for (Factura factura : facturas) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(factura.getIdLoteFacturacion());
                row.createCell(1).setCellValue(factura.getIdViaje());
                row.createCell(2).setCellValue(factura.getDriverName());
                row.createCell(3).setCellValue(factura.getIdCompania());
                row.createCell(4).setCellValue(factura.getNombreCompania());
                row.createCell(5).setCellValue(factura.getOrigen());

                Cell fechaOrigenCell = row.createCell(6);
                if (factura.getFechaOrigen() != null) {
                    fechaOrigenCell.setCellValue(factura.getFechaOrigen());
                    fechaOrigenCell.setCellStyle(dateCellStyle);
                } else {
                    fechaOrigenCell.setCellValue("");
                }

                row.createCell(7).setCellValue(factura.getDestino());

                Cell fechaDestinoCell = row.createCell(8);
                if (factura.getFechaDestino() != null) {
                    fechaDestinoCell.setCellValue(factura.getFechaDestino());
                    fechaDestinoCell.setCellStyle(dateCellStyle);
                } else {
                    fechaDestinoCell.setCellValue("");
                }

                row.createCell(9).setCellValue(factura.getContractId());
                row.createCell(10).setCellValue(factura.getPay().doubleValue()); // Convertir BigDecimal a double
                row.createCell(11).setCellValue(factura.getEstado());
            }

            // Autoajustar el tamaño de las columnas (opcional, puede ser costoso para muchas filas)
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // 5. Escribir el contenido del workbook a un ByteArrayOutputStream
            workbook.write(out);
            return out.toByteArray();
        }
    }

    /**
     * Genera un archivo Excel con las capacities de un lote específico.
     *
     * @param idLoteFacturacion El ID del lote de facturación.
     * @return Un arreglo de bytes que representa el archivo Excel.
     * @throws IOException Si ocurre un error al generar el Excel.
     */
    public byte[] generarExcelCapacities(Long idLoteFacturacion) throws IOException {
        // 1. Recuperar las capacities de la base de datos
        List<Capacity> capacities = capacityRepository.findByIdLoteFacturacion(idLoteFacturacion);

        if (capacities.isEmpty()) {
            throw new IllegalArgumentException("No se encontraron capacities para el lote ID: " + idLoteFacturacion);
        }

        // 2. Crear el libro de Excel y la hoja
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Capacities Lote " + idLoteFacturacion);

            // 3. Crear la fila de encabezados
            String[] headers = {
                    "ID Lote", "Contract id", "Valor a Pagar"
            };
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // 4. Llenar los datos
            int rowNum = 1;
            for (Capacity capacity : capacities) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(capacity.getIdLoteFacturacion());
                row.createCell(1).setCellValue(capacity.getContractId());
                row.createCell(2).setCellValue(capacity.getValorAPagar().doubleValue());
            }

            // Autoajustar el tamaño de las columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // 5. Escribir el contenido del workbook a un ByteArrayOutputStream
            workbook.write(out);
            return out.toByteArray();
        }
    }
    @Transactional(readOnly = true)
    public List<Capacity> findCapacitiesByLoteFacturacionId(Long loteFacturacionId) {
        return capacityRepository.findByIdLoteFacturacion(loteFacturacionId);
    }
    /**
     * Recupera todos los lotes de facturación existentes.
     * @return Una lista de objetos LoteFacturacion.
     */
    @Transactional(readOnly = true)
    public List<LoteFacturacion> findAllLotesFacturacion() {
        return loteFacturacionRepository.findAll();
    }

    /**
     * Reconstruye y devuelve la FacturacionConsolidadaResponse completa para un lote dado.
     * Esto recupera las facturas y capacities de la DB asociadas a ese lote.
     * @param idLoteFacturacion El ID del lote a reconstruir.
     * @return FacturacionConsolidadaResponse con los datos del lote.
     * @throws IllegalArgumentException si el lote no se encuentra.
     */
    @Transactional(readOnly = true)
    public FacturacionConsolidadaResponse getConsolidatedResponseForLote(Long idLoteFacturacion) {
        LoteFacturacion lote = loteFacturacionRepository.findById(idLoteFacturacion)
                .orElseThrow(() -> new IllegalArgumentException("Lote de facturación no encontrado con ID: "
                        + idLoteFacturacion));

        // Obtener las facturas asociadas a este lote
        List<Factura> facturas = facturaRepository.findByIdLoteFacturacion(idLoteFacturacion);
        float porcentaje = 0;
        BigDecimal totalFacturasConsolidadas = new BigDecimal(0);
        List<FacturaConsolidadaDTO> facturasConsolidadasDTO = new ArrayList<>();
        for (Factura factura : facturas) {
            Optional<Compania> compania = companiaRepository.findById(factura.getIdCompania());
            if (compania.isPresent()) {
                porcentaje = compania.get().getPorcentaje();
            }

            facturasConsolidadasDTO.add(new FacturaConsolidadaDTO(
                    factura.getId(),
                    factura.getIdLoteFacturacion(),
                    factura.getIdViaje(),
                    factura.getDriverName(),
                    factura.getOrigen(),
                    factura.getFechaOrigen(),
                    factura.getDestino(),
                    factura.getFechaDestino(),
                    factura.getContractId(),
                    factura.getPay(),
                    factura.getEstado(),
                    factura.getIdCompania(),
                    factura.getNombreCompania(),
                    porcentaje
                    ));

            BigDecimal porcentajeBigDecimal = new BigDecimal(String.valueOf(porcentaje));

            BigDecimal CIEN = new BigDecimal("100");
            BigDecimal factorDescuento = porcentajeBigDecimal.divide(CIEN, MathContext.DECIMAL64);

            BigDecimal montoDescontado = factura.getPay().multiply(factorDescuento);

            BigDecimal valorFinalFactura = factura.getPay().subtract(montoDescontado);
            totalFacturasConsolidadas = totalFacturasConsolidadas.add(valorFinalFactura);
        }


        // Obtener las capacities asociadas a este lote
        List<Capacity> capacities = capacityRepository.findByIdLoteFacturacion(idLoteFacturacion);
        List<CapacityAgrupadaDTO> capacitiesDTO = new ArrayList<>();

        for (Capacity capacity : capacities) {
            List<Factura> facturaCapacity = getFacturasByContractid(capacity.getContractId());
            List<CapacityDTO> capacitiesCapacityDTO  = new ArrayList<>();
            for (Factura factura : facturaCapacity) {
                String companyName = "";
                Optional<Compania> compania = companiaService.buscarCompaniaPorId(factura.getIdCompania());
                if (compania.isPresent()) {
                    companyName = compania.get().getNombre();
                }
                capacitiesCapacityDTO.add(new CapacityDTO(factura.getContractId(), factura.getIdViaje(),
                        factura.getDriverName(), companyName));

            }
            capacitiesDTO.add(new CapacityAgrupadaDTO(
                            "",
                            capacity.getContractId(),
                            capacity.getValorAPagar(),
                            capacitiesCapacityDTO));
        }

        BigDecimal totalCapacities = capacitiesDTO.stream()
                .map(CapacityAgrupadaDTO::getValorAPagar)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Retornamos una nueva FacturacionConsolidadaResponse con los datos recuperados
        return new FacturacionConsolidadaResponse(
                facturasConsolidadasDTO,
                totalFacturasConsolidadas,
                capacitiesDTO,
                totalCapacities,
                lote.getId(),
                new ArrayList<>()
        );
    }

    public List<Factura> getFacturasByContractid(String contractid) {
        System.out.println("contact id " + contractid);
        return facturaRepository.findByContractId(contractid);

    }

}