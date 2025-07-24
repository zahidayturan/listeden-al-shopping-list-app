package com.example.listedenalbackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity // Web güvenliğini etkinleştirir
public class SecurityConfig {

    // Güvenlik filtre zincirini yapılandırır
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // CSRF korumasını devre dışı bırakır (API'ler için yaygın)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/users/**").permitAll() // /api/users altındaki tüm isteklere izin ver
                        .anyRequest().authenticated() // Diğer tüm isteklere kimlik doğrulama zorunluluğu getir
                )
                .httpBasic(org.springframework.security.config.Customizer.withDefaults()); // Temel HTTP kimlik doğrulamasını etkinleştirir
        // .formLogin(org.springframework.security.config.Customizer.withDefaults()); // Form tabanlı login'i etkinleştirir
        return http.build();
    }

    // Bellekte kullanıcı tanımlama (test veya basit durumlar için)
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails user = User.builder()
                .username("admin") // Kullanıcı adı
                .password(passwordEncoder.encode("password")) // Şifre (BCrypt ile kodlanmış)
                .roles("ADMIN") // Rol
                .build();
        return new InMemoryUserDetailsManager(user);
    }

    // Şifre kodlayıcı
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}