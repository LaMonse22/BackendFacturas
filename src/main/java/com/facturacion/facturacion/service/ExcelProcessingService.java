package com.facturacion.facturacion.service;


import com.facturacion.facturacion.dto.CapacityAgrupadaDTO;
import com.facturacion.facturacion.dto.FacturaAgrupadaDTO;
import com.facturacion.facturacion.dto.PrevisualizacionInicialDTO;
import com.facturacion.facturacion.dto.ViajeAgrupadoDTO;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExcelProcessingService {
    private static final DateTimeFormatter MM_DD_YYYY_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");


    /**
     * Procesa un archivo Excel general, lee sus filas y las convierte en una lista de mapas.
     * Cada mapa representa una fila, con claves correspondientes a los encabezados de las columnas.
     */
    public List<Map<String, String>> processExcelFile(MultipartFile file) throws IOException {
        List<Map<String, String>> data = new ArrayList<>();
        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);

            Row headerRow = sheet.getRow(0);
            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                headers.add(cell.getStringCellValue().trim().toLowerCase());
            }

            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                Map<String, String> rowData = new LinkedHashMap<>();
                for (int c = 0; c < headers.size(); c++) {
                    Cell cell = row.getCell(c);
                    String cellValue = getCellValueAsString(cell);
                    rowData.put(headers.get(c), cellValue);
                }
                data.add(rowData);
            }
        }
        return data;
    }

    /**
     * Procesa el archivo Excel de facturas, extrayendo las facturas agrupadas y las capacities.
     * La lógica para identificar 'capacities' se basa en la ausencia de 'Block Id', 'Trip Id' o 'Load Id'.
     */
    public PrevisualizacionInicialDTO processFacturasExcelFile(MultipartFile file) throws IOException {
        List<FacturaAgrupadaDTO> facturasAgrupadas = new ArrayList<>();
        List<CapacityAgrupadaDTO> capacities = new ArrayList<>();
        Map<String, BigDecimal> facturasGrossPayMap = new LinkedHashMap<>();
        Map<String, String> facturasContractIdMap = new LinkedHashMap<>();

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);

            Row headerRow = sheet.getRow(0);
            Map<String, Integer> headerIndexMap = new LinkedHashMap<>();
            for (Cell cell : headerRow) {
                // Normaliza el encabezado para la búsqueda (minúsculas y sin saltos de línea)
                headerIndexMap.put(cell.getStringCellValue().trim().toLowerCase().replace("\n", ""),
                        cell.getColumnIndex());
            }

            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                // Intenta obtener los IDs de viaje. Usa el nombre de la columna normalizado.
                String blockId = getCellValueAsString(row.getCell(headerIndexMap.get("block id")));
                String tripId = getCellValueAsString(row.getCell(headerIndexMap.get("trip id"))); // Asumiendo que también podría existir un 'Trip Id'
                String loadId = getCellValueAsString(row.getCell(headerIndexMap.get("load id"))); // Prioridad más baja para Load Id en esta lógica si BlockId/TripId existen.

                // Determinar si es una Capacity: Si BlockId, TripId y LoadId están vacíos o nulos
                boolean isCapacity = (blockId == null || blockId.trim().isEmpty()) &&
                        (tripId == null || tripId.trim().isEmpty()) &&
                        (loadId == null || loadId.trim().isEmpty());

                // Obtener Contract ID y Gross Pay, independientemente de si es factura o capacity
                String contractId = getCellValueAsString(row.getCell(headerIndexMap.get("contract id")));
                String grossPayStr = getCellValueAsString(row.getCell(headerIndexMap.get("gross pay"))); // Normaliza también para Gross Pay


                BigDecimal grossPay = BigDecimal.ZERO;
                if (grossPayStr != null && !grossPayStr.trim().isEmpty()) {
                    try {
                        grossPay = new BigDecimal(grossPayStr.replace(",", ""));
                    } catch (NumberFormatException e) {
                        System.err.println("Advertencia: No se pudo parsear grossPay '"
                                + grossPayStr + "' en la fila " + (r + 1));
                    }
                }

                if (isCapacity) {
                    // Si es una 'capacity', agrégala directamente a la lista de capacities
                    // Una capacity aún necesita un ID de viaje para el DTO, aunque sea nulo o vacío
                    // Usamos el contractId como identificador si no hay LoadId definido explícitamente en el Excel para Capacities.
                    // O si se prefiere, se pasa un null o cadena vacía si no tiene un LoadId de viaje asociado.
                    capacities.add(new CapacityAgrupadaDTO(loadId, contractId, grossPay));
                } else {
                    // Si es una 'factura' (tiene algún ID de viaje), agrúpala
                    // Prioriza Block Id, luego Trip Id, luego Load Id
                    String actualIdViaje = (blockId != null && !blockId.trim().isEmpty()) ? blockId :
                            (tripId != null && !tripId.trim().isEmpty()) ? tripId :
                                    loadId; // Asegúrate de que este 'loadId' sea el que viene del Excel para facturas.

                    if (actualIdViaje != null && !actualIdViaje.trim().isEmpty()) { // Solo procesar si se encontró un ID de viaje válido
                        BigDecimal finalGrossPay = grossPay;
                        facturasGrossPayMap.compute(actualIdViaje, (key, existingValue) -> {
                            if (existingValue == null) {
                                return finalGrossPay;
                            } else {
                                return existingValue.add(finalGrossPay);
                            }
                        });
                        facturasContractIdMap.put(actualIdViaje, contractId);
                    } else {
                        System.err.println("Advertencia: Fila " + (r + 1)
                                +  " no es capacity y no tiene Block Id, Trip Id, ni Load Id para ser factura."
                                + " Se omitirá.");
                    }
                }
            }

            // Convertir el mapa de facturas agrupadas a una lista de FacturaAgrupadaDTO
            for (Map.Entry<String, BigDecimal> entry : facturasGrossPayMap.entrySet()) {
                facturasAgrupadas.add(new FacturaAgrupadaDTO(entry.getKey(), facturasContractIdMap.get(entry.getKey())
                        , entry.getValue()));
            }

        }
        return new PrevisualizacionInicialDTO(facturasAgrupadas, capacities);
    }

    /**
     * Agrupa los datos de viajes leídos del Excel para consolidar las paradas de origen y destino,
     * seleccionando las fechas más tempranas para el origen y más tardías para el destino.
     */
    public List<ViajeAgrupadoDTO> agruparViajes(List<Map<String, String>> viajesData) {
        Map<String, ViajeAgrupadoDTO> viajesMap = new LinkedHashMap<>();

        for (Map<String, String> row : viajesData) {
            String blockId = row.get("block id");
            String tripId = row.get("trip id");
            String idViaje = (blockId != null && !blockId.trim().isEmpty()) ? blockId : tripId;

            if (idViaje == null || idViaje.trim().isEmpty()) {
                continue;
            }

            String driverName = row.get("driver name");
            String stop1 = row.get("stop 1");
            LocalDateTime fechaStop1 = parseDate(row.get("stop 1 planned arrival date"));
            String stop2 = row.get("stop 2");
            LocalDateTime fechaStop2 = parseDate(row.get("stop 2 planned arrival date"));

            String estadoViaje = row.get("trip stage");

            viajesMap.compute(idViaje, (key, existingViaje) -> {
                if (existingViaje == null) {
                    return new ViajeAgrupadoDTO(idViaje, driverName, stop1, fechaStop1, stop2, fechaStop2, estadoViaje);
                } else {
                    existingViaje.setDestino(stop2);
                    if (fechaStop2 != null && (existingViaje.getFechaDestino() == null || fechaStop2.isAfter(existingViaje.getFechaDestino()))) {
                        existingViaje.setFechaDestino(fechaStop2);
                    }
                    if (fechaStop1 != null && (existingViaje.getFechaOrigen() == null || fechaStop1.isBefore(existingViaje.getFechaOrigen()))) {
                        existingViaje.setOrigen(stop1);
                        existingViaje.setFechaOrigen(fechaStop1);
                    }
                    return existingViaje;
                }
            });
        }
        return new ArrayList<>(viajesMap.values());
    }

    /**
     * Método auxiliar para obtener el valor de una celda como String, manejando diferentes tipos de celda.
     * También convierte números de Excel que representan fechas en cadenas legibles.
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toString();
                } else {
                    return BigDecimal.valueOf(cell.getNumericCellValue()).stripTrailingZeros().toPlainString();
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return String.valueOf(cell.getNumericCellValue());
            case BLANK:
                return "";
            default:
                return "";
        }
    }

    /**
     * Método auxiliar para parsear un String que representa una fecha de Excel a un LocalDateTime.
     * Maneja valores nulos o vacíos.
     */
    private LocalDateTime parseDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }
        try {
            // Intentar parsear con el formato MM/DD/AAAA
            // Se añade .atStartOfDay() para convertir LocalDate a LocalDateTime (a las 00:00:00)
            // ya que los archivos Excel a menudo no tienen información de hora para las fechas.
            return LocalDate.parse(dateString, MM_DD_YYYY_FORMATTER).atStartOfDay();
        } catch (java.time.format.DateTimeParseException e1) {
            // Si falla el formato MM/DD/AAAA, intentar con el formato ISO (por si acaso o para depuración)
            try {
                return LocalDateTime.parse(dateString);
            } catch (java.time.format.DateTimeParseException e2) {
                System.err.println("Error al parsear fecha: '" + dateString
                        + "' no coincide con 'MM/dd/yyyy' ni con el formato ISO. Mensaje: " + e2.getMessage());
                return null;
            }
        }
    }
}