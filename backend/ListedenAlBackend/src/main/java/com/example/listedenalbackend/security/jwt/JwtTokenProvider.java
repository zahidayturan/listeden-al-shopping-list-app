package com.example.listedenalbackend.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.io.Decoders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${app.jwtSecret}")
    private String jwtSecret; // Uygulama özelliklerinde tanımlanacak gizli anahtar

    @Value("${app.jwtExpirationMs}")
    private int jwtExpirationMs; // Uygulama özelliklerinde tanımlanacak token geçerlilik süresi (milisaniye)

    // JWT gizli anahtarını Base64'ten çözerek bir Key nesnesi oluşturur.
    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    // Kimlik doğrulama nesnesinden JWT oluşturur.
    public String generateToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .setSubject(userPrincipal.getUsername()) // Token'ın konusu (username)
                .setIssuedAt(new Date()) // Oluşturulma zamanı
                .setExpiration(expiryDate) // Bitiş zamanı
                .signWith(key(), SignatureAlgorithm.HS512) // İmzalama algoritması ve anahtarı
                .compact(); // JWT'yi sıkıştır ve String olarak döndür
    }

    // JWT'den kullanıcı adını alır.
    public String getUsernameFromJwtToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key()) // İmzalama anahtarını ayarla
                .build()
                .parseClaimsJws(token) // Token'ı ayrıştır
                .getBody() // Payload'ı al
                .getSubject(); // Kullanıcı adını al
    }

    // JWT'nin geçerliliğini doğrular.
    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parse(authToken);
            return true;
        } catch (MalformedJwtException ex) {
            logger.error("Invalid JWT token: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            logger.error("Expired JWT token: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported JWT token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty: {}", ex.getMessage());
        }
        return false;
    }
}