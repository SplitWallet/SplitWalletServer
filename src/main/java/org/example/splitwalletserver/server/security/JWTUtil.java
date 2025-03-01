package org.example.splitwalletserver.server.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.Date;


@Component
public class JWTUtil {

    @Value("${jwt_secret}")
    private String secretKey;

    public String generateToken(String name){
        Date expirationDate = Date.from(ZonedDateTime.now().plusMinutes(60).toInstant());

        return JWT.create()
                .withSubject("User details")
                .withClaim("name", name)
                .withIssuedAt(new Date())
                .withIssuer("splitWalletKpoAndApc")
                .withExpiresAt(expirationDate)
                .sign(Algorithm.HMAC256(secretKey));
    }

    public String validateTokenRetrieveClaim(String token) throws JWTVerificationException {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secretKey))
                .withSubject("User details")
                .withIssuer("splitWalletKpoAndApc")
                .build();
        DecodedJWT jwt = verifier.verify(token);
        return jwt.getClaim("name").asString();

    }

    public boolean isTokenValid(String token) {
        try {
            validateTokenRetrieveClaim(token);
            return true;
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            Date expirationDate = jwt.getExpiresAt();
            return expirationDate.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

}
