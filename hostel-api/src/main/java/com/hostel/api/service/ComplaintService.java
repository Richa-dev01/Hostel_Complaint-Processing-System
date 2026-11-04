package com.hostel.api.service;

import com.hostel.api.model.Complaint;
import com.hostel.api.repository.ComplaintRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ComplaintService {

    private final ComplaintRepository repository;

    public ComplaintService(ComplaintRepository repository) {
        this.repository = repository;
    }

    public Complaint create(Complaint c) {
        c.setComplaintId("C" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        c.setStatus("SUBMITTED");
        c.setCreatedAt(LocalDateTime.now());
        return repository.save(c);   // Stage 10: this line becomes "send to SQS"
    }

    public List<Complaint> getByStudent(Integer studentId) {
        return repository.findByStudentId(studentId);
    }

    public Complaint getById(String id) {
        return repository.findById(id).orElse(null);
    }
}
