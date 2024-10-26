package com.umg.cognitiva.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResultadosXUsuario {

    String nombre;
    String nombreJuego;
    int puntuacion;
}
