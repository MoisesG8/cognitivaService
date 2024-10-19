package com.umg.cognitiva.services;

import com.umg.cognitiva.dto.LoginResponse;
import com.umg.cognitiva.dto.SesionDTO;
import com.umg.cognitiva.dto.UsuarioLoginResponse;
import com.umg.cognitiva.model.Actividad;
import com.umg.cognitiva.model.Sesion;
import com.umg.cognitiva.model.Usuario;
import com.umg.cognitiva.repository.ActividadRepository;
import com.umg.cognitiva.repository.SesionRepository;
import com.umg.cognitiva.repository.UsuarioRepository;
import com.umg.cognitiva.utilerias.JwTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class CognitivaServices {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JwTokenProvider jwTokenProvider;

    @Autowired
    private ActividadRepository actividadRepository;

    @Autowired
    private SesionRepository sesionRepository;

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

    public LoginResponse login(String email, String password) {
        LoginResponse loginResponse = new LoginResponse();
        Optional <Usuario> usuario = usuarioRepository.findByCorreoAndPassword(email, password);
        if (usuario.isPresent()) {
            Usuario u = usuario.get();
            String token = jwTokenProvider.generateToken(email);
            UsuarioLoginResponse usuarioLoginResponse = new UsuarioLoginResponse(u.getId(), u.getNombre(), u.getCorreo(), u.getTotal_puntos());
            loginResponse.setToken(token);
            loginResponse.setUsuarioLoginResponse(usuarioLoginResponse);
        } else {
            loginResponse = null;
        }
        return loginResponse;
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

    // Método para obtener todas las actividades
    public List<Actividad> obtenerActividades() {
        return actividadRepository.findAll();
    }

    // Método para registrar una nueva sesión
    public boolean registrarSesion(SesionDTO sesionDTO) {
        try {
            Usuario usuarioExistente = usuarioRepository.findById(sesionDTO.getIdUsuario()).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            Sesion nuevaSesion = new Sesion();
            nuevaSesion.setUsuario(usuarioExistente);
            nuevaSesion.setFechaInicio(sesionDTO.getFechaInicio());
            nuevaSesion.setFechaFin(sesionDTO.getFechaFin());
            nuevaSesion.setDuracionTotal(sesionDTO.getDuracionTotal());

            sesionRepository.save(nuevaSesion);
            return true;
        }catch (Exception e) {
            return false;
        }
    }
}
