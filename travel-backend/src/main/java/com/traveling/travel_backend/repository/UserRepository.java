package com.traveling.travel_backend.repository;

import com.traveling.travel_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Buscar por username (campo moderno)
    Optional<User> findByUsername(String username);
    
    // Buscar por email (campo moderno)
    Optional<User> findByEmail(String email);
    
    // Verificar existencia por username
    boolean existsByUsername(String username);
    
    // Verificar existencia por email
    boolean existsByEmail(String email);
    
    // Buscar usuarios por rol
    List<User> findByRole(String role);
    
    // Buscar usuarios activos/inactivos
    List<User> findByActive(boolean active);
    
    // Buscar por ciudad
    List<User> findByCity(String city);
    
    // Buscar por username que contenga (búsqueda parcial)
    List<User> findByUsernameContainingIgnoreCase(String username);
    
    // Buscar por email que contenga
    List<User> findByEmailContainingIgnoreCase(String email);
    
    // Contar usuarios por rol
    long countByRole(String role);
    
    // Contar usuarios activos
    long countByActive(boolean active);
}