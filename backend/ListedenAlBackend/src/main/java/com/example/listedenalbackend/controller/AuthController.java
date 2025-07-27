package com.example.listedenalbackend.controller;

import com.example.listedenalbackend.model.User;
import com.example.listedenalbackend.security.jwt.JwtTokenProvider;
import com.example.listedenalbackend.service.UserService;
import com.example.listedenalbackend.dto.RegisterRequest;
import com.example.listedenalbackend.dto.LoginRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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

    @Operation(summary = "Register a new user",
            description = "Registers a new user with a unique username and a password. Returns the created user object without the password hash.",
            requestBody = @RequestBody(
                    description = "User details for registration",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RegisterRequest.class), // Use DTO here
                            examples = @ExampleObject(
                                    name = "New User Example",
                                    summary = "Example of a new user",
                                    value = "{\"username\": \"john.doe\", \"password\": \"securePassword123\", \"email\": \"john.doe@example.com\", \"firstName\": \"John\", \"lastName\": \"Doe\"}"
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "User successfully registered",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = User.class),
                                    examples = @ExampleObject(
                                            name = "Registered User Response",
                                            summary = "Response for a successfully registered user",
                                            value = "{\"id\": 1, \"username\": \"john.doe\", \"email\": \"john.doe@example.com\", \"firstName\": \"John\", \"lastName\": \"Doe\"}"
                                            // passwordHash will be null or omitted due to @ToString exclude and explicit nulling
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid user data or username/email already exists",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "Bad Request Example",
                                            summary = "Example of a bad request",
                                            value = "{\"status\": 400, \"error\": \"Bad Request\", \"message\": \"Username 'john.doe' already exists.\"}"
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "Server Error Example",
                                            summary = "Example of an internal server error",
                                            value = "{\"timestamp\": \"2023-10-27T10:00:00.000+00:00\", \"status\": 500, \"error\": \"Internal Server Error\", \"message\": \"An unexpected error occurred.\"}"
                                    )
                            )
                    )
            })
    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@RequestBody RegisterRequest registerRequest) {
        System.out.println("Received RegisterRequest: " + registerRequest.toString());

        User registeredUser = userService.createUser(registerRequest);
        registeredUser.setPasswordHash(null);
        return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
    }

    @Operation(summary = "Authenticate user and get JWT token",
            description = "Authenticates a user with provided username and password and returns a JWT access token.",
            requestBody = @RequestBody(
                    description = "User credentials for login",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginRequest.class), // Use DTO here
                            examples = @ExampleObject(
                                    name = "Login Request Example",
                                    summary = "Example for user login",
                                    value = "{\"username\": \"john.doe\", \"password\": \"securePassword123\"}"
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User successfully authenticated and token generated",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "Login Success Response",
                                            summary = "Response with JWT token",
                                            value = "{\"accessToken\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\", \"tokenType\": \"Bearer\", \"username\": \"john.doe\"}"
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Invalid credentials",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "Unauthorized Example",
                                            summary = "Example of invalid credentials",
                                            value = "{\"status\": 401, \"error\": \"Unauthorized\", \"message\": \"Bad credentials\"}"
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "Server Error Example",
                                            summary = "Example of an internal server error",
                                            value = "{\"timestamp\": \"2023-10-27T10:00:00.000+00:00\", \"status\": 500, \"error\": \"Internal Server Error\", \"message\": \"An unexpected error occurred.\"}"
                                    )
                            )
                    )
            })
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