package com.hostel.consumer.repository;

import com.hostel.consumer.model.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComplaintRepository extends JpaRepository<Complaint, String> {
}
