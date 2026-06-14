package org.example.project_badminton.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.example.project_badminton.entity.CourtImage;
import org.example.project_badminton.instance.CourtStatus;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourtUpdateRequest {

    @Size(max = 100, message = "Tên sân không được vượt quá 100 ký tự")
    private String name;

    @Size(max = 1000, message = "Mô tả không được vượt quá 1000 ký tự")
    private String description;

    @DecimalMin(value = "0.0", inclusive = false, message = "Giá thuê phải lớn hơn 0")
    private BigDecimal pricePerHour;

    private CourtStatus status;

    private Long managerId;

    @Valid
    private List<CourtImage> images;
}