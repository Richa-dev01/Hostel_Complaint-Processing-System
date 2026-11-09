package com.hostel.api.service;

import tools.jackson.databind.ObjectMapper;
import com.hostel.api.model.Complaint;
import com.hostel.api.repository.ComplaintRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ComplaintService {

    private final ComplaintRepository repository;
    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;

    @Value("${app.sqs.queue-url}")
    private String queueUrl;

    public ComplaintService(ComplaintRepository repository, SqsClient sqsClient, ObjectMapper objectMapper) {
        this.repository = repository;
        this.sqsClient = sqsClient;
        this.objectMapper = objectMapper;
    }

    public Complaint create(Complaint c) {
        c.setComplaintId("C" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        c.setStatus("SUBMITTED");
        c.setCreatedAt(LocalDateTime.now());

        try {
            String messageBody = objectMapper.writeValueAsString(c);
            sqsClient.sendMessage(SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(messageBody)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("Failed to queue complaint: " + e.getMessage(), e);
        }

        // Intentionally NOT saved to the DB here.
        // The consumer (Stage 11) reads this off the queue, validates it (Stage 12),
        // and saves valid ones to RDS. Invalid ones go to the DLQ (Stage 13).
        return c;
    }

    public List<Complaint> getByStudent(Integer studentId) {
        return repository.findByStudentId(studentId);
    }

    public Complaint getById(String id) {
        return repository.findById(id).orElse(null);
    }
}
