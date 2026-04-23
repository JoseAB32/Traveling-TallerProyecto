package com.traveling.travel_backend.controller;

import org.springframework.web.bind.annotation.*;
import com.traveling.travel_backend.dto.PasswordResetRequest;
import com.traveling.travel_backend.dto.NewPasswordRequest;
import com.traveling.travel_backend.service.PasswordResetService;

@RestController
@RequestMapping("/api/password")
public class PasswordResetController {

    private final PasswordResetService service;

    public PasswordResetController(PasswordResetService service) {
        this.service = service;
    }

    @PostMapping("/forgot")
    public String forgot(@RequestBody PasswordResetRequest request) {
        return service.sendResetLink(request.getCorreo());
    }

    @PostMapping("/reset")
    public String reset(@RequestBody NewPasswordRequest request) {
        return service.resetPassword(request.getToken(), request.getPassword());
    }
}