package org.example.project_badminton.repository;

import org.example.project_badminton.entity.Court;
import org.example.project_badminton.entity.User;
import org.example.project_badminton.instance.CourtStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourtRepository extends JpaRepository<Court, Long> {

    // Lấy danh sách sân theo trạng thái (VD: Chỉ lấy sân đang AVAILABLE cho khách đặt)
    List<Court> findByStatus(CourtStatus status);

    // Manager xem danh sách sân do mình quản lý
    List<Court> findByManagerId(Long managerId);
    Page<Court> findByManagerId(Long managerId, Pageable pageable);

    Page<Court> findByNameContainingIgnoreCaseAndManagerId(
            String name,
            Long id,
            Pageable pageable
    );

    Page<Court> findByNameContainingIgnoreCase(
            String name,
            Pageable pageable
    );

}
