package com.umg.cognitiva.services;

import com.umg.cognitiva.model.Usuario;
import com.umg.cognitiva.repository.CognitivaRepository;
import com.umg.cognitiva.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Date;
import java.util.Optional;

@Service
public class CognitivaServices {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Método para actualizar un usuario existente
    public Optional<Usuario> actualizarInfoUsuario(Long id, Usuario usuarioActualizado) {
        Optional<Usuario> usuarioExistente = Optional.ofNullable(usuarioRepository.findById(id).orElseThrow(() -> new RuntimeException("USUARIO NO ENCONTRADO")));

        if (usuarioExistente.isPresent()) {
            Usuario usuario = usuarioExistente.get();
            usuario.setNombre(usuarioActualizado.getNombre());
            usuario.setCorreo(usuarioActualizado.getCorreo());
            //usuario.setPassword(usuarioActualizado.getPassword());
            usuario.setEdad(usuarioActualizado.getEdad());
            //usuario.setTotal_puntos(usuarioActualizado.getTotal_puntos());
            return Optional.of(usuarioRepository.save(usuario));
        } else {
            return Optional.empty();
        }
    }

    public Optional<Usuario> actualizarPuntos(Long id, int puntos) {
        Optional<Usuario> usuarioExistente = Optional.ofNullable(usuarioRepository.findById(id).orElseThrow(() -> new RuntimeException("USUARIO NO ENCONTRADO")));
        if (usuarioExistente.isPresent()) {
            Usuario usuario = usuarioExistente.get();
            usuario.setTotal_puntos(puntos);
            return Optional.of(usuarioRepository.save(usuario));
        } else {
            return Optional.empty();
        }
    }


    public boolean crearUsuario(Usuario usuario) {
        try {
            usuario.setFechaRegistro(LocalDate.now());
            usuarioRepository.save(usuario);
            return true;
        }catch (Exception e) {
            return false;
        }
    }
}
