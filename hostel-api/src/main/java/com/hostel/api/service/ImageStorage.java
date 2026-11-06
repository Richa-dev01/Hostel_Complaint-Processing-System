package com.hostel.api.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

// Stage 9 will add an S3 version of this; the rest of the app will not change
public interface ImageStorage {
    String save(MultipartFile file) throws IOException;
}
