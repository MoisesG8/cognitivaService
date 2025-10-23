package com.umg.cognitiva.controller;

import com.itextpdf.text.DocumentException;
import com.umg.cognitiva.dto.*;
import com.umg.cognitiva.model.Actividad;
import com.umg.cognitiva.model.Usuario;
import com.umg.cognitiva.services.CognitivaServices;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
public class RequestController {

    @Autowired
    private CognitivaServices cognitivaServices;

    @Qualifier("conversionService")

    @PostMapping("/addUsuario")
    public ResponseEntity<?> addUsuario(
            @Valid @RequestBody AddUserDTO dto,
            BindingResult bindingResult) {

        Map<String, Object> response = new HashMap<>();

        if (bindingResult.hasErrors()) {
            String errores = bindingResult.getFieldErrors()
                    .stream()
                    .map(f -> f.getField() + ": " + f.getDefaultMessage())
                    .collect(Collectors.joining("; "));
            response.put("estado", "error");
            response.put("mensaje", "Datos inválidos: " + errores);
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(response);
        }

        try {
            boolean resultado = cognitivaServices.crearUsuario(
                    dto
            );
            if (resultado){
                response.put("status", true);
                response.put("mensaje", "Usuario creado exitosamente");
                return ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(response);
            } else {
                response.put("status", false);
                response.put("mensaje", "No fue posible crear el usuario");
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(response);
            }


        } catch (Exception e) {
            response.put("estado", "error");
            response.put("mensaje", "Error interno al crear usuario");
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }
    }

    // Endpoint para actualizar un usuario existente
    @PutMapping("/editar/{id}")
    public ResponseEntity<?> actualizarUsuario(@PathVariable("id") Long id, @RequestBody Usuario usuarioActualizado) {
        Optional<Usuario> usuario = cognitivaServices.actualizarInfoUsuario(id, usuarioActualizado);

        if (usuario.isPresent()) {
            return new ResponseEntity<>("Usuario actualizado con exito", HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/actualizarPuntos")
    public ResponseEntity<?> actualizarPuntosUsuario(@RequestBody Map<String, Object> parametros) {
        // Convertir id y puntos con manejo adecuado de tipos
        Long id = ((Number) parametros.get("id")).longValue();  // Convierte a Long
        Integer puntos = (Integer) parametros.get("puntos");    // Mantenemos puntos como Integer

        Optional<Usuario> usuario = cognitivaServices.actualizarPuntos(id, puntos);
        return usuario.<ResponseEntity<?>>map(value -> new ResponseEntity<>("Puntos de usuario " + value.getNombre() + " actualizados con éxito", HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>("Error al actualizar puntos", HttpStatus.BAD_REQUEST));
    }

    // Endpoint para obtener todas las actividades registradas
    @GetMapping("/listarActividades")
    public ResponseEntity<?> listarActividades() {
        try {
            List<Actividad> actividades = cognitivaServices.obtenerActividades();
            return new ResponseEntity<>(actividades, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(Collections.emptyList(), HttpStatus.OK);
        }
    }

    @PostMapping("/registrarSesion")
    public ResponseEntity<?> addSesion(@RequestBody SesionDTO sesionDTO) {
        Map<String, String> response = new HashMap<>();
        try {
            boolean b = cognitivaServices.registrarSesion(sesionDTO);
            if (b) {
                response.put("estado", "exito");
            } else {
                response.put("estado", "error");
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("estado", "error");
            return ResponseEntity.ok(response);
        }
    }

    @PostMapping("/registrarResultado")
    public ResponseEntity<?> addResultado(@RequestBody AddResultDTO resultDTO) {
        System.out.println("Ejecutando actualizacion "+resultDTO);
        Map<String, String> response = new HashMap<>();
        try {
            boolean b = cognitivaServices.registrarResultado(resultDTO);
            if (b) {
                response.put("estado", "exito");
                return ResponseEntity.ok(response);
            } else {
                response.put("estado", "error");
                return ResponseEntity.badRequest().body(response);
            }

        } catch (Exception e) {
            response.put("estado", "error");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/startSession")
    public ResponseEntity<StartSessionResponseDTO> start(
            @RequestBody StartSessionDTO dto) {
        StartSessionResponseDTO resp = cognitivaServices.iniciarSesion(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @PutMapping("/{id}/endSession")
    public ResponseEntity<EndSessionResponseDTO> end(
            @PathVariable("id") Long sesionId) {
        EndSessionResponseDTO resp = cognitivaServices.finalizarSesion(sesionId);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/registrarEstadoAnimo")
    public ResponseEntity<?> registrarEstadoAnimo(@RequestBody EstadoAnimoDTO dto) {
        Map<String, String> response = new HashMap<>();
        try {
            boolean b = cognitivaServices.registrarEstadoAnimo(dto);
            if (b) {
                response.put("estado", "exito");
            } else {
                response.put("estado", "error");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("estado", "error");
            return (ResponseEntity<?>) ResponseEntity.badRequest  ();
        }
    }

    @GetMapping("/descargar/{usuarioId}")
    public ResponseEntity<?> descargarReporte(@PathVariable Long usuarioId) throws IOException, DocumentException {
        try {
            byte[] pdf = cognitivaServices.generateEstadoAnimoPdf(usuarioId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition
                    .attachment()
                    .filename("reporte_estado_animo.pdf")
                    .build());

            return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al generar pdf");
        }
    }

    @GetMapping("/enviar-reporte/{usuarioId}")
    public ResponseEntity<?> enviarReportePorCorreo(@PathVariable Long usuarioId) {
        Map<String, Object> response = new HashMap<>();
        try {

            boolean enviado = cognitivaServices.enviarReporte(usuarioId);
            if (enviado) {
                response.put("estado", "exito");
                response.put("status", true);
                return ResponseEntity.ok(response);
            } else {
                response.put("estado", "error");
                response.put("status", false);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al enviar el correo.");
            }
        } catch (Exception e) {
            response.put("estado", "error");
            response.put("status", false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
