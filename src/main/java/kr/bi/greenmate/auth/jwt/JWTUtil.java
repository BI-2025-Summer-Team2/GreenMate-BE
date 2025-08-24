package kr.bi.greenmate.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import kr.bi.greenmate.config.properties.JwtProperties;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static io.jsonwebtoken.security.Keys.hmacShaKeyFor;

@Component
public class JWTUtil {
    private final SecretKey secretKey;

    public JWTUtil(JwtProperties jwtProperties) {
        this.secretKey = hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String getEmail(String token) {
        return parseClaims(token)
                .get("email", String.class);
    }

    public boolean isExpired(String token) {
        return parseClaims(token)
                .getExpiration()
                .before(new Date());
    }

    public Claims parseClaims(String token) {
        return Jwts.parser().verifyWith(secretKey).build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String createJwt(String email, Long expiredMs) {
        return Jwts.builder()
                .claim("email", email)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(secretKey)
                .compact();
    }
}
