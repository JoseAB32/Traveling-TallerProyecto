package com.traveling.travel_backend.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.traveling.travel_backend.model.*;
import com.traveling.travel_backend.repository.*;

@Service
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public PasswordResetService(UserRepository userRepository,
                                PasswordResetTokenRepository tokenRepository,
                                BCryptPasswordEncoder passwordEncoder,
                                EmailService emailService) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    // 🔹 Enviar link
    public String sendResetLink(String correo) {

        User user = userRepository.findByCorreo(correo).orElse(null);

        // 🔐 No revelar si existe
        if (user == null || !user.isState()) {
            return "Si el correo existe, se enviará un enlace";
        }

        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiration(LocalDateTime.now().plusMinutes(15));

        tokenRepository.save(resetToken);

        String link = "http://localhost:4200/reset-password?token=" + token;

        // 📧 ENVÍO REAL
        emailService.sendResetEmail(user.getCorreo(), link);

        return "Si el correo existe, se enviará un enlace";
    }

    // 🔹 Reset password
    public String resetPassword(String token, String password) {

        if (password == null || password.length() < 8) {
            return "La contraseña debe tener mínimo 8 caracteres";
        }

        PasswordResetToken resetToken = tokenRepository.findByToken(token).orElse(null);

        if (resetToken == null || resetToken.getExpiration().isBefore(LocalDateTime.now())) {
            return "Token inválido o expirado";
        }

        User user = resetToken.getUser();

        user.setPass(passwordEncoder.encode(password));

        userRepository.save(user);
        tokenRepository.delete(resetToken);

        return "Contraseña actualizada correctamente";
    }
}