package com.example.listedenalbackend.controller;

import com.example.listedenalbackend.model.User;
import com.example.listedenalbackend.security.jwt.JwtTokenProvider;
import com.example.listedenalbackend.service.UserService;
import com.example.listedenalbackend.dto.RegisterRequest;
import com.example.listedenalbackend.dto.LoginRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "Authentication", description = "User registration and login operations")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    @Autowired
    public AuthController(UserService userService, AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    @GetMapping("/hello")
    public ResponseEntity<String> sayHello() {
        return ResponseEntity.ok("Hello from Spring Boot API!");
    }

    @GetMapping("/greet/{name}")
    public ResponseEntity<String> greetUser(@PathVariable String name) {
        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Name cannot be empty.");
        }
        return ResponseEntity.ok("Hello, " + name + " from Spring Boot API!");
    }

    @Operation(
            summary = "Register a new user",
            description = "Registers a new user with a unique username and a password. Returns the created user object."
    )
    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@RequestBody RegisterRequest registerRequest) {
        System.out.println("Received RegisterRequest: " + registerRequest.toString());

        User registeredUser = userService.createUser(registerRequest);
        registeredUser.setPasswordHash(null);
        return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Authenticate user and get JWT token",
            description = "Authenticates a user with provided username and password and returns a JWT access token."
    )
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> authenticateUser(@RequestBody LoginRequest loginDto) { // Use DTO here
        String username = loginDto.getUsername();
        String password = loginDto.getPassword();

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.generateToken(authentication);

        Map<String, String> response = new HashMap<>();
        response.put("accessToken", jwt);
        response.put("tokenType", "Bearer");
        response.put("username", username);

        return ResponseEntity.ok(response);
    }
}