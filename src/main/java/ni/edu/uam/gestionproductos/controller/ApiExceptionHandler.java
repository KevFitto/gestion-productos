package ni.edu.uam.gestionproductos.controller;

import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> conflicto(DataIntegrityViolationException exception) {
        return Map.of("mensaje", "Los datos entran en conflicto con un registro existente. Verifique el código del producto y sus relaciones.");
    }
}

