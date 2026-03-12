package com.traveling.travel_backend.config;

import com.traveling.travel_backend.model.User;
import com.traveling.travel_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== INICIALIZANDO DATOS DE PRUEBA ===");
        
        // Usar findByUsername (NO findByUserName)
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@gmail.com");
            admin.setPassword("1234");
            admin.setRole("ADMIN");
            admin.setActive(true);
            
            userRepository.save(admin);
            System.out.println("✅ Usuario admin creado");
        }
        
        // Listar usuarios
        System.out.println("\n📊 USUARIOS EN BASE DE DATOS:");
        userRepository.findAll().forEach(user -> 
            System.out.println("   - " + user.getUsername() + " (" + user.getRole() + ")")
        );
    }
}