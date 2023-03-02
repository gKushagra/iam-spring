package com.softwright.iam.security;

import java.util.Calendar;
import java.util.Date;
import org.springframework.stereotype.Component;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

@Component
public class JWTUtil {

	public String generateToken(String email, Date issued, Date expiry, String secret) throws IllegalArgumentException, JWTCreationException {
		return JWT.create()
				.withSubject("User Details")
				.withClaim("email", email)
				.withIssuedAt(issued)
				.withExpiresAt(expiry)
				.withIssuer("com.softwright.iam")
				.sign(Algorithm.HMAC256(secret));
	}
	
	public String validateTokenAndReturnClaims(String token, String secret) throws JWTVerificationException {
		
		JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret))
				.withSubject("User Details")
				.withIssuer("com.softwright.iam")
				.build();
		
		DecodedJWT jwt = verifier.verify(token);
		
		Calendar cal = Calendar.getInstance();
		Date curr = cal.getTime();
		Date expiry = jwt.getExpiresAt();
		
		if (curr.compareTo(expiry) >= 0) {
			return "Expired Token";
		}
		
		String email = jwt.getClaim("email").asString();
		
		if (email.isEmpty()) {
			return "Invalid Token";
		}
		
		return email;
	}
}
