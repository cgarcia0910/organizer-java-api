package com.example.api.auth;
import com.example.api.auth.entity.AppUser;
import com.example.api.auth.repository.AppUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;
@Component
public class DataInitializer implements CommandLineRunner {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        if (appUserRepository.findByEmail("ana@empresa-a.com").isEmpty()) {
            AppUser ana = new AppUser();

            ana.setEmail("ana@empresa-a.com");
            ana.setPassword(passwordEncoder.encode("Ana123!"));
            ana.setTenantId(1L);
            ana.setRole("ADMIN");

            appUserRepository.save(ana);
        }

        if (appUserRepository.findByEmail("juan@empresa-b.com").isEmpty()) {
            AppUser juan = new AppUser();

            juan.setEmail("juan@empresa-b.com");
            juan.setPassword(passwordEncoder.encode("Juan123!"));
            juan.setTenantId(2L);
            juan.setRole("USER");

            appUserRepository.save(juan);
        }
    }
}
