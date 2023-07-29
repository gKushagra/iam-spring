package com.softwright.iam.controllers;

import com.softwright.iam.models.*;
import com.softwright.iam.repository.OrgRepository;
import com.softwright.iam.repository.RoleRepository;
import com.softwright.iam.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${iam.app.jwtSecret}")
    private String secret;

    @Autowired
    OrgRepository orgRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @GetMapping("/setup")
    public String getOrganizationSetupPage(@RequestParam Optional<String> error, Model model) {
        _logger.log(Level.INFO,  "GET /setup begin");
        model.addAttribute("pageTitle", "SSO Setup Organization");
        model.addAttribute("organization", new OrgSetupRequest());
        model.addAttribute("uri", "/org/setup?redirectUri="+"/org/dashboard");
        model.addAttribute("loginUri", "/auth/login?redirectUri="+"/auth/setup/callback");
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
        return "setup-org";
    }

    @PostMapping("/setup")
    public RedirectView setupOrganization(@Valid OrgSetupRequest req) {
        _logger.log(Level.INFO, "POST /setup begin");
        try {
            User existingUser = userRepository.findById(req.getUserId());
            if (existingUser != null) {
                _logger.log(Level.INFO, "POST /setup user exists");
                Organization existingOrganization = orgRepository.findByNameAndUserId(req.getName(), existingUser.getId());
                if (existingOrganization != null) {
                    _logger.log(Level.INFO, "POST /setup org exists");
                    return new RedirectView("/org/setup?error=Organization Exists");
                } else {
                    _logger.log(Level.INFO, "POST /setup create org");
                    Organization org = new Organization(req.getName(), existingUser.getId());
                    orgRepository.save(org);
                    Role existingRole = roleRepository.findAdminRole(existingUser.getId(), Roles.ADMIN);
                    if (existingRole == null) {
                        _logger.log(Level.INFO, "POST /setup admin role does not exists, create admin role");
                        Role role = new Role(existingUser.getId(), Roles.USER);
                        roleRepository.save(role);
                    }
                }
            } else {
                _logger.log(Level.INFO, "POST /setup user does not exists");
                return new RedirectView("/org/setup?error=Bad Request");
            }
        } catch (Exception ex) {
            _logger.log(Level.WARNING, "POST /setup complete Error: Exception "+ex);
            return new RedirectView("/org/setup?error=InternalServerError");
        }
        _logger.log(Level.INFO, "POST /setup complete Success: organization created");
        return new RedirectView("/org/dashboard");
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        _logger.log(Level.INFO,  "GET /dashboard begin");
        model.addAttribute("pageTitle", "SSO Dashboard");
        _logger.log(Level.INFO,  "GET /dashboard complete");
        return "dashboard";
    }
}
