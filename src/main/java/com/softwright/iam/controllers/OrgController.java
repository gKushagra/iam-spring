package com.softwright.iam.controllers;

import com.softwright.iam.models.*;
import com.softwright.iam.repository.OrgRepository;
import com.softwright.iam.repository.RoleRepository;
import com.softwright.iam.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import javax.validation.Valid;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@CrossOrigin(origins="*",maxAge=3600)
@Controller
@RequestMapping("/org")
public class OrgController {

    private final static Logger _logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    @Autowired
    OrgRepository orgRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @GetMapping("/setup")
    public String getOrganizationSetupPage(@RequestParam String redirectUri, @RequestParam Optional<String> error, Model model) {
        _logger.log(Level.INFO,  "GET /setup begin");
        model.addAttribute("organization", new SetupOrgRequest());
        model.addAttribute("uri", "/org/setup?redirectUri="+redirectUri);
        model.addAttribute("loginUri", "/auth/login?redirectUri="+redirectUri);
        //"/org/setup?redirectUri=https://sso.opensourcedit.com/auth/login?redirectUri=https://admin.sso.opensourcedit.com"
        _logger.log(Level.INFO,  "GET /setup --> error? "+error);
        if (!error.isEmpty()) {
            try {
                model.addAttribute("error", error.get());
            }
            catch (NoSuchElementException ex) {
                _logger.log(Level.WARNING, "GET /setup --> error value not avl.");
            }
        }
        _logger.log(Level.INFO,  "GET /setup complete");
        return "org-setup";
    }

    @PostMapping("/setup")
    public RedirectView setupOrganization(@Valid SetupOrgRequest req, @RequestParam String redirectUri) {
        _logger.log(Level.INFO, "POST /setup begin");
        try {
            // get existing user
            User existingUser = userRepository.findByEmail(req.getEmail());
            if (existingUser != null) {
                _logger.log(Level.INFO, "POST /setup user exists");
                // check if organization exists
                Organization existingOrganization = orgRepository.findByNameAndUserId(req.getName(), existingUser.getId());
                if (existingOrganization != null) {
                    _logger.log(Level.INFO, "POST /setup org exists");
                    return new RedirectView("/org/setup?redirectUri="+redirectUri+"&error=Organization Exists");
                } else {
                    // create org
                    _logger.log(Level.INFO, "POST /setup create org");
                    Organization org = new Organization(req.getName(), existingUser.getId());
                    orgRepository.save(org);
                    // find if role exists
                    Role existingRole = roleRepository.findAdminRole(existingUser.getId(), Roles.ADMIN);
                    if (existingRole == null) {
                        _logger.log(Level.INFO, "POST /setup admin role does not exists, create admin role");
                        // create role
                        Role role = new Role(existingUser.getId(), Roles.USER);
                        roleRepository.save(role);
                    }
                }
            } else {
                _logger.log(Level.INFO, "POST /setup user does not exists");
                // create user
                User user = new User(req.getEmail(), passwordEncoder().encode(req.getPassword()), req.getFirstName(), req.getLastName());
                userRepository.save(user);
                // create org
                _logger.log(Level.INFO, "POST /setup create org");
                Organization org = new Organization(req.getName(), user.getId());
                orgRepository.save(org);
                // create role
                _logger.log(Level.INFO, "POST /setup create admin role");
                Role role = new Role(user.getId(), Roles.USER);
                roleRepository.save(role);
            }
        } catch (Exception ex) {
            _logger.log(Level.WARNING, "POST /setup complete Error: Exception "+ex);
            return new RedirectView("/org/setup?redirectUri="+redirectUri+"&error=InternalServerError");
        }
        _logger.log(Level.INFO, "POST /setup complete Success: organization created");
        return new RedirectView("/auth/login?redirectUri="+redirectUri);
    }
}
