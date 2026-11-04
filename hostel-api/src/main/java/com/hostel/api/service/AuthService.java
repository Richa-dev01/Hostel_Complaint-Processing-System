package com.hostel.api.service;

import com.hostel.api.dto.LoginRequest;
import com.hostel.api.dto.RegisterRequest;
import com.hostel.api.model.Admin;
import com.hostel.api.model.Student;
import com.hostel.api.repository.AdminRepository;
import com.hostel.api.repository.StudentRepository;
import com.hostel.api.security.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {

    private final StudentRepository studentRepo;
    private final AdminRepository adminRepo;
    private final BCryptPasswordEncoder encoder;
    private final JwtUtil jwtUtil;

    public AuthService(StudentRepository studentRepo, AdminRepository adminRepo,
                       BCryptPasswordEncoder encoder, JwtUtil jwtUtil) {
        this.studentRepo = studentRepo;
        this.adminRepo = adminRepo;
        this.encoder = encoder;
        this.jwtUtil = jwtUtil;
    }

    public void register(RegisterRequest r) {
        if (isBlank(r.name()) || isBlank(r.email()) || isBlank(r.password())) {
            throw new IllegalArgumentException("Name, email and password are required.");
        }
        if (r.password().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters.");
        }
        String email = r.email().trim().toLowerCase();
        if (studentRepo.existsByEmail(email)) {
            throw new IllegalStateException("This email is already registered.");
        }
        Student s = new Student();
        s.setName(r.name().trim());
        s.setEmail(email);
        s.setPasswordHash(encoder.encode(r.password()));   // store the hash, not the password
        s.setRoomNo(r.roomNo());
        studentRepo.save(s);
    }

    public Optional<Map<String, String>> loginStudent(LoginRequest r) {
        if (isBlank(r.email()) || isBlank(r.password())) return Optional.empty();
        return studentRepo.findByEmail(r.email().trim().toLowerCase())
                .filter(s -> encoder.matches(r.password(), s.getPasswordHash()))
                .map(s -> Map.of(
                        "token", jwtUtil.generate(s.getStudentId(), "STUDENT"),
                        "name", s.getName(),
                        "role", "STUDENT"));
    }

    public Optional<Map<String, String>> loginAdmin(LoginRequest r) {
        if (isBlank(r.email()) || isBlank(r.password())) return Optional.empty();
        return adminRepo.findByEmail(r.email().trim().toLowerCase())
                .filter(a -> encoder.matches(r.password(), a.getPasswordHash()))
                .map(a -> Map.of(
                        "token", jwtUtil.generate(a.getAdminId(), "ADMIN"),
                        "name", a.getName(),
                        "role", "ADMIN"));
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
