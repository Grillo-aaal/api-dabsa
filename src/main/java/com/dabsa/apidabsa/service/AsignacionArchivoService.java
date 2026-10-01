package com.dabsa.apidabsa.service;

import com.dabsa.apidabsa.model.Asignacion;
import tools.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class AsignacionArchivoService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String guardarAsignacion(Asignacion asignacion) {

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
                    "asignacion_" + fecha + ".json"
            );

            objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValue(archivo, asignacion);

            return archivo.getAbsolutePath();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Error al guardar la asignación: "
                            + e.getMessage(),
                    e
            );
        }
    }
}