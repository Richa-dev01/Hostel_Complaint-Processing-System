package com.hostel.api.controller;

import com.hostel.api.model.Complaint;
import com.hostel.api.service.ComplaintService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/complaints")
public class ComplaintController {

    private final ComplaintService service;

    public ComplaintController(ComplaintService service) {
        this.service = service;
    }

    private boolean isStudent(HttpServletRequest request) {
        return "STUDENT".equals(request.getAttribute("role"));
    }

    @PostMapping
    public ResponseEntity<?> submit(@RequestBody Complaint complaint, HttpServletRequest request) {
        if (!isStudent(request)) {
            return ResponseEntity.status(403).body(Map.of("message", "Only students can submit complaints."));
        }
        // the student ID is taken from the login token, never from the request body
        complaint.setStudentId((Integer) request.getAttribute("userId"));
        Complaint saved = service.create(complaint);
        return ResponseEntity.ok(Map.of(
                "message", "Complaint submitted successfully.",
                "complaintId", saved.getComplaintId()));
    }

    @GetMapping
    public ResponseEntity<?> myComplaints(HttpServletRequest request) {
        if (!isStudent(request)) {
            return ResponseEntity.status(403).body(Map.of("message", "Students only."));
        }
        Integer studentId = (Integer) request.getAttribute("userId");
        return ResponseEntity.ok(service.getByStudent(studentId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOne(@PathVariable String id, HttpServletRequest request) {
        if (!isStudent(request)) {
            return ResponseEntity.status(403).body(Map.of("message", "Students only."));
        }
        Integer studentId = (Integer) request.getAttribute("userId");
        Complaint c = service.getById(id);
        if (c == null || !studentId.equals(c.getStudentId())) {
            return ResponseEntity.notFound().build();   // someone else's complaint looks like "not found"
        }
        return ResponseEntity.ok(c);
    }
}
