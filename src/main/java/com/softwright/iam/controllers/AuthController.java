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
import com.softwright.iam.models.*;
import com.softwright.iam.utils.Email;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import com.softwright.iam.repository.SessionRepository;
import com.softwright.iam.repository.UserRepository;
import com.softwright.iam.security.JWTUtil;
import jakarta.servlet.http.HttpServletRequest;

@CrossOrigin(origins="*",maxAge=3600)
@Controller
@RequestMapping("/auth")
public class AuthController {
	
	private final static Logger _logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

	@Value("${iam.app.jwtSecret}")
	private String secret;

	@Value("${iam.app.mail.uri}")
	private String uri;

	@Value("${iam.app.mail.from}")
	private String from;

	@Value("${iam.app.domain}")
	private String domain;
	
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

	@Bean
	Email emailUtil() { return new Email(); }
	
	@GetMapping("/signup")
	public String registrationPage(@RequestParam String redirectUri, @RequestParam Optional<String> error, Model model) {
		_logger.log(Level.INFO,  "GET /signup begin");
		model.addAttribute("pageTitle", "SSO Sign Up");
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
		model.addAttribute("pageTitle", "SSO Login");
		model.addAttribute("user", new LoginRequest());
		model.addAttribute("uri", "/auth/login?redirectUri="+redirectUri);
		model.addAttribute("resetUri", "/auth/reset?redirectUri="+redirectUri+"&isNew=true");
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
			if (user == null) {
				_logger.log(Level.INFO, "POST /login complete Info: User not found");
				return new RedirectView("/auth/signup?redirectUri="+redirectUri);
			}
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
				_logger.log(Level.INFO, "POST /login complete Error: Incorrect Username or Password");
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

	@GetMapping("/reset")
	public String requestResetLink(@RequestParam String redirectUri, @RequestParam Optional<String> message, @RequestParam boolean isNew, Model model) {
		_logger.log(Level.INFO,  "GET /reset begin");
		String msg = "Please enter your email!";
		model.addAttribute("pageTitle", "SSO Reset");
		model.addAttribute("user", new ResetRequest());
		model.addAttribute("uri", "/auth/reset?redirectUri="+redirectUri);
		if (!message.isEmpty()) {
			try {
				model.addAttribute("message", message.get());
			} catch (NoSuchElementException ex) {
				_logger.log(Level.WARNING, "GET /login --> error value not avl.");
				model.addAttribute("message", msg);
			}
		} else {
			model.addAttribute("message", msg);
		}
		if(isNew) {
			model.addAttribute("newRequest", true);
		} else {
			model.addAttribute("newRequest", false);
		}
		_logger.log(Level.INFO,  "GET /reset complete");
		return "reset";
	}

	@PostMapping("/reset")
	public RedirectView sendResetLink(@Valid ResetRequest req, @RequestParam String redirectUri, HttpServletRequest request) {
		_logger.log(Level.INFO, "POST /reset begin");
		try {
			User user = userRepository.findByEmail(req.getEmail());
			if (user != null) {
				_logger.log(Level.INFO, "POST /reset send email");
				Calendar cal = Calendar.getInstance();
				Date issued = cal.getTime();
				cal.add(Calendar.MINUTE, 5);
				Date expiry = cal.getTime();
				Session session = new Session(user.getId(), issued, expiry);
				session.setResetSession(true);
				sessionRepository.save(session);
				String resetLink = domain + "/auth/reset-link?redirectUri=" + redirectUri + "&id=" + session.getId();
				emailUtil().send(uri, from, req.getEmail(), EmailTemplate.RESET_LINK, Optional.of(resetLink), Optional.empty());
			} else {
				_logger.log(Level.INFO, "POST /reset complete Error: User not found.");
			}
		}
		catch (Exception ex) {
			_logger.log(Level.WARNING, "POST /reset complete Error: Exception "+ex);
		}
		return new RedirectView("/auth/reset?redirectUri="+redirectUri+"&isNew=false&message=You will receive an email with the reset link if an account is associated with this email.");
	}

	@GetMapping("/reset-link")
	public String getResetPage(@RequestParam String redirectUri, @RequestParam UUID id,  @RequestParam Optional<String> error, Model model) {
		_logger.log(Level.INFO,  "GET /reset-link begin");
		model.addAttribute("user", new ProcessResetRequest());
		model.addAttribute("uri", "/auth/reset-link?redirectUri="+redirectUri+"&id="+id);
		_logger.log(Level.INFO,  "GET /reset-link --> error? "+error);
		if (!error.isEmpty()) {
			try {
				model.addAttribute("error", error.get());
			}
			catch (NoSuchElementException ex) {
				_logger.log(Level.WARNING, "GET /reset-link --> error value not avl.");
			}
		}
		_logger.log(Level.INFO,  "GET /reset-link complete");
		return "reset-link";
	}

	@PostMapping("/reset-link")
	public RedirectView processResetRequest(@Valid ProcessResetRequest req, @RequestParam String redirectUri, @RequestParam UUID id) {
		_logger.log(Level.INFO, "POST /reset-link begin");
		boolean isInvalid = false;
		try {
			Date issued = new Date();
			Timestamp issuedTimestamp = new Timestamp(issued.getTime());
			Session existingSession = sessionRepository.findByIdAndLessThanExpiry(id, issued);
			if (existingSession == null || (existingSession != null && existingSession.isResetSession() && !existingSession.isResetProcessed()
					&& existingSession.getExpiry().compareTo(issuedTimestamp) <= 0)) { isInvalid = true; }
			else {
				_logger.log(Level.INFO, "POST /reset-link session exists");
				User user = userRepository.findById(existingSession.getUserId());
				if (user != null) {
					_logger.log(Level.INFO, "POST /reset-link user exists");
					userRepository.updateHashAndLastUpdated(passwordEncoder().encode(req.getPassword()), new Date(), user.getId());
					sessionRepository.updateResetProcessed(true, existingSession.getId());
					String notificationBody = "Your password reset request was processed successfully.";
					emailUtil().send(uri, from, user.getEmail(), EmailTemplate.NOTIFICATION, Optional.empty(), Optional.of(notificationBody));
				} else { isInvalid = true; }
			}
		}
		catch (Exception ex) {
			_logger.log(Level.WARNING, "POST /reset complete Error: Exception "+ex);
			return new RedirectView("/auth/reset-link?redirectUri="+redirectUri+"&id="+id+"&error=Unable to process this request. Please try again later!");
		}
		if (isInvalid) {
			_logger.log(Level.INFO, "POST /reset complete Error: Invalid Request");
			return new RedirectView("/auth/reset-link?redirectUri="+redirectUri+"&id="+id+"&error=Invalid Request");
		} else {
			_logger.log(Level.INFO, "POST /reset-link complete");
			return new RedirectView("/auth/login?redirectUri="+redirectUri);
		}
	}

	@GetMapping("/{page}/callback")
	public String authCallback(@PathVariable("page") String page, @RequestParam String token, Model model) {
		_logger.log(Level.INFO,  "GET /callback begin");
		model.addAttribute("pageTitle", "SSO Callback");
		boolean checkPage = page.equalsIgnoreCase(Pages.SETUP.toString())
				|| page.equalsIgnoreCase(Pages.DASHBOARD.toString());
		if (checkPage == false) {
			model.addAttribute("error", "404");
			model.addAttribute("redirectError", "/auth/error");
			_logger.log(Level.INFO,  "GET /callback complete - Page Not Found");
			return "callback";
		}
		model.addAttribute("page", page);
		model.addAttribute("redirectPage", "/org/" + page);
		model.addAttribute("redirectLogin", "/auth/login?redirectUri=/org/" + page + "/callback");
		_logger.log(Level.INFO,  "GET /callback complete");
		return "callback";
	}

	@GetMapping("/error")
	public String errorPage(Model model) {
		_logger.log(Level.INFO,  "GET /error begin");
		model.addAttribute("pageTitle", "SSO Error");
		_logger.log(Level.INFO,  "GET /error complete");
		return "error";
	}
}
