package com.traveling.travel_backend.service;

import com.traveling.travel_backend.model.User;
import com.traveling.travel_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    // 1. Solicitar recuperación
    public void forgotPassword(String correo) {

        var userOpt = userRepository.findByCorreo(correo);

        if (userOpt.isEmpty()) return; // seguridad

        User user = userOpt.get();

        String token = UUID.randomUUID().toString();

        user.setResetToken(token);
        user.setTokenExpiration(LocalDateTime.now().plusHours(1));

        userRepository.save(user);

        String link = "http://localhost:4200/reset-password?token=" + token;

        emailService.sendEmail(
                user.getCorreo(),
                "Recuperación de contraseña",
                "Haz clic aquí: " + link
        );
    }

    // 2. Resetear contraseña
    public boolean resetPassword(String token, String password, String confirmPassword) {

        if (!password.equals(confirmPassword)) return false;
        if (password.length() < 8) return false;

        var userOpt = userRepository.findByResetToken(token);

        if (userOpt.isEmpty()) return false;

        User user = userOpt.get();

        if (user.getTokenExpiration() == null ||
            user.getTokenExpiration().isBefore(LocalDateTime.now())) {
            return false;
        }

        user.setPass(passwordEncoder.encode(password));
        user.setResetToken(null);
        user.setTokenExpiration(null);

        userRepository.save(user);

        return true;
    }
}