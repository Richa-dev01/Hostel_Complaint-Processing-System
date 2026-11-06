package com.hostel.api.controller;

import com.hostel.api.dto.UpdateStatusRequest;
import com.hostel.api.service.AdminComplaintService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/admin/complaints")
public class AdminComplaintController {

    private final AdminComplaintService service;

    public AdminComplaintController(AdminComplaintService service) {
        this.service = service;
    }

    private boolean isAdmin(HttpServletRequest request) {
        return "ADMIN".equals(request.getAttribute("role"));
    }

    private ResponseEntity<?> forbidden() {
        return ResponseEntity.status(403).body(Map.of("message", "Admins only."));
    }

    @GetMapping
    public ResponseEntity<?> list(@RequestParam(required = false) String status,
                                  @RequestParam(required = false) Integer categoryId,
                                  @RequestParam(required = false) String priority,
                                  @RequestParam(required = false) String q,
                                  HttpServletRequest request) {
        if (!isAdmin(request)) return forbidden();
        return ResponseEntity.ok(service.search(status, categoryId, priority, q));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOne(@PathVariable String id, HttpServletRequest request) {
        if (!isAdmin(request)) return forbidden();
        try {
            return ResponseEntity.ok(service.getById(id));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable String id,
                                          @RequestBody UpdateStatusRequest body,
                                          HttpServletRequest request) {
        if (!isAdmin(request)) return forbidden();
        try {
            return ResponseEntity.ok(service.updateStatus(id, body));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id, HttpServletRequest request) {
        if (!isAdmin(request)) return forbidden();
        try {
            service.delete(id);
            return ResponseEntity.ok(Map.of("message", "Complaint deleted."));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(Map.of("message", e.getMessage()));
        }
    }
}
