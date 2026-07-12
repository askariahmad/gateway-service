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
            userRepository.count().subscribe(count -> {
                if (count == 0) {
                    logger.info("Seeding test users into the database...");
                    
                    User admin = new User();
                    admin.setUsername("admin");
                    admin.setPassword(passwordEncoder.encode("admin123"));
                    admin.setTenantId("tenant-a");
                    admin.setRole("ADMIN");

                    User user1 = new User();
                    user1.setUsername("user1");
                    user1.setPassword(passwordEncoder.encode("password"));
                    user1.setTenantId("tenant-b");
                    user1.setRole("USER");

                    userRepository.saveAll(Flux.just(admin, user1)).subscribe(
                        user -> logger.info("Seeded user: " + user.getUsername())
                    );
                } else {
                    logger.info("Users already exist, skipping seeding.");
                }
            });
        };
    }
}
