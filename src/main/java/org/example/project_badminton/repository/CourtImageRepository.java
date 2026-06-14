package org.example.project_badminton.repository;

import org.example.project_badminton.entity.CourtImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourtImageRepository extends JpaRepository<CourtImage, Long> {
    void deleteByCourtId(Long courtId);
}
