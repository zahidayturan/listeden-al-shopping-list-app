package com.example.listedenalbackend.controller;

import com.example.listedenalbackend.model.User;
import com.example.listedenalbackend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Tag(name = "Kullanıcı Yönetimi", description = "Kullanıcıların kaydı, girişi ve profil yönetimi API'leri")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Tüm kullanıcıları getirir. (Genellikle ADMIN yetkisi gereklidir).
     * GET /api/users
     * @return Tüm kullanıcıların listesiyle birlikte 200 OK yanıtı.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')") // Spring Security entegrasyonu sonrası
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        users.forEach(user -> user.setPasswordHash(null)); // Güvenlik için
        return ResponseEntity.ok(users);
    }

    /**
     * Belirli bir kullanıcıyı ID'sine göre getirir.
     * GET /api/users/{id}
     * @param id Kullanıcının ID'si.
     * @param currentUser Mevcut oturum açmış kullanıcının detayları.
     * @return Kullanıcı nesnesiyle birlikte 200 OK yanıtı.
     * @throws IllegalArgumentException Kullanıcı bulunamazsa (GlobalExceptionHandler yakalar).
     * @throws SecurityException Kullanıcı kendi profili değilse ve ADMIN yetkisi yoksa (GlobalExceptionHandler yakalar).
     */
    @Operation(summary = "Belirli bir kullanıcıyı ID'sine göre getirir",
            description = "Kullanıcının kendi profilini veya ADMIN yetkisiyle başka bir kullanıcının profilini getirir.",
            parameters = @Parameter(name = "id", description = "Kullanıcı ID'si", required = true),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Kullanıcı başarıyla getirildi",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))),
                    @ApiResponse(responseCode = "404", description = "Kullanıcı bulunamadı"),
                    @ApiResponse(responseCode = "403", description = "Bu kaynağa erişim yetkiniz yok")
            })
    @GetMapping("/{id}")
    // @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id") // Spring Security entegrasyonu sonrası
    public ResponseEntity<User> getUserById(@PathVariable Long id, @AuthenticationPrincipal UserDetails currentUser) {
        // Servis katmanında yetki kontrolü yapılır.
        User user = userService.getUserById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
        user.setPasswordHash(null); // Hassas bilgiyi kaldırma
        return ResponseEntity.ok(user);
    }

    /**
     * Belirli bir kullanıcıyı günceller.
     * PUT /api/users/{id}
     * @param id Güncellenecek kullanıcının ID'si.
     * @param userDetails Güncellenmiş kullanıcı bilgileri.
     * @param currentUser Mevcut oturum açmış kullanıcının detayları.
     * @return Güncellenmiş kullanıcı nesnesiyle birlikte 200 OK yanıtı.
     * @throws IllegalArgumentException Kullanıcı bulunamazsa veya username/email zaten kullanılıyorsa (GlobalExceptionHandler yakalar).
     * @throws SecurityException Yetkisiz erişim varsa (GlobalExceptionHandler yakalar).
     */
    @PutMapping("/{id}")
    // @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id") // Spring Security entegrasyonu sonrası
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User userDetails, @AuthenticationPrincipal UserDetails currentUser) {
        // Servis katmanında yetki kontrolü yapılır.
        User updatedUser = userService.updateUser(id, userDetails);
        updatedUser.setPasswordHash(null);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Belirli bir kullanıcıyı siler.
     * DELETE /api/users/{id}
     * @param id Silinecek kullanıcının ID'si.
     * @param currentUser Mevcut oturum açmış kullanıcının detayları.
     * @return 204 No Content yanıtı.
     * @throws IllegalArgumentException Kullanıcı bulunamazsa (GlobalExceptionHandler yakalar).
     * @throws SecurityException Yetkisiz erişim varsa (GlobalExceptionHandler yakalar).
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT) // 204 No Content
    // @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id") // Spring Security entegrasyonu sonrası
    public void deleteUser(@PathVariable Long id, @AuthenticationPrincipal UserDetails currentUser) {
        // Servis katmanında yetki kontrolü yapılır.
        userService.deleteUser(id);
    }
}