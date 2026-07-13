package com.devops.gateway.config;

import com.devops.gateway.model.User;
import com.devops.gateway.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Flux;

@Configuration
public class DataSeeder {
    
    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);

    @Bean
    public CommandLineRunner seedDatabase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            userRepository.deleteAll().thenMany(
                Flux.just(
                    createUser("sysadmin@devops.com", "devops-com-tenant", "ROLE_SYSTEM_ADMIN", passwordEncoder),
                    createUser("tenantadmin@devops.com", "devops-com-tenant", "ROLE_TENANT_ADMIN", passwordEncoder),
                    createUser("security@devops.com", "devops-com-tenant", "ROLE_SECURITY_ENGINEER", passwordEncoder),
                    createUser("dev@devops.com", "devops-com-tenant", "ROLE_DEVELOPER_VIEWER", passwordEncoder),
                    createUser("realuser@devops.com", "real-tenant", "ROLE_SYSTEM_ADMIN", passwordEncoder)
                )
                .flatMap(userRepository::save)
            ).subscribe(
                user -> logger.info("Seeded user: " + user.getUsername()),
                err -> logger.error("Error seeding users", err),
                () -> logger.info("Test users successfully seeded into database!")
            );
        };
    }

    private User createUser(String email, String tenantId, String role, PasswordEncoder passwordEncoder) {
        User u = new User();
        u.setUsername(email);
        u.setEmail(email);
        u.setPassword(passwordEncoder.encode("password123"));
        u.setTenantId(tenantId);
        u.setRole(role);
        return u;
    }
}
