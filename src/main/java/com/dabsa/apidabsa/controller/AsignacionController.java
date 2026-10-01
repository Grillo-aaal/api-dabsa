package com.dabsa.apidabsa.controller;

import com.dabsa.apidabsa.model.Asignacion;
import com.dabsa.apidabsa.service.AsignacionArchivoService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/asignaciones")
public class AsignacionController {

    private final AsignacionArchivoService archivoService;

    @Value("${api.token}")
    private String apiToken;

    public AsignacionController(
            AsignacionArchivoService archivoService) {
        this.archivoService = archivoService;
    }

    @PostMapping
    public ResponseEntity<?> recibirAsignacion(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorization,

            @RequestBody Asignacion asignacion) {

        // ==========================================
        // VALIDAR TOKEN
        // ==========================================

        if (authorization == null
                || !authorization.startsWith("Bearer ")) {

            Map<String, Object> respuesta = new HashMap<>();

            respuesta.put("status", "error");
            respuesta.put(
                    "message",
                    "Token de autenticación requerido."
            );

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(respuesta);
        }

        String tokenRecibido =
                authorization.substring(7);

        if (!tokenRecibido.equals(apiToken)) {

            Map<String, Object> respuesta = new HashMap<>();

            respuesta.put("status", "error");
            respuesta.put(
                    "message",
                    "Token de autenticación inválido."
            );

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(respuesta);
        }

        // ==========================================
        // VALIDAR ASIGNACIÓN
        // ==========================================

        String error = validarAsignacion(asignacion);

        if (error != null) {

            Map<String, Object> respuesta =
                    new HashMap<>();

            respuesta.put("status", "error");
            respuesta.put("message", error);

            return ResponseEntity
                    .status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(respuesta);
        }

        // ==========================================
        // PROCESAR ASIGNACIÓN
        // ==========================================

        System.out.println(
                "================================="
        );

        System.out.println(
                "ASIGNACIÓN RECIBIDA"
        );

        System.out.println(
                "Cliente: "
                + asignacion.getCliente().getNombre()
        );

        if (asignacion.getContrato() != null) {

            System.out.println(
                    "Contratos: "
                    + asignacion.getContrato().size()
            );
        }

        if (asignacion.getTelefono() != null) {

            System.out.println(
                    "Teléfonos: "
                    + asignacion.getTelefono().size()
            );
        }

        if (asignacion.getDireccion() != null) {

            System.out.println(
                    "Direcciones: "
                    + asignacion.getDireccion().size()
            );
        }

        // ==========================================
        // GUARDAR JSON
        // ==========================================

        String rutaArchivo =
                archivoService.guardarAsignacion(asignacion);

        System.out.println(
                "Archivo guardado en: "
                + rutaArchivo
        );

        System.out.println(
                "================================="
        );

        // ==========================================
        // RESPUESTA
        // ==========================================

        Map<String, Object> respuesta =
                new HashMap<>();

        respuesta.put(
                "status",
                "success"
        );

        respuesta.put(
                "message",
                "Asignación recibida correctamente."
        );

        respuesta.put(
                "cliente",
                asignacion.getCliente().getNumero()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(respuesta);
    }

    // ==========================================
    // VALIDACIÓN
    // ==========================================

    private String validarAsignacion(
            Asignacion asignacion) {

        if (asignacion == null) {

            return "La asignación no puede ser nula.";
        }

        if (asignacion.getCliente() == null) {

            return "Los datos del cliente son obligatorios.";
        }

        if (asignacion.getCliente().getNumero() == null
                || asignacion.getCliente()
                        .getNumero()
                        .trim()
                        .isEmpty()) {

            return "El número de cliente es obligatorio.";
        }

        if (asignacion.getCliente().getNombre() == null
                || asignacion.getCliente()
                        .getNombre()
                        .trim()
                        .isEmpty()) {

            return "El nombre del cliente es obligatorio.";
        }

        if (asignacion.getCliente().getCurp() == null
                || asignacion.getCliente()
                        .getCurp()
                        .trim()
                        .isEmpty()) {

            return "La CURP es obligatoria.";
        }

        return null;
    }
}