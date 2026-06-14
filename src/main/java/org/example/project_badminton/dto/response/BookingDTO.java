package org.example.project_badminton.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingDTO {
    private Long id;

    // Sử dụng UserDTO và CourtDTO tinh gọn để gửi về Frontend
    private UserDTO customer;
    private CourtDTO court;
    private List<TimeSlotDTO> timeSlots;

    private LocalDate bookingDate;
    private String status;
    private BigDecimal totalPrice;
    private String paymentProofImageUrl;
    private String rejectionReason;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}