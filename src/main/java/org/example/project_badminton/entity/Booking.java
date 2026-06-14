package org.example.project_badminton.entity;


import jakarta.persistence.*;
import lombok.*;
import org.example.project_badminton.instance.BookingStatus;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "court_id", nullable = false)
    private Court court;

    @Column(name = "booking_date", nullable = false)
    private LocalDate bookingDate;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "booking_time_slots",
            joinColumns = @JoinColumn(name = "booking_id"),
            inverseJoinColumns = @JoinColumn(name = "time_slot_id")
    )
    @Builder.Default
    private List<TimeSlot> timeSlots = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private BookingStatus status = BookingStatus.PENDING;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(length = 500)
    private String paymentProofImageUrl; // Hình ảnh bill chuyển khoản (Nếu áp dụng Upload Cloudinary)

    @Column(length = 255)
    private String rejectionReason; // Lý do từ chối (Nếu Admin/Manager chuyển status = REJECTED)

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

}

