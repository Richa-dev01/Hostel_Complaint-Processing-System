package com.hostel.api.repository;

import com.hostel.api.model.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ComplaintRepository extends JpaRepository<Complaint, String> {
    List<Complaint> findByStudentId(Integer studentId);
}
