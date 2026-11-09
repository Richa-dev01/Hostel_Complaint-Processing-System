package com.hostel.consumer.service;

import com.hostel.consumer.model.Complaint;
import com.hostel.consumer.repository.ComplaintRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ComplaintProcessor {

    private final SqsClient sqsClient;
    private final ComplaintRepository repository;
    private final ObjectMapper objectMapper;

    @Value("${app.sqs.queue-url}")
    private String queueUrl;

    public ComplaintProcessor(SqsClient sqsClient, ComplaintRepository repository, ObjectMapper objectMapper) {
        this.sqsClient = sqsClient;
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelay = 5000)
    public void poll() {
        ReceiveMessageRequest request = ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .maxNumberOfMessages(10)
                .waitTimeSeconds(5)
                .build();

        List<Message> messages = sqsClient.receiveMessage(request).messages();

        for (Message message : messages) {
            try {
                Complaint c = objectMapper.readValue(message.body(), Complaint.class);

                boolean valid = c.getRoomNo() != null && !c.getRoomNo().isBlank()
                        && c.getDescription() != null && !c.getDescription().isBlank();

                if (valid) {
                    c.setStatus("SUBMITTED");
                    c.setPriority("MEDIUM");
                    c.setUpdatedAt(LocalDateTime.now());
                    repository.save(c);
                    sqsClient.deleteMessage(DeleteMessageRequest.builder()
                            .queueUrl(queueUrl)
                            .receiptHandle(message.receiptHandle())
                            .build());
                    System.out.println("Saved complaint " + c.getComplaintId() + " to RDS.");
                } else {
                    System.out.println("Invalid complaint " + c.getComplaintId()
                            + " - leaving in queue for retry/DLQ.");
                }
            } catch (Exception e) {
                System.out.println("Failed to process message: " + e.getMessage());
            }
        }
    }
}
