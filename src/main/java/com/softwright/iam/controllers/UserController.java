package com.softwright.iam.controllers;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.validation.Valid;

import com.auth0.jwt.exceptions.TokenExpiredException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import com.softwright.iam.models.LoginRequest;
import com.softwright.iam.models.Session;
import com.softwright.iam.models.SignupRequest;
import com.softwright.iam.models.User;
import com.softwright.iam.repository.SessionRepository;
import com.softwright.iam.repository.UserRepository;
import com.softwright.iam.security.JWTUtil;
import jakarta.servlet.http.HttpServletRequest;

@CrossOrigin(origins="*",maxAge=3600)
@Controller
@RequestMapping("/auth")
public class UserController {
	
	private final static Logger _logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

	@Value("${iam.app.jwtSecret}")
	private String secret;
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	SessionRepository sessionRepository;
	
	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	JWTUtil jwtUtil() { return new JWTUtil(); }
	
	@GetMapping("/signup")
	public String registrationPage(@RequestParam String redirectUri, @RequestParam Optional<String> error, Model model) {
		_logger.log(Level.INFO,  "GET /signup begin");
		model.addAttribute("user", new SignupRequest());
		model.addAttribute("uri", "/auth/signup?redirectUri="+redirectUri);
		_logger.log(Level.INFO,  "GET /signup --> error? "+error);
		if (!error.isEmpty()) {
			try {
				model.addAttribute("error", error.get());
			}
			catch (NoSuchElementException ex) {
				_logger.log(Level.WARNING, "GET /signup --> error value not avl.");
			}
		}
		_logger.log(Level.INFO,  "GET /signup complete");
		return "signup";
	}

	@PostMapping("/signup")
	public RedirectView registerUser(@Valid SignupRequest req, @RequestParam String redirectUri) {
		_logger.log(Level.INFO, "POST /signup begin");
		if (userRepository.existsByEmail(req.getEmail())) {
			_logger.log(Level.INFO, "POST /signup complete Error: Email already in use.");
			return new RedirectView("/auth/signup?error=Email already in use&redirectUri="+redirectUri);
		}
		User user = new User(req.getEmail(), passwordEncoder().encode(req.getPassword()), req.getFirstName(), req.getLastName());
		try {
			_logger.log(Level.INFO, "POST /signup save new user");
			userRepository.save(user);
		}
		catch (Exception ex) {
			_logger.log(Level.WARNING, "POST /signup complete Error: Exception "+ex);
			return new RedirectView("/auth/signup?error=InternalServerError&redirectUri="+redirectUri);
		}
		_logger.log(Level.INFO, "POST /signup complete Success: account created");
		return new RedirectView("/auth/login?redirectUri="+redirectUri);
	}
	
	@GetMapping("/login")
	public String loginPage(@RequestParam String redirectUri, @RequestParam Optional<String> error, Model model) {
		_logger.log(Level.INFO, "GET /login begin");
		model.addAttribute("user", new LoginRequest());
		model.addAttribute("uri", "/auth/login?redirectUri="+redirectUri);
		_logger.log(Level.INFO, "GET /login error? "+error);
		if (!error.isEmpty()) {
			try {
				model.addAttribute("error", error.get());
			}
			catch (NoSuchElementException ex) {
				_logger.log(Level.WARNING, "GET /login --> error value not avl.");
			}
		}
		_logger.log(Level.INFO, "GET /login complete");
		return "login";
	}
	
	@PostMapping("/login")
	public RedirectView loginUser(@Valid LoginRequest req, @RequestParam String redirectUri) {
		_logger.log(Level.INFO, "POST /login begin");
		try {
			User user = userRepository.findByEmail(req.getEmail());	
			if (user != null && passwordEncoder().matches(req.getPassword(), user.getHash())) {
				_logger.log(Level.INFO, "POST /login checking if session exists");
				Calendar cal = Calendar.getInstance();
				Date issued = cal.getTime();
				cal.add(Calendar.MINUTE, 60);
				Date expiry = cal.getTime();
				Date curr = new Date();
				Timestamp currTimestamp = new Timestamp(curr.getTime());
				Session existingSession = sessionRepository.findByUserIdAndLessThanExpiry(user.getId(), issued);
				if (existingSession != null && existingSession.getExpiry().compareTo(currTimestamp) > 0){
					_logger.log(Level.INFO, "POST /login session already exists");
					// use expiry from existing session
					String token = jwtUtil().generateToken(user.getEmail(), issued, existingSession.getExpiry(), secret);
					_logger.log(Level.INFO, "POST /login complete");
					return new RedirectView(redirectUri+"?token="+token);	
				} else {
					_logger.log(Level.INFO, "POST /login session does not exist.");
					Session newSession = new Session(user.getId(), issued, expiry);
					sessionRepository.save(newSession);
					String token = jwtUtil().generateToken(user.getEmail(), issued, expiry, secret);
					_logger.log(Level.INFO, "POST /login complete");
					return new RedirectView(redirectUri+"?token="+token);
				}
			} else {
				_logger.log(Level.INFO, "POST /login complete Error: User not found.");
				return new RedirectView("/auth/login?error=Invalid Username or Password&redirectUri="+redirectUri);
			}
		}
		catch (Exception ex) {
			_logger.log(Level.WARNING, "POST /login complete Error: Exception "+ex);
			return new RedirectView("/auth/login?error=InternalServerError&redirectUri="+redirectUri);
		}
	}
	
	@GetMapping("/verify")
	public ResponseEntity<?> redirect(@RequestParam String redirectUri, HttpServletRequest req) {
		_logger.log(Level.INFO, "GET /verify begin");
		try {
			String authHeader = req.getHeader("Authorization");
			boolean unauthorized = false;
			if (authHeader != null && !authHeader.isBlank() && authHeader.startsWith("Bearer ")) {
				_logger.log(Level.INFO, "GET /verify token avl.");
				String token = authHeader.split(" ")[1];
				String claim = jwtUtil().validateTokenAndReturnClaims(token, secret);
				if (claim.equals("Invalid Token") || claim.equals("Expired Token")) { unauthorized = true; } 
				else {
					_logger.log(Level.INFO, "GET /verify valid claim");
					User user = userRepository.findByEmail(claim);
					if (user != null) {
						_logger.log(Level.INFO, "GET /verify valid user");
						Date issued = new Date();
						Timestamp issuedTimestamp = new Timestamp(issued.getTime());
						Session existingSession = sessionRepository.findByUserIdAndLessThanExpiry(user.getId(), issued);
						if (existingSession == null || (existingSession != null && existingSession.getExpiry().compareTo(issuedTimestamp) <= 0)) { unauthorized = true; }
					}
					else { unauthorized = true; }
				}
			} else { unauthorized = true; }
			
			if (unauthorized) {
				_logger.log(Level.INFO, "GET /verify complete Unauthorized");
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
			} else {
				_logger.log(Level.INFO, "GET /verify complete Success");
				return ResponseEntity.status(HttpStatus.OK).body("Authorized");
			}
		}
		catch (TokenExpiredException ex) {
			_logger.log(Level.WARNING, "GET /verify complete Error: Exception "+ex);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
		}
		catch (Exception ex) {
			_logger.log(Level.WARNING, "GET /verify complete Error: Exception "+ex);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("InternalServerError");
		}
	}
}
