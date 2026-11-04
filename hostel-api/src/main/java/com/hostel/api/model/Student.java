package com.hostel.api.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "student")
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "student_id")
    private Integer studentId;

    private String name;
    private String email;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "room_no")
    private String roomNo;
}
