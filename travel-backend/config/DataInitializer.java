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
        // Crear usuario admin si no existe
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@travelapp.com");
            admin.setPassword("1234"); // En producción deberías encriptar
            admin.setRole("ADMIN");
            
            userRepository.save(admin);
            System.out.println("✅ Usuario admin creado: admin / 1234");
        } else {
            System.out.println("✅ Usuario admin ya existe");
        }
        
        // Opcional: crear un usuario de prueba
        if (!userRepository.existsByUsername("test")) {
            User test = new User();
            test.setUsername("test");
            test.setEmail("test@email.com");
            test.setPassword("1234");
            test.setRole("USER");
            
            userRepository.save(test);
            System.out.println("✅ Usuario test creado: test / 1234");
        }
    }
}