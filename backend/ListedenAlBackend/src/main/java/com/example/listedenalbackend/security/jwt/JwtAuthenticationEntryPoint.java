package com.example.listedenalbackend.security.jwt;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationEntryPoint.class);

    @Override
    public void commence(HttpServletRequest httpServletRequest,
                         HttpServletResponse httpServletResponse,
                         AuthenticationException e) throws IOException, ServletException {
        // Kimlik doğrulama hatasını logla. Üretim ortamında ham hata mesajlarını doğrudan istemciye göndermek yerine
        // daha genel bir mesaj tercih etmek genellikle daha iyidir.
        logger.error("Yetkisiz erişim denemesi. Mesaj: {}", e.getMessage());

        // 401 Yetkisiz (Unauthorized) hata yanıtı gönder.
        // İstemciye dönen hata mesajını daha açıklayıcı hale getirebilir veya JSON formatında hata detayları döndürebilirsiniz.
        httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Yetkisiz: " + e.getMessage());
    }
}