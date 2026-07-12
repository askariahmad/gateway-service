package com.devops.gateway.controller;

import com.devops.gateway.model.User;
import com.devops.gateway.repository.UserRepository;
import com.devops.gateway.util.JwtUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(JwtUtil jwtUtil, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<AuthResponse>> login(@RequestBody AuthRequest request) {
        return userRepository.findByUsername(request.getUsername())
            .filter(user -> passwordEncoder.matches(request.getPassword(), user.getPassword()))
            .map(user -> {
                String token = jwtUtil.generateToken(user.getUsername(), user.getTenantId());
                return ResponseEntity.ok(new AuthResponse(token));
            })
            .defaultIfEmpty(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @PostMapping("/entra-login")
    public Mono<ResponseEntity<AuthResponse>> entraLogin(@RequestBody EntraAuthRequest request) {
        // In a real production scenario, this endpoint would validate the MSAL token against Azure AD.
        // For development, we extract the username (email) and provision/fetch the tenant.
        
        String email = request.getUsername();
        if (email == null || email.isEmpty()) {
             return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).build());
        }
        
        // Mock lookup: assume their tenant is derived from their email domain, e.g. company.com
        String tenantId = extractDomain(email) + "-tenant";
        
        // We could look them up in DB, or just issue the token if we trust the entra token
        String token = jwtUtil.generateToken(email, tenantId);
        return Mono.just(ResponseEntity.ok(new AuthResponse(token)));
    }
    
    private String extractDomain(String email) {
        if (email.contains("@")) {
            return email.substring(email.indexOf("@") + 1).replace(".", "-");
        }
        return "default-tenant";
    }

    @Data
    public static class AuthRequest {
        private String username;
        private String password;
    }
    
    @Data
    public static class EntraAuthRequest {
        private String entraToken;
        private String username; // email from msal
    }

    @Data
    public static class AuthResponse {
        private String token;
        public AuthResponse(String token) {
            this.token = token;
        }
    }
}
