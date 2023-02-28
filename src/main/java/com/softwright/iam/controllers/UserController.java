package com.softwright.iam.controllers;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.softwright.iam.models.MessageResponse;
import com.softwright.iam.models.SignupRequest;
import com.softwright.iam.models.User;
import com.softwright.iam.repository.UserRepository;

@CrossOrigin(origins="*",maxAge=3600)
@RestController
@RequestMapping("/api/auth")
public class UserController {
	
	@Autowired
	UserRepository userRepository;
	
	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@PostMapping("/signup")
	public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest req) {
		
		if (userRepository.existsByEmail(req.getEmail())) {
			return ResponseEntity
					.badRequest()
					.body(new MessageResponse("Error: Email already in use."));
		}
		
		User user = new User(req.getEmail(), passwordEncoder().encode(req.getPassword()), req.getFirstName(), req.getLastName());
		
		try {
			userRepository.save(user);	
		}
		catch (Exception ex) {
			return ResponseEntity
					.internalServerError()
					.body(new MessageResponse("Error: Internal Server Error"));
		}
		
		return ResponseEntity.ok(new MessageResponse("User registered successfully"));
	}
	
}
