package com.hostel.api.service;

import com.hostel.api.dto.UpdateStatusRequest;
import com.hostel.api.model.Complaint;
import com.hostel.api.repository.ComplaintRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class AdminComplaintService {

    // the allowed status flow
    private static final List<String> STATUSES =
            List.of("SUBMITTED", "IN_REVIEW", "ASSIGNED", "IN_PROGRESS", "RESOLVED");

    private final ComplaintRepository repository;

    public AdminComplaintService(ComplaintRepository repository) {
        this.repository = repository;
    }

    // all complaints, with optional filters and a simple text search
    public List<Complaint> search(String status, Integer categoryId, String priority, String q) {
        String query = q == null ? "" : q.trim().toLowerCase();
        return repository.findAll().stream()
                .filter(c -> isBlank(status) || status.equalsIgnoreCase(c.getStatus()))
                .filter(c -> categoryId == null || categoryId.equals(c.getCategoryId()))
                .filter(c -> isBlank(priority) || priority.equalsIgnoreCase(c.getPriority()))
                .filter(c -> query.isEmpty() || contains(c.getComplaintId(), query)
                        || contains(c.getRoomNo(), query) || contains(c.getDescription(), query))
                .sorted(Comparator.comparing(Complaint::getCreatedAt,
                        Comparator.nullsLast(Comparator.<LocalDateTime>reverseOrder())))
                .toList();
    }

    public Complaint getById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Complaint not found."));
    }

    public Complaint updateStatus(String id, UpdateStatusRequest r) {
        Complaint c = getById(id);

        String status = r.status() == null ? "" : r.status().trim().toUpperCase();
        if (!STATUSES.contains(status)) {
            throw new IllegalArgumentException(
                    "Invalid status. Use: SUBMITTED, IN_REVIEW, ASSIGNED, IN_PROGRESS or RESOLVED.");
        }
        if ("ASSIGNED".equals(status) && isBlank(r.assignedTo()) && isBlank(c.getAssignedTo())) {
            throw new IllegalArgumentException("Please enter who the complaint is assigned to.");
        }

        if (!isBlank(r.assignedTo())) c.setAssignedTo(r.assignedTo().trim());
        if (!isBlank(r.remarks())) c.setResolutionRemarks(r.remarks().trim());

        LocalDateTime now = LocalDateTime.now();
        c.setStatus(status);
        c.setUpdatedAt(now);
        c.setResolvedAt("RESOLVED".equals(status) ? now : null);
        return repository.save(c);
    }

    public void delete(String id) {
        Complaint c = getById(id);
        if ("RESOLVED".equals(c.getStatus())) {
            throw new IllegalStateException("Resolved complaints cannot be deleted (history is kept).");
        }
        repository.delete(c);
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private boolean contains(String text, String query) {
        return text != null && text.toLowerCase().contains(query);
    }
}
