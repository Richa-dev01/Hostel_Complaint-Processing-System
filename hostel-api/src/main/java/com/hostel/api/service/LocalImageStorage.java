package com.hostel.api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@Service
public class LocalImageStorage implements ImageStorage {

    private static final Map<String, String> TYPES =
            Map.of("image/jpeg", "jpg", "image/png", "png", "image/webp", "webp");

    private final Path folder;

    public LocalImageStorage(@Value("${app.upload-dir:uploads}") String uploadDir) throws IOException {
        this.folder = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(folder);
    }

    @Override
    public String save(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Please choose an image.");
        }
        String type = file.getContentType();
        String extension = type == null ? null : TYPES.get(type);
        if (extension == null) {
            throw new IllegalArgumentException("Only JPG, PNG or WebP images are allowed.");
        }
        // random name: the student's own file name is never used
        String name = UUID.randomUUID() + "." + extension;
        file.transferTo(folder.resolve(name));
        return "/uploads/" + name;      // this reference is what goes into the database
    }
}
