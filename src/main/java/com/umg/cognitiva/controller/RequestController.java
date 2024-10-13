package com.umg.cognitiva.controller;

import com.umg.cognitiva.model.Usuario;
import com.umg.cognitiva.services.CognitivaServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1")
public class RequestController {

    @Autowired
    private CognitivaServices cognitivaServices;

    @PostMapping("/addUsuario")
    public ResponseEntity<?> addUsuario(@RequestBody Usuario usuario) {
        if (cognitivaServices.crearUsuario(usuario)){
            return new ResponseEntity<>("Usuario agregado con exito", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Error al crear el usuario", HttpStatus.BAD_REQUEST);
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

        if (usuario.isPresent()) {
            return new ResponseEntity<>("Puntos de usuario " + usuario.get().getNombre() + " actualizados con éxito", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Error al actualizar puntos",HttpStatus.BAD_REQUEST);
        }
    }
}
