package com.example.listedenalbackend.controller;

import com.example.listedenalbackend.model.User;
import com.example.listedenalbackend.service.UserService;
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

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    //private final AuthenticationManager authenticationManager;

    @Autowired
    public AuthController(UserService userService/*, AuthenticationManager authenticationManager*/) {
        this.userService = userService;
        //this.authenticationManager = authenticationManager;
    }

    /**
     * Yeni bir kullanıcı kaydeder.
     * POST /api/auth/register
     * @param user Kaydedilecek kullanıcı bilgileri (username, email, passwordHash).
     * @return Oluşturulan kullanıcı nesnesiyle birlikte 201 Created yanıtı.
     * @throws IllegalArgumentException Kullanıcı adı veya e-posta zaten mevcutsa (GlobalExceptionHandler yakalar).
     */
    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@RequestBody User user) {
        User registeredUser = userService.createUser(user);
        registeredUser.setPasswordHash(null); // Güvenlik için parola hash'ini göndermiyoruz
        return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
    }

    /**
     * Kullanıcı kimlik doğrulaması yapar ve oturum açar.
     * POST /api/auth/login
     * @param loginRequest Kullanıcı adı ve parolayı içeren Map.
     * @return Başarılı giriş mesajıyla birlikte 200 OK yanıtı.
     * (Gerçek uygulamada burada JWT token dönecektir).
     * @throws org.springframework.security.core.AuthenticationException Geçersiz kimlik bilgileri ise (GlobalExceptionHandler yakalar).
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> authenticateUser(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");

       /* Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );*/

        //SecurityContextHolder.getContext().setAuthentication(authentication);

        // Burada JWT token oluşturma ve döndürme mantığı yer alacaktır.
        // Örnek: final String jwt = jwtTokenProvider.generateToken(authentication);
        // return ResponseEntity.ok(new JwtAuthResponse(jwt));

        Map<String, String> response = new HashMap<>();
        response.put("message", "User logged in successfully!");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}