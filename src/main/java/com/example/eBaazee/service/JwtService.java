package com.example.eBaazee.service;

import io.jsonwebtoken.Claims;
import java.util.Date;
import java.util.function.Function;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public interface JwtService {
    String generateToken(String email);

    String extractUsername(String token);

    Date extractExpiration(String token);

    <T> T extractClaim(String token, Function<Claims, T> claimsResolver);

    Boolean validateToken(String token, UserDetails userDetails);
}
