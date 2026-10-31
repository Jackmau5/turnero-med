package com.dr_commerce.e_commerce.vi.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private ResponseEntity<Map<String, Object>> error(int status, String mensaje) {
        return ResponseEntity.status(status).body(Map.of("status", status, "mensaje", mensaje));
    }
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<?> noEncontrado(NotFoundException e) { return error(404, e.getMessage()); }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> invalido(IllegalArgumentException e) { return error(400, e.getMessage()); }
    @ExceptionHandler(ConflictoException.class)
    public ResponseEntity<?> conflicto(ConflictoException e) { return error(409, e.getMessage()); }
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<?> autenticacion(AuthenticationException e) { return error(401, "Email o contraseña incorrectos"); }
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> prohibido(AccessDeniedException e) { return error(403, "No tenés permiso para realizar esta acción"); }
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class})
    public ResponseEntity<?> formato(Exception e) { return error(400, "Revisá el JSON, los parámetros, las fechas y los valores enviados"); }
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> integridad(Exception e) { return error(409, "Hay datos duplicados o registros relacionados que impiden la operación"); }
    @ExceptionHandler(ConcurrencyFailureException.class)
    public ResponseEntity<?> concurrencia(Exception e) { return error(409, "El registro cambió mientras operabas. Actualizá la pantalla y volvé a intentar"); }
}
