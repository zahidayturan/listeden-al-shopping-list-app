// Bu sınıf, JSON Web Token (JWT) oluşturma, doğrulama ve token'dan kullanıcı adını çıkarma işlemlerini sağlar.
// Uygulama genelinde JWT ile ilgili tüm operasyonları merkezi bir şekilde yönetir.
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
    private String jwtSecret;
    @Value("${app.jwtExpirationMs}")
    private int jwtExpirationMs;

    // JWT gizli anahtarını Base64'ten çözerek bir Key nesnesi oluşturur.
    // Spring'in @Value enjeksiyonu ve @Component yapısı ile bir kez başlatma sırasında gerçekleşecektir.
    private Key key() {
        // Güvenlik: jwtSecret'ın yeterince uzun ve karmaşık olduğundan emin olun.
        // En az 256 bit (32 karakter) uzunluğunda olması önerilir.
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    /**
     * Kimlik doğrulama nesnesinden (Authentication) bir JWT oluşturur.
     * Token'ın konusu olarak kullanıcının username'ini içerir.
     * @param authentication Kimlik doğrulama nesnesi.
     * @return Oluşturulan JWT String'i.
     */
    public String generateToken(Authentication authentication) {
        // Authentication nesnesinden UserDetails'i alıyoruz.
        // Bu, CustomUserDetailsService tarafından döndürülen Spring Security User nesnesidir.
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        // Token'ın oluşturulma ve bitiş zamanlarını belirliyoruz.
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        // JWT oluşturma süreci:
        // .setSubject(): Token'ın ana konusu, genellikle kullanıcı adı veya ID.
        // .setIssuedAt(): Token'ın ne zaman oluşturulduğu.
        // .setExpiration(): Token'ın ne zaman geçerliliğini yitireceği.
        // .signWith(): Token'ı belirtilen anahtar ve algoritma ile imzalar.
        // .compact(): Oluşturulan JWT'yi sıkıştırılmış String formatına dönüştürür.
        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Verilen JWT'den kullanıcının adını (subject) çıkarır.
     * @param token JWT String'i.
     * @return Token'ın konusu olan kullanıcı adı.
     * @throws JwtException Token ayrıştırma veya doğrulama sırasında bir hata oluşursa.
     */
    public String getUsernameFromJwtToken(String token) {
        // JWT'yi ayrıştırma ve doğrulama için gerekli anahtarı ayarlar.
        // parseClaimsJws(): Token'ı ayrıştırır ve bir Jws<Claims> nesnesi döndürür.
        // getBody(): Token'ın payload (Claims) kısmını alır.
        // getSubject(): Payload'dan konuyu (username) alır.
        return Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /**
     * Verilen JWT'nin geçerliliğini doğrular.
     * Token'ın süresinin dolup dolmadığını, bozuk olup olmadığını vb. kontrol eder.
     * @param authToken Doğrulanacak JWT String'i.
     * @return Token geçerliyse true, aksi takdirde false.
     */
    public boolean validateToken(String authToken) {
        try {
            // Token'ı ayrıştırmaya çalışarak geçerliliğini kontrol eder.
            // Herhangi bir hata durumunda ilgili catch bloğuna düşer.
            Jwts.parserBuilder().setSigningKey(key()).build().parse(authToken);
            return true;
        } catch (MalformedJwtException ex) {
            // Güvenlik: Geçersiz biçimli JWT. Saldırı girişimi veya hatalı token üretimi olabilir.
            logger.error("Invalid JWT token: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            // Güvenlik: Süresi dolmuş JWT. Yeniden kimlik doğrulama gereklidir.
            logger.error("Expired JWT token: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            // Güvenlik: Desteklenmeyen JWT. Farklı bir format veya algoritma kullanılmış olabilir.
            logger.error("Unsupported JWT token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            // Güvenlik: JWT claimleri boş veya geçersiz.
            logger.error("JWT claims string is empty: {}", ex.getMessage());
        }
        // Token geçerli değilse false döner.
        return false;
    }
}
