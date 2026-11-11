package com.example.api.auth;

import com.example.api.auth.entity.AppUser;
import com.example.api.auth.repository.AppUserRepository;
import com.example.api.auth.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Siembra datos de desarrollo: dos tenants y tres usuarios con roles.
 * Solo se ejecuta con el perfil "dev" y requiere la propiedad app.seed.password.
 */
@Component
@Profile("dev")
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private static final UUID TENANT_A = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID TENANT_B = UUID.fromString("22222222-2222-2222-2222-222222222222");

    private final AppUserRepository appUserRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;
    private final String seedPassword;

    public DataInitializer(
            AppUserRepository appUserRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            JdbcTemplate jdbcTemplate,
            @Value("${app.seed.password:}") String seedPassword) {
        this.appUserRepository = appUserRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
        this.seedPassword = seedPassword;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (seedPassword == null || seedPassword.isBlank()) {
            log.warn("app.seed.password (SEED_PASSWORD) no definida: no se siembran usuarios de desarrollo");
            return;
        }

        createTenant(TENANT_A, "Tenant A");
        createTenant(TENANT_B, "Tenant B");

        createUser("admin@tenant-a.example", TENANT_A, Roles.CATALOG_ADMIN);
        createUser("editor@tenant-a.example", TENANT_A, Roles.CATALOG_EDITOR);
        createUser("admin@tenant-b.example", TENANT_B, Roles.CATALOG_ADMIN);
    }

    private void createTenant(UUID id, String name) {
        jdbcTemplate.update(
                "INSERT INTO public.tenants (id, name) VALUES (?, ?) ON CONFLICT (id) DO NOTHING",
                id, name);
    }

    private void createUser(String email, UUID tenantId, String roleName) {
        if (appUserRepository.findByEmail(email).isPresent()) {
            return;
        }
        AppUser user = new AppUser();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(seedPassword));
        user.setTenantId(tenantId);
        user.setRole(roleName);
        user.getRoles().add(roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalStateException("Rol no encontrado: " + roleName)));
        appUserRepository.save(user);
    }
}
