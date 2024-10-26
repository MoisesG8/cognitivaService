package com.umg.cognitiva.repository;

import com.umg.cognitiva.dto.ResultadosXUsuario;
import com.umg.cognitiva.model.Resultado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ResultadoRepository extends JpaRepository<Resultado, Long> {

    @Query("SELECT new com.umg.cognitiva.dto.ResultadosXUsuario(u.nombre, a.descripcion, r.puntuacion) " +
            "FROM Resultado r " +
            "JOIN r.actividad a " +
            "JOIN r.usuario u "+
            "WHERE r.usuario.id = :usuarioId")
    List<ResultadosXUsuario> obtenerResultadosConDetalles(@Param("usuarioId") Long usuarioId);
}
