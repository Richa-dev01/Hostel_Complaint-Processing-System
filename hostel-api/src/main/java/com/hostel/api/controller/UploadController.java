package com.hostel.api.controller;

import com.hostel.api.service.ImageStorage;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/uploads")
public class UploadController {

    private final ImageStorage storage;

    public UploadController(ImageStorage storage) {
        this.storage = storage;
    }

    @PostMapping
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        if (!"STUDENT".equals(request.getAttribute("role"))) {
            return ResponseEntity.status(403).body(Map.of("message", "Only students can upload images."));
        }
        try {
            return ResponseEntity.ok(Map.of("imageUrl", storage.save(file)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.status(500).body(Map.of("message", "Could not save the image."));
        }
    }
}
