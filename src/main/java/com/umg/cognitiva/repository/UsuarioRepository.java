package com.umg.cognitiva.repository;

import com.umg.cognitiva.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
}
