package com.softwright.iam.controllers;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.softwright.iam.models.Session;
import com.softwright.iam.models.User;
import com.softwright.iam.models.UserDetails;
import com.softwright.iam.repository.SessionRepository;
import com.softwright.iam.repository.UserRepository;
import com.softwright.iam.security.JWTUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

@CrossOrigin(origins="*",maxAge=3600)
@Controller
@RequestMapping("/user")
public class UserController {

    private final static Logger _logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    @Value("${iam.app.jwtSecret}")
    private String secret;

    @Autowired
    UserRepository userRepository;

    @Autowired
    SessionRepository sessionRepository;

    @Bean
    JWTUtil jwt() { return new JWTUtil(); }

    @GetMapping("/")
    public ResponseEntity<?> getUserDetails(HttpServletRequest req) {
        _logger.log(Level.INFO, "GET /user begin");
        try {
            User user = new User();
            String authHeader = req.getHeader("Authorization");
            boolean unauthorized = false;
            if (authHeader != null && !authHeader.isBlank() && authHeader.startsWith("Bearer ")) {
                _logger.log(Level.INFO, "GET /user token avl.");
                String token = authHeader.split(" ")[1];
                String claim = jwt().validateTokenAndReturnClaims(token, secret);
                if (claim.equals("Invalid Token") || claim.equals("Expired Token")) { unauthorized = true; }
                else {
                    _logger.log(Level.INFO, "GET /user valid claim");
                    user = userRepository.findByEmail(claim);
                    if (user != null) {
                        _logger.log(Level.INFO, "GET /user valid user");
                        Date issued = new Date();
                        Timestamp issuedTimestamp = new Timestamp(issued.getTime());
                        Session existingSession = sessionRepository.findByUserIdAndLessThanExpiry(user.getId(), issued);
                        if (existingSession == null || (existingSession != null && existingSession.getExpiry().compareTo(issuedTimestamp) <= 0)) { unauthorized = true; }
                    }
                    else { unauthorized = true; }
                }
            } else { unauthorized = true; }

            if (unauthorized) {
                _logger.log(Level.INFO, "GET /user complete Unauthorized");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
            } else {
                _logger.log(Level.INFO, "GET /user complete Success");
                UserDetails userDetails = new UserDetails(user);
                return ResponseEntity.status(HttpStatus.OK).body(userDetails);
            }
        }
        catch (TokenExpiredException ex) {
            _logger.log(Level.WARNING, "GET /user complete Error: Exception "+ex);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        catch (Exception ex) {
            _logger.log(Level.WARNING, "GET /user complete Error: Exception "+ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("InternalServerError");
        }
    }

    @PutMapping("/")
    public ResponseEntity<?> updateUserDetails(@RequestBody UserDetails userDetails, HttpServletRequest req) {
        _logger.log(Level.INFO, "PUT /user begin");
        if (userDetails.userId == null || userDetails.email == null ||
        userDetails.firstName == null || userDetails.lastName == null) {
            _logger.log(Level.INFO, "PUT /user bad request");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Bad Request");
        }
        _logger.log(Level.INFO, "PUT /user valid request");
        try {
            String authHeader = req.getHeader("Authorization");
            boolean unauthorized = false;
            if (authHeader != null && !authHeader.isBlank() && authHeader.startsWith("Bearer ")) {
                _logger.log(Level.INFO, "PUT /user token avl.");
                String token = authHeader.split(" ")[1];
                String claim = jwt().validateTokenAndReturnClaims(token, secret);
                if (claim.equals("Invalid Token") || claim.equals("Expired Token")) { unauthorized = true; }
                else {
                    _logger.log(Level.INFO, "PUT /user valid claim");
                    Date issued = new Date();
                    Timestamp issuedTimestamp = new Timestamp(issued.getTime());
                    Session existingSession = sessionRepository.findByUserIdAndLessThanExpiry(userDetails.getUserId(), issued);
                    if (existingSession == null || (existingSession != null && existingSession.getExpiry().compareTo(issuedTimestamp) <= 0)) { unauthorized = true; }
                }
            } else { unauthorized = true; }

            if (unauthorized) {
                _logger.log(Level.INFO, "PUT /user complete Unauthorized");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
            } else {
                _logger.log(Level.INFO, "PUT /user update user");
                Date lastUpdated = new Date();
                userRepository.updateUser(userDetails.getEmail(), userDetails.getFirstName(), userDetails.getLastName(), lastUpdated, userDetails.getUserId());
                _logger.log(Level.INFO, "PUT /user update Success");
                return ResponseEntity.status(HttpStatus.OK).body(userDetails);
            }
        }
        catch (TokenExpiredException ex) {
            _logger.log(Level.WARNING, "PUT /user complete Error: Exception "+ex);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        catch (Exception ex) {
            _logger.log(Level.WARNING, "PUT /user complete Error: Exception "+ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("InternalServerError");
        }
    }
}
