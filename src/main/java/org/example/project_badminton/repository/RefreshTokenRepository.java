package org.example.project_badminton.repository;

import jakarta.transaction.Transactional;
import org.example.project_badminton.entity.RefreshToken;
import org.example.project_badminton.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    RefreshToken findByToken(String token);

    // Xóa tất cả RefreshToken của một User (Dùng khi User đổi mật khẩu hoặc bị ban)
    @Transactional
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.user = :user")
    void deleteByUser(@Param("user") User user);
}
