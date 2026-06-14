package org.example.project_badminton.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.util.concurrent.TimeUnit;

@RedisHash("TokenBlacklist")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenBlacklist {

    // Bắt buộc dùng @Id của Spring Data, KHÔNG dùng của jakarta.persistence
    @Id
    private String token;

    // Đổi Instant thành Long để lưu trữ số giây (seconds) đếm ngược
    @TimeToLive(unit = TimeUnit.SECONDS)
    private Long expiration;

}