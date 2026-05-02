package com.traveling.travel_backend.controller;

import com.traveling.travel_backend.constants.AppConstants;
import com.traveling.travel_backend.dto.LoginRequest;
import com.traveling.travel_backend.dto.LoginResponse;
import com.traveling.travel_backend.dto.UserResponseDTO;
import com.traveling.travel_backend.model.User;
import com.traveling.travel_backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(AppConstants.API_BASE_PATH)
@CrossOrigin(origins = { AppConstants.CORS_LOCALHOST, AppConstants.CORS_NETLIFY })
@Tag(name = "User", description = "Gestión de usuarios y autenticación")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Get all users", description = "Returns a list with the information of all users", operationId = "getAllUsers")
    @GetMapping(AppConstants.USERS_ENDPOINT)
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @Operation(summary = "Create new user", description = "Creates a new user if all checks pass", operationId = "createUser")
    @PostMapping(AppConstants.USERS_ENDPOINT)
    public ResponseEntity<UserResponseDTO> createUser(@RequestBody User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(user));
    }

    @Operation(summary = "Login", description = "Authenticates a user and returns a JWT token", operationId = "login")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest credentials) {
        return ResponseEntity.ok(userService.login(credentials));
    }
}