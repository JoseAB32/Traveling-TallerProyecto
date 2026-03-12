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
        System.out.println("=================================");
        System.out.println("INICIALIZANDO DATOS DE PRUEBA...");
        System.out.println("=================================");
        
        // Crear usuario admin
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@travelapp.com");
            admin.setPassword("1234"); // Por ahora sin encriptar
            admin.setRole("ADMIN");
            
            userRepository.save(admin);
            System.out.println("✅ Usuario ADMIN creado:");
            System.out.println("   Usuario: admin");
            System.out.println("   Password: 1234");
        } else {
            System.out.println("✅ Usuario admin ya existe");
        }
        
        // Crear usuario de prueba
        if (!userRepository.existsByUsername("test")) {
            User test = new User();
            test.setUsername("test");
            test.setEmail("test@email.com");
            test.setPassword("1234");
            test.setRole("USER");
            
            userRepository.save(test);
            System.out.println("✅ Usuario TEST creado: test / 1234");
        }
        
        // Mostrar usuarios existentes
        System.out.println("\n📋 USUARIOS EN BASE DE DATOS:");
        userRepository.findAll().forEach(user -> {
            System.out.println("   - " + user.getUsername() + 
                             " (" + user.getRole() + ") - " + 
                             user.getEmail());
        });
        
        System.out.println("=================================\n");
    }
}