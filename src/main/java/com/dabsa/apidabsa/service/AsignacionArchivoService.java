package com.dabsa.apidabsa.service;

import com.dabsa.apidabsa.model.Asignacion;
import tools.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class AsignacionArchivoService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String guardarAsignaciones(List<Asignacion> asignaciones) {

        try {

            String fecha = LocalDateTime.now()
                    .format(
                            DateTimeFormatter.ofPattern(
                                    "yyyyMMdd_HHmmss"
                            )
                    );

            File carpeta = new File("asignaciones");

            if (!carpeta.exists()) {
                carpeta.mkdirs();
            }

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
}