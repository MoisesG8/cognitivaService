package com.umg.cognitiva.controller;

import com.umg.cognitiva.dto.*;
import com.umg.cognitiva.model.Actividad;
import com.umg.cognitiva.model.Usuario;
import com.umg.cognitiva.services.CognitivaServices;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<?> actualizarUsuario(@RequestBody Map<String, Object> parametros) {
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
        Map<String, String> response = new HashMap<>();
        try {
            boolean b = cognitivaServices.registrarResultado(resultDTO);
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

    @GetMapping("/obtenerResultadosUsuario/{id}")
    public ResponseEntity<?> obtenerResultadosUsuario(@PathVariable("id") Long id){
        try{
            return ResponseEntity.ok(cognitivaServices.obtenerResultadosUsuario(id));
        }catch (Exception e){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
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
}
