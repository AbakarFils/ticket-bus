package com.ticketbus.config;

import com.ticketbus.entity.AppUser;
import com.ticketbus.entity.UserRole;
import com.ticketbus.repository.AppUserRepository;
import com.ticketbus.repository.SigningKeyPairRepository;
import com.ticketbus.service.CryptoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final AppUserRepository appUserRepository;
    private final SigningKeyPairRepository signingKeyPairRepository;
    private final CryptoService cryptoService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (appUserRepository.count() == 0) {
            AppUser admin = AppUser.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .role(UserRole.ADMIN)
                    .enabled(true)
                    .build();
            appUserRepository.save(admin);
            log.info("Created default admin user");
        }

        if (signingKeyPairRepository.count() == 0) {
            cryptoService.generateKeyPair();
            log.info("Generated initial signing key pair");
        }
    }
}
