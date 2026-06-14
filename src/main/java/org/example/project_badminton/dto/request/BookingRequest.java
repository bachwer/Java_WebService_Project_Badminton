package org.example.project_badminton.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest {

    @NotNull(message = "Court ID không được để trống")
    private Long courtId;

    @NotNull(message = "Ngày đặt sân không được để trống")
    @FutureOrPresent(message = "Ngày đặt sân phải là hôm nay hoặc trong tương lai")
    private LocalDate bookingDate;

    @NotEmpty(message = "Phải chọn ít nhất 1 khung giờ")
    private List<Long> timeSlotIds;
}
