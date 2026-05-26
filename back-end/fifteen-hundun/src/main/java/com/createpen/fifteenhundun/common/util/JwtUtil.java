package com.createpen.fifteenhundun.common.util;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

public class JwtUtil {
    private static String SECRET_KEY = "THVvZmVuZ3d1cWkxNWh1bmR1bkp3dFBhc3N3b3JkTG9uZ0Vub3VnaFBsZWFzZQ==";
    private static long EXPIRATION_TIME = 24 * 60 * 60 * 1000;

    @Value("${jwt.secret:5LiA5LqU6aaE6aWoY3JlYXRwZW4=}")
    public void setSecretKey(String secretKey) {
        JwtUtil.SECRET_KEY = secretKey;
    }

    @Value("${jwt.expiration:86400000}")
    public void setExpirationTime(long expirationTime) {
        JwtUtil.EXPIRATION_TIME = expirationTime;
    }

    private static SecretKey getSigningKey() {
        byte[] keyBytes = Base64.getDecoder().decode(SECRET_KEY);
        return new SecretKeySpec(keyBytes, SignatureAlgorithm.HS256.getJcaName());
    }

    public static String makeToken(Map<String, Object> claims) {
        return Jwts.builder()
                .signWith(getSigningKey())
                .addClaims(claims)
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .compact();
    }

    public static String makeToken(Integer id, String username) {
        return makeToken(Map.of("id", id, "username", username));
    }

    public static Claims solveToken(String token) throws ExpiredJwtException, MalformedJwtException {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
