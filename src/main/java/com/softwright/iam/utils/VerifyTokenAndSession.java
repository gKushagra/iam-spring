package com.softwright.iam.utils;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.softwright.iam.models.Session;
import com.softwright.iam.models.User;
import com.softwright.iam.repository.SessionRepository;
import com.softwright.iam.repository.UserRepository;
import com.softwright.iam.security.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class VerifyTokenAndSession {
    private final static Logger _logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    @Value("${iam.app.jwtSecret}")
    private String secret;
    @Autowired
    SessionRepository sessionRepository;
    @Autowired
    UserRepository userRepository;
    @Bean
    JWTUtil tokenUtil() { return new JWTUtil(); }

    public boolean isValidTokenAndSession(String token) {
        try {
            boolean unauthorized = false;
            _logger.log(Level.INFO, "isValidTokenAndSession token avl.");
            String claim = tokenUtil().validateTokenAndReturnClaims(token, secret);
            if (claim.equals("Invalid Token") || claim.equals("Expired Token")) { unauthorized = true; }
            else {
                _logger.log(Level.INFO, "isValidTokenAndSession valid claim");
                User user = userRepository.findByEmail(claim);
                if (user != null) {
                    _logger.log(Level.INFO, "isValidTokenAndSession valid user");
                    Date issued = new Date();
                    Timestamp issuedTimestamp = new Timestamp(issued.getTime());
                    Session existingSession = sessionRepository.findByUserIdAndLessThanExpiry(user.getId(), issued);
                    if (existingSession == null || (existingSession != null && existingSession.getExpiry().compareTo(issuedTimestamp) <= 0)) { unauthorized = true; }
                }
                else { unauthorized = true; }
            }

            if (unauthorized) {
                _logger.log(Level.INFO, "isValidTokenAndSession complete Unauthorized");
                return false;
            } else {
                _logger.log(Level.INFO, "isValidTokenAndSession complete Success");
                return true;
            }
        }
        catch (TokenExpiredException ex) {
            _logger.log(Level.WARNING, "isValidTokenAndSession complete Error: Exception "+ex);
            return false;
        }
        catch (Exception ex) {
            _logger.log(Level.WARNING, "isValidTokenAndSession complete Error: Exception " + ex);
            return false;
        }
    }
}
