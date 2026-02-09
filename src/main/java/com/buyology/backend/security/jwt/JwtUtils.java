package com.buyology.backend.security.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {

    private static final Logger log = LoggerFactory.getLogger(JwtUtils.class);

    private final int jwtExpirationMs;
    private final String jwtSecretKey;
    private final String cookieName;

    public JwtUtils(@Value("${spring.app.jwtExpirationMs}") int jwtExpirationMs, @Value("${spring.app.jwtSecret}") String jwtSecretKey, @Value("${spring.app.cookieName}") String cookieName) {
        this.jwtExpirationMs = jwtExpirationMs;
        this.jwtSecretKey = jwtSecretKey;
        this.cookieName = cookieName;
    }

    //Getting JWT manually
    @Deprecated
    public String getJwtTokenFromHeader(HttpServletRequest httpServletRequest) {
        String authHeader = httpServletRequest.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            log.info("Retrieved the token Successfully");
            return authHeader.substring(7);
        }
        return null;
    }

    //Getting jwt from cookie, as we are implementing the cookie based
    public String getJwtFromCookie(HttpServletRequest httpServletRequest){
        log.info("Extracting the cookie at this point");
        Cookie cookie = WebUtils.getCookie(httpServletRequest,cookieName);
        return cookie != null ? cookie.getValue() : null;
    }

    //Generate the cookie(which contains the jwt) to send to client.
    //ResponseCookie is Spring’s representation of an HTTP cookie that the SERVER sends to the CLIENT.
    public ResponseCookie generateJwtCookie(UserDetails userDetails) {
        String jwt = generateTokenNameFromUserName(userDetails.getUsername());
        log.info("Generating the Cookie");
        return ResponseCookie.from(cookieName,jwt)
                .path("/api") //“Only send this cookie for URLs starting with /api”
                .maxAge(24 * 60 * 60)
                .httpOnly(false) //true → JS cannot steal JWT (protects against XSS) false → JS can read JWT (dangerous)
                .build();
    }


    public ResponseCookie getCleanJwtCookie() {
        log.info("Generating the  clean/empty Cookie");
        return ResponseCookie.from(cookieName,null)
                .path("/api") //“Only send this cookie for URLs starting with /api”
                .maxAge(0)
                .httpOnly(true)
                .build();
    }



    //Generating token from username
    public String generateTokenNameFromUserName(String userName){
        return Jwts.builder()
                .setSubject(userName)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date().getTime() + jwtExpirationMs)))
                .signWith(key())
                .compact();
    }

    //Getting the username from the jwt token
    public String getUserNameFromJwt(String token){
        return Jwts.parser()
                .verifyWith((SecretKey) key())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    //Generaate the signedKey
    public Key key(){
        return Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(jwtSecretKey)
        );
    }

    //validate jwt token
    public boolean validateJwtToken(String authToken){
        try{
            log.info("Request to validate the jwt token");
            Jwts.parser()
                    .verifyWith((SecretKey) key())
                    .build()
                    .parseSignedClaims(authToken);

            return true;
        }catch (ExpiredJwtException e) {
            log.error("JWT expired: {}",e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT format: {}",e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT: {}",e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}",e.getMessage());
        }
        return false;
    }

}
