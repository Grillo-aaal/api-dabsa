package com.dabsa.apidabsa.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dabsa.apidabsa.model.Asignacion;
import com.dabsa.apidabsa.service.AsignacionArchivoService;

import java.util.List;

@RestController
@RequestMapping("/api/asignaciones")
public class AsignacionController {

    private final AsignacionArchivoService archivoService;

    public AsignacionController(AsignacionArchivoService archivoService) {
        this.archivoService = archivoService;
    }

    @PostMapping
    public String recibirAsignacion(@RequestBody List<Asignacion> asignaciones) {

        System.out.println("Asignaciones recibidas: " + asignaciones.size());

        for (Asignacion asignacion : asignaciones) {

            System.out.println("Cliente: " + asignacion.getCliente().getNombre());

            System.out.println("Contratos recibidos: " + asignacion.getContrato().size());

            System.out.println("Teléfonos recibidos: " + asignacion.getTelefono().size());

            System.out.println("Direcciones recibidas: " + asignacion.getDireccion().size());

            System.out.println("-----------------------------");
        }

        String rutaArchivo = archivoService.guardarAsignaciones(asignaciones);

        System.out.println("Archivo guardado en: " + rutaArchivo);

        return "Asignaciones recibidas correctamente";
    }
}