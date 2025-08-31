package kr.bi.greenmate.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import kr.bi.greenmate.config.properties.JwtProperties;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static io.jsonwebtoken.security.Keys.hmacShaKeyFor;

@Component
public class JWTUtil {
    private final SecretKey secretKey;

    public JWTUtil(JwtProperties jwtProperties) {
        this.secretKey = hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String getClaim(String token, String claimName) {
        return parseClaims(token)
                .get(claimName, String.class);
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

    public String createAccessToken(String userId, String userEmail, String userNickname, Long expiredMs) {
        return Jwts.builder()
                .claim("user_id", userId)
                .claim("user_email", userEmail)
                .claim("user_nickname", userNickname)
                .issuedAt(convertLocalDateTimeToDate(LocalDateTime.now()))
                .expiration(convertLocalDateTimeToDate(getExpiredAt(expiredMs)))
                .signWith(secretKey)
                .compact();
    }

    public String createRefreshToken(String userId, Long expiredMs){
        return Jwts.builder()
                .claim("user_id", userId)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(secretKey)
                .compact();
    }

    public LocalDateTime getExpiredAt(Long expiredMs){
        return LocalDateTime.now().plus(Duration.ofMillis(expiredMs));
    }

    public Date convertLocalDateTimeToDate(LocalDateTime time){
        return Date.from(time.atZone(ZoneId.systemDefault()).toInstant());
    }
}
