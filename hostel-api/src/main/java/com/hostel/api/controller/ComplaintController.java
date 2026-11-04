package com.hostel.api.controller;

import com.hostel.api.model.Complaint;
import com.hostel.api.service.ComplaintService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/complaints")
public class ComplaintController {

    private final ComplaintService service;

    public ComplaintController(ComplaintService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> submit(@RequestBody Complaint complaint) {
        Complaint saved = service.create(complaint);
        return ResponseEntity.ok(Map.of(
            "message", "Complaint submitted successfully.",
            "complaintId", saved.getComplaintId()));
    }

    @GetMapping
    public List<Complaint> list(@RequestParam Integer studentId) {
        return service.getByStudent(studentId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Complaint> get(@PathVariable String id) {
        Complaint c = service.getById(id);
        return c == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(c);
    }
}
