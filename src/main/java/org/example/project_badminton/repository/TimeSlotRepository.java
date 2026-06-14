package org.example.project_badminton.repository;

import org.example.project_badminton.entity.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {
    // Lấy tất cả các khung giờ đang được mở (active)
    List<TimeSlot> findByIsActiveTrue();
}
