package com.dabsa.apidabsa.controller;

import com.dabsa.apidabsa.model.Asignacion;
import com.dabsa.apidabsa.service.AsignacionArchivoService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/asignaciones")
public class AsignacionController {

    private final AsignacionArchivoService archivoService;

    @Value("${api.token}")
    private String apiToken;

    public AsignacionController(AsignacionArchivoService archivoService) {
        this.archivoService = archivoService;
    }

    // =========================================================
    // RECIBIR ASIGNACIONES DE FORMA MASIVA
    // =========================================================

    @PostMapping
    public ResponseEntity<?> recibirAsignaciones(
            @RequestHeader(value = "Authorization", required = false)
            String authorization,

            @RequestBody List<Asignacion> asignaciones) {

        // =====================================================
        // 1. VALIDAR TOKEN
        // =====================================================

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

        String tokenRecibido = authorization.substring(7);

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

        // =====================================================
        // 2. VALIDAR QUE LA LISTA NO ESTÉ VACÍA
        // =====================================================

        if (asignaciones == null || asignaciones.isEmpty()) {

            Map<String, Object> respuesta = new HashMap<>();

            respuesta.put("status", "error");
            respuesta.put(
                    "message",
                    "La lista de registros no puede estar vacía."
            );

            respuesta.put("total_recibidos", 0);
            respuesta.put("total_exitosos", 0);
            respuesta.put("total_fallidos", 0);

            return ResponseEntity
                    .status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(respuesta);
        }

        // =====================================================
        // 3. PREPARAR LISTAS DE RESULTADOS
        // =====================================================

        List<Asignacion> exitosos = new ArrayList<>();
        List<Map<String, Object>> errores = new ArrayList<>();

        // =====================================================
        // 4. PROCESAR CADA REGISTRO
        // =====================================================

        for (int i = 0; i < asignaciones.size(); i++) {

            Asignacion asignacion = asignaciones.get(i);

            String error = validarAsignacion(asignacion);

            // -------------------------------------------------
            // REGISTRO CORRECTO
            // -------------------------------------------------

            if (error == null) {

                exitosos.add(asignacion);

                System.out.println(
                        "Cliente procesado: "
                                + asignacion
                                    .getCliente()
                                    .getNumero()
                                + " - "
                                + asignacion
                                    .getCliente()
                                    .getNombre()
                );

            }

            // -------------------------------------------------
            // REGISTRO INCORRECTO
            // -------------------------------------------------

            else {

                Map<String, Object> errorRegistro =
                        new HashMap<>();

                errorRegistro.put(
                        "registro",
                        i + 1
                );

                // Intentamos incluir el número de cliente
                if (asignacion != null
                        && asignacion.getCliente() != null) {

                    errorRegistro.put(
                            "cliente",
                            asignacion
                                    .getCliente()
                                    .getNumero()
                    );
                }

                errorRegistro.put(
                        "error",
                        error
                );

                errores.add(errorRegistro);
            }
        }

        // =====================================================
        // 5. CALCULAR TOTALES
        // =====================================================

        int totalRecibidos = asignaciones.size();
        int totalExitosos = exitosos.size();
        int totalFallidos = errores.size();

        // =====================================================
        // 6. SI TODOS LOS REGISTROS FALLARON
        // =====================================================

        if (totalExitosos == 0) {

            Map<String, Object> respuesta = new HashMap<>();

            respuesta.put("status", "error");

            respuesta.put(
                    "message",
                    "Todos los registros enviados fallaron "
                            + "en el procesamiento."
            );

            respuesta.put(
                    "total_recibidos",
                    totalRecibidos
            );

            respuesta.put(
                    "total_exitosos",
                    0
            );

            respuesta.put(
                    "total_fallidos",
                    totalFallidos
            );

            respuesta.put(
                    "errores",
                    errores
            );

            return ResponseEntity
                    .status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(respuesta);
        }

        // =====================================================
        // 7. GUARDAR ÚNICAMENTE LOS REGISTROS CORRECTOS
        // =====================================================

        String rutaArchivo =
                archivoService.guardarAsignaciones(exitosos);

        System.out.println(
                "Archivo guardado en: " + rutaArchivo
        );

        // =====================================================
        // 8. SI ALGUNOS REGISTROS FALLARON
        // =====================================================

        if (totalFallidos > 0) {

            Map<String, Object> respuesta = new HashMap<>();

            respuesta.put(
                    "status",
                    "partial_success"
            );

            respuesta.put(
                    "message",
                    "Algunos registros se procesaron "
                            + "correctamente y otros fallaron."
            );

            respuesta.put(
                    "total_recibidos",
                    totalRecibidos
            );

            respuesta.put(
                    "total_exitosos",
                    totalExitosos
            );

            respuesta.put(
                    "total_fallidos",
                    totalFallidos
            );

            respuesta.put(
                    "errores",
                    errores
            );

            return ResponseEntity
                    .status(HttpStatus.MULTI_STATUS)
                    .body(respuesta);
        }

        // =====================================================
        // 9. TODOS LOS REGISTROS FUERON CORRECTOS
        // =====================================================

        Map<String, Object> respuesta = new HashMap<>();

        respuesta.put(
                "status",
                "success"
        );

        respuesta.put(
                "message",
                "Procesamiento masivo terminado con éxito."
        );

        respuesta.put(
                "total_recibidos",
                totalRecibidos
        );

        respuesta.put(
                "total_exitosos",
                totalExitosos
        );

        respuesta.put(
                "total_fallidos",
                0
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(respuesta);
    }

    // =========================================================
    // VALIDAR CADA ASIGNACIÓN
    // =========================================================

    private String validarAsignacion(Asignacion asignacion) {

        if (asignacion == null) {

            return "El registro no puede ser nulo.";
        }

        if (asignacion.getCliente() == null) {

            return "Los datos del cliente son obligatorios.";
        }

        // =====================================================
        // VALIDAR NÚMERO DE CLIENTE
        // =====================================================

        if (asignacion.getCliente().getNumero() == null
                || asignacion
                    .getCliente()
                    .getNumero()
                    .trim()
                    .isEmpty()) {

            return "El número de cliente es obligatorio.";
        }

        // =====================================================
        // VALIDAR NOMBRE
        // =====================================================

        if (asignacion.getCliente().getNombre() == null
                || asignacion
                    .getCliente()
                    .getNombre()
                    .trim()
                    .isEmpty()) {

            return "El nombre del cliente es obligatorio.";
        }

        // =====================================================
        // VALIDAR CURP
        // =====================================================

        if (asignacion.getCliente().getCurp() == null
                || asignacion
                    .getCliente()
                    .getCurp()
                    .trim()
                    .isEmpty()) {

            return "La CURP es obligatoria.";
        }

        return null;
    }
}