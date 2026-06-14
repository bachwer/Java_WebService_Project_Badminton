package org.example.project_badminton.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.example.project_badminton.dto.response.CourtImageDTO;
import org.example.project_badminton.entity.CourtImage;

import java.math.BigDecimal;
import java.util.List;


@Data
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CourtRequest {

    @NotBlank(message = "Tên sân không được để trống")

    @Size(max = 100, message = "Tên sân không được vượt quá 100 ký tự")

    private String name;

    @NotBlank(message = "Mô tả không được để trống")

    @Size(max = 1000, message = "Mô tả không được vượt quá 1000 ký tự")

    private String description;

    @NotNull(message = "Giá thuê không được để trống")

    @DecimalMin(value = "0.0", inclusive = false, message = "Giá thuê phải lớn hơn 0")

    private BigDecimal pricePerHour;


    private Long managerId;

    @Valid
    private List<CourtImage> images;

}
