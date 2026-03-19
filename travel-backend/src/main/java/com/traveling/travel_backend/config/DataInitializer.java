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
        System.out.println("\n=== INICIALIZANDO DATOS DE PRUEBA ===");
        
        // Crear usuario admin si no existe
        if (!userRepository.existsByUserName("admin")) {
            User admin = new User();
            admin.setUserName("admin");
            admin.setCorreo("admin@gmail.com");
            admin.setPass("1234");
            admin.setState(true);
            
            userRepository.save(admin);
            System.out.println("✅ Usuario ADMIN creado: admin / 1234");
        }
        
        // Crear usuario test si no existe
        if (!userRepository.existsByUserName("test")) {
            User test = new User();
            test.setUserName("test");
            test.setCorreo("test@email.com");
            test.setPass("1234");
            test.setState(true);
            
            userRepository.save(test);
            System.out.println("✅ Usuario TEST creado: test / 1234");
        }
        
        // Mostrar usuarios
        System.out.println("\n📊 USUARIOS EN BD:");
        userRepository.findAll().forEach(user -> 
            System.out.println("   - " + user.getUserName() + " (" + user.getCorreo() + ")")
        );
        
        System.out.println("=== INICIALIZACIÓN COMPLETADA ===\n");
    }
}