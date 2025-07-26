package com.example.listedenalbackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity // Web güvenliğini etkinleştirir
public class SecurityConfig {

    // JWT kimlik doğrulaması için özel filtre (ileride ekleyeceğiniz)
    // @Autowired
    // private JwtAuthenticationFilter jwtAuthenticationFilter;

    // AuthenticationEntryPoint (401 Unauthorized yanıtları için)
    // @Autowired
    // private JwtAuthenticationEntryPoint unauthorizedHandler;

    // 1. PasswordEncoder Bean'i: Parolaları hash'lemek için kullanılır.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 2. AuthenticationManager Bean'i: Kimlik doğrulama sürecini yönetir.
    // Spring Boot 2.x ve sonrası için önerilen yöntem.
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // 3. SecurityFilterChain Bean'i: HTTP güvenlik kurallarını tanımlar.
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // CSRF korumasını devre dışı bırak (JWT tabanlı API'ler için genellikle gerekli değil)
                // .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler)) // Yetkilendirme hataları için
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Oturum kullanma
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/auth/**").permitAll() // Kimlik doğrulama endpoint'lerine herkes erişebilir
                        .requestMatchers("/h2-console/**").permitAll() // H2-Console için (sadece geliştirme ortamında!)
                        // Diğer API endpoint'leri için kimlik doğrulaması gerektir
                        .anyRequest().authenticated()
                );
        // JWT filtresini ekle (JWT tabanlı kimlik doğrulaması için)
        // .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // H2-Console Frame'lerinin düzgün çalışması için ekleme (CORS/CSRF ile ilgili sorunları giderebilir)
        http.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()));

        return http.build();
    }
}