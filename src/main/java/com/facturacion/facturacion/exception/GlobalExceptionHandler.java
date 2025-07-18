package com.facturacion.facturacion.exception;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Maneja errores de validación de @Valid en @RequestBody o @ModelAttribute
    @ExceptionHandler(MethodArgumentNotValidException.class)
    // @ResponseStatus(HttpStatus.BAD_REQUEST) // <-- ELIMINAR ESTA ANOTACIÓN
    public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST); // <-- HTTP Status definido aquí
    }

    // Maneja errores para recursos no encontrados (ej. findById().orElseThrow() con NoSuchElementException)
    @ExceptionHandler({IllegalArgumentException.class, NoSuchElementException.class})
    // @ResponseStatus(HttpStatus.NOT_FOUND) // <-- ELIMINAR ESTA ANOTACIÓN
    public ResponseEntity<String> handleNotFoundExceptions(RuntimeException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND); // <-- HTTP Status definido aquí
    }

    // Maneja errores de lectura de archivos o problemas de I/O
    @ExceptionHandler(IOException.class)
    // @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // <-- ELIMINAR ESTA ANOTACIÓN
    public ResponseEntity<String> handleIOException(IOException ex) {
        return new ResponseEntity<>("Error de entrada/salida: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR); // <-- HTTP Status definido aquí
    }

    // Maneja errores cuando el tipo de argumento en un path variable o request param no coincide
    @ExceptionHandler(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class)
    // @ResponseStatus(HttpStatus.BAD_REQUEST) // <-- ELIMINAR ESTA ANOTACIÓN
    public ResponseEntity<String> handleTypeMismatch(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException ex) {
        String name = ex.getName();
        String requiredType = Objects.requireNonNull(ex.getRequiredType()).getSimpleName();
        Object value = ex.getValue();
        String errorMessage = String.format("Parámetro '%s' con valor '%s' no puede ser convertido a tipo '%s'.",
                name, value, requiredType);
        return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST); // <-- HTTP Status definido aquí
    }

    // Captura errores cuando el JSON de entrada no es válido o está mal formado
    @ExceptionHandler(HttpMessageNotReadableException.class)
    // @ResponseStatus(HttpStatus.BAD_REQUEST) // <-- ELIMINAR ESTA ANOTACIÓN
    public ResponseEntity<String> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        String errorMessage = "Cuerpo de la solicitud JSON mal formado o ilegible. Verifique la sintaxis JSON.";
        if (ex.getCause() != null) {
            errorMessage += " Causa: " + ex.getCause().getMessage();
        }
        return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST); // <-- HTTP Status definido aquí
    }

    // Manejador genérico para cualquier otra excepción no capturada
    @ExceptionHandler(Exception.class)
    // @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // <-- ELIMINAR ESTA ANOTACIÓN
    public ResponseEntity<String> handleAllUncaughtExceptions(Exception ex) {
        return new ResponseEntity<>("Ocurrió un error inesperado: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR); // <-- HTTP Status definido aquí
    }
}