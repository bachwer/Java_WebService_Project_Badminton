package org.example.project_badminton.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "court_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourtImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "court_id", nullable = false)
    private Court court;

    @Column(nullable = false, length = 500)
    private String imageUrl; // URL tĩnh an toàn trả về từ Cloudinary

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime uploadedAt;

}
