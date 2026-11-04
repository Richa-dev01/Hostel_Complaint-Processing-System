package com.hostel.api.config;

import com.hostel.api.model.Admin;
import com.hostel.api.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements CommandLineRunner {

    private final AdminRepository adminRepo;
    private final BCryptPasswordEncoder encoder;

    @Value("${ADMIN_PASSWORD:}")
    private String adminPassword;

    public AdminInitializer(AdminRepository adminRepo, BCryptPasswordEncoder encoder) {
        this.adminRepo = adminRepo;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        if (adminRepo.count() > 0) return;
        if (adminPassword == null || adminPassword.isBlank()) {
            System.out.println("WARNING: no admin exists. Set ADMIN_PASSWORD and restart to create one.");
            return;
        }
        Admin admin = new Admin();
        admin.setName("Hostel Admin");
        admin.setEmail("admin@hostel.com");
        admin.setPasswordHash(encoder.encode(adminPassword));
        adminRepo.save(admin);
        System.out.println("Default admin created: admin@hostel.com");
    }
}
