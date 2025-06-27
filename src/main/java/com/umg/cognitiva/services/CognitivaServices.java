package com.umg.cognitiva.services;

import com.umg.cognitiva.dto.*;
import com.umg.cognitiva.model.Actividad;
import com.umg.cognitiva.model.Resultado;
import com.umg.cognitiva.model.Sesion;
import com.umg.cognitiva.model.Usuario;
import com.umg.cognitiva.repository.ActividadRepository;
import com.umg.cognitiva.repository.ResultadoRepository;
import com.umg.cognitiva.repository.SesionRepository;
import com.umg.cognitiva.repository.UsuarioRepository;
import com.umg.cognitiva.utilerias.JwTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

    @Autowired
    private ResultadoRepository resultadoRepository;

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
        Optional usuario = usuarioRepository.spLogin(email, password);
        if (usuario.isPresent()) {
            Usuario u = (Usuario) usuario.get();
            String token = jwTokenProvider.generateToken(email);
            UsuarioLoginResponse usuarioLoginResponse = new UsuarioLoginResponse(u.getId(), u.getNombre(), u.getCorreo(), u.getTotal_puntos());
            loginResponse.setToken(token);
            loginResponse.setUsuarioLoginResponse(usuarioLoginResponse);
        } else {
            loginResponse = null;
        }
        return loginResponse;
    }

    /**
     * Llama al SP via repository y devuelve true si retorna un ID válido.
     */
    public boolean crearUsuario(AddUserDTO usuario) {
        try {
            Integer newId = usuarioRepository.spRegistrarUsuario(
                    usuario.getNombre(),
                    usuario.getCorreo(),
                    usuario.getPassword(),
                    usuario.getEdad()
            );
            return newId != null;
        } catch (Exception e) {
            // loguea e si lo deseas
            return false;
        }
    }

    // Método para obtener todas las actividades
    public List<Actividad> obtenerActividades() {
        return actividadRepository.findAll();
    }

    public boolean registrarResultado(AddResultDTO addResultDTO){

        try {
            Usuario usuario = usuarioRepository.findById(addResultDTO.getIdUsuario()).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            Actividad actividad = actividadRepository.findById(addResultDTO.getIdActividad()).orElseThrow(() -> new RuntimeException("Actividad no encontrado"));

            Resultado nuevoResultado = new Resultado();
            nuevoResultado.setUsuario(usuario);
            nuevoResultado.setActividad(actividad);
            nuevoResultado.setPuntuacion(addResultDTO.getPuntuacion());
            nuevoResultado.setTiempoTotal(addResultDTO.getTiempoTotal());
            nuevoResultado.setFechaRealizacion(addResultDTO.getFechaRealizacion());

            resultadoRepository.save(nuevoResultado);
            return true;
        }catch (Exception e) {
            return false;
        }

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

    public List<ResultadosXUsuario> obtenerResultadosUsuario(Long id){

        Usuario usuario = usuarioRepository.findById(id).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<ResultadosXUsuario> resultados = resultadoRepository.obtenerResultadosConDetalles(id);
        return resultados;

    }

    /** Crea la sesión y devuelve el ID y la fecha de inicio */
    public StartSessionResponseDTO iniciarSesion(StartSessionDTO dto) {
        Usuario u = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        Sesion sesion = new Sesion();
        sesion.setUsuario(u);
        sesion.setFechaInicio(LocalDateTime.now());
        // fechaFin y duracion_total quedan null/0
        sesion = sesionRepository.save(sesion);

        return new StartSessionResponseDTO(
                sesion.getId(),
                sesion.getFechaInicio().toString()
        );
    }

    /** Cierra la sesión abierta y calcula la duración */
    public EndSessionResponseDTO finalizarSesion(Long sesionId) {
        Sesion sesion = sesionRepository.findById(sesionId)
                .orElseThrow(() -> new IllegalArgumentException("Sesión no encontrada"));

        LocalDateTime fin = LocalDateTime.now();
        sesion.setFechaFin(fin);
        int segundos = (int) Duration.between(sesion.getFechaInicio(), fin).getSeconds();
        sesion.setDuracionTotal(segundos);

        sesionRepository.save(sesion);

        return new EndSessionResponseDTO(
                sesion.getId(),
                segundos,
                sesion.getFechaFin().toString()
        );
    }
}
