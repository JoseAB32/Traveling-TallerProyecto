package com.traveling.travel_backend.controller;

import com.traveling.travel_backend.dto.ForgotPasswordRequest;
import com.traveling.travel_backend.dto.ResetPasswordRequest;
import com.traveling.travel_backend.service.AuthService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/forgot-password")
    public void forgotPassword(@RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.getCorreo());
    }

    @PostMapping("/reset-password")
    public boolean resetPassword(@RequestBody ResetPasswordRequest request) {
        return authService.resetPassword(
                request.getToken(),
                request.getPassword(),
                request.getConfirmPassword()
        );
    }
}