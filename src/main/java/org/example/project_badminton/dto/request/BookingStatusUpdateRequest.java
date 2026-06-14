package org.example.project_badminton.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingStatusUpdateRequest {
    @NotNull(message = "Trạng thái mới không được để trống")
    private String newStatus; // CONFIRMED, REJECTED, CANCELLED

    private String rejectionReason; // Bắt buộc nếu newStatus là REJECTED
}