package com.example.listedenalbackend.security.jwt;

import com.example.listedenalbackend.security.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // İstekten JWT'yi al
            String jwt = getJwtFromRequest(request);

            // JWT mevcut ve geçerliyse işleme devam et
            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                // JWT'den kullanıcı adını al
                String username = tokenProvider.getUsernameFromJwtToken(jwt);

                // Kullanıcı detaylarını yükle ve kimlik doğrulamasını SecurityContext'e ayarla
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
                // Kimlik doğrulama nesnesi oluşturulurken yetkiler (authorities) açıkça belirtilmelidir.
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Kimlik doğrulamasını Spring Security bağlamına (context) ayarla
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            // Güvenlik bağlamında kullanıcı kimlik doğrulaması ayarlanamadı hatasını güvenli bir şekilde logla
            // Hassas bilgilerin açığa çıkmasını önlemek için sadece hata mesajını logla
            logger.error("Kullanıcı kimlik doğrulaması güvenlik bağlamında ayarlanamadı: {}", ex.getMessage());
            // İsteğe bağlı olarak, kimlik doğrulama başarısız olursa burada 401 yanıtı gönderebilirsiniz,
            // ancak genellikle JwtAuthenticationEntryPoint genel yetkisiz erişim hatalarını yönetir.
        }

        // Filtre zincirinde bir sonraki filtreye geç
        filterChain.doFilter(request, response);
    }

    /**
     * İsteğin Authorization başlığından JWT'yi çıkarır.
     * Beklenen format: "Bearer <token>"
     * @param request HttpServletRequest nesnesi
     * @return Çıkarılan JWT dizesi veya bulunamazsa/geçersiz formatta ise null
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        // "Bearer " önekiyle başlayıp başlamadığını kontrol et
        // StringUtils.startsWithIgnoreCase daha sağlam bir kontrol sağlar.
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            // "Bearer " kısmını atlamak için 7. indeksten itibaren substring al
            return bearerToken.substring(7);
        }
        return null;
    }
}