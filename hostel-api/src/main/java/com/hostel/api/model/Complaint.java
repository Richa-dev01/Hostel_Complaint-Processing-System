package com.hostel.api.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "complaint")
public class Complaint {
    @Id
    @Column(name = "complaint_id")
    private String complaintId;

    @Column(name = "student_id")
    private Integer studentId;

    @Column(name = "category_id")
    private Integer categoryId;

    @Column(name = "room_no")
    private String roomNo;

    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    private String priority;
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "resolution_remarks")
    private String resolutionRemarks;

    @Column(name = "assigned_to")
    private String assignedTo;
}
