package com.umg.cognitiva.scheduled;

import com.umg.cognitiva.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ConsejoScheduler {

    @Autowired
    private UsuarioRepository usuarioRepo;

    // Corre cada 6 horas (ajusta el cron según lo desees)
    @Scheduled(cron = "0 0 */6 * * *")
    public void enviarConsejos(){
        System.out.println("Consejos enviados");
    }
}
