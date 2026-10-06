package com.dabsa.apidabsa.service;

import com.dabsa.apidabsa.model.Asignacion;
import tools.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AsignacionArchivoService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // =========================================================
    // GUARDAR REGISTROS CORRECTOS
    // =========================================================

    public String guardarAsignaciones(List<Asignacion> asignaciones) {

        try {

            String fecha = generarFechaArchivo();

            File carpeta = obtenerCarpetaAsignaciones();

            File archivo = new File(
                    carpeta,
                    "asignaciones_" + fecha + ".json"
            );

            objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValue(archivo, asignaciones);

            return archivo.getAbsolutePath();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Error al guardar las asignaciones: "
                            + e.getMessage(),
                    e
            );
        }
    }

    // =========================================================
    // GUARDAR REGISTRO DE CONTROL DE LA RECEPCIÓN
    // =========================================================

    public String guardarControlRecepcion(
            int totalRecibidos,
            int totalExitosos,
            int totalFallidos,
            String status,
            List<Map<String, Object>> errores) {

        try {

            String fechaArchivo = generarFechaArchivo();

            File carpeta = obtenerCarpetaAsignaciones();

            Map<String, Object> control = new LinkedHashMap<>();

            control.put(
                    "fecha",
                    LocalDateTime.now().toString()
            );

            control.put(
                    "total_recibidos",
                    totalRecibidos
            );

            control.put(
                    "total_exitosos",
                    totalExitosos
            );

            control.put(
                    "total_fallidos",
                    totalFallidos
            );

            control.put(
                    "status",
                    status
            );

            control.put(
                    "errores",
                    errores
            );

            File archivo = new File(
                    carpeta,
                    "control_" + fechaArchivo + ".json"
            );

            objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValue(archivo, control);

            return archivo.getAbsolutePath();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Error al guardar el registro de control: "
                            + e.getMessage(),
                    e
            );
        }
    }

    // =========================================================
    // OBTENER / CREAR CARPETA DE ASIGNACIONES
    // =========================================================

    private File obtenerCarpetaAsignaciones() {

        File carpeta = new File("asignaciones");

        if (!carpeta.exists()) {

            boolean creada = carpeta.mkdirs();

            if (!creada && !carpeta.exists()) {

                throw new RuntimeException(
                        "No se pudo crear la carpeta asignaciones."
                );
            }
        }

        return carpeta;
    }

    // =========================================================
    // GENERAR FECHA PARA EL NOMBRE DEL ARCHIVO
    // =========================================================

    private String generarFechaArchivo() {

        return LocalDateTime.now()
                .format(
                        DateTimeFormatter.ofPattern(
                                "yyyyMMdd_HHmmss_SSS"
                        )
                );
    }
}