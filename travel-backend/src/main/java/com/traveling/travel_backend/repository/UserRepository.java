package com.traveling.travel_backend.repository;

import com.traveling.travel_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUserName(String userName);
    boolean existsByUserName(String userName);
    boolean existsByCorreo(String correo);
    Optional<User> findByUserNameAndStateTrue(String userName);

    Optional<User> findByCorreo(String correo);
    Optional<User> findByResetToken(String resetToken);
}