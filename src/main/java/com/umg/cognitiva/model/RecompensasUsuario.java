package com.umg.cognitiva.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "recompensas_usuarios")
@AllArgsConstructor
@NoArgsConstructor
public class RecompensasUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recompensa_usuario_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "recompensa_id", nullable = false)
    private Recompensa recompensa;

    @Column(name = "fecha_obtenida", nullable = false)
    private LocalDateTime fechaObtenida;
}