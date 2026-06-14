package org.example.project_badminton.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.project_badminton.instance.CourtStatus;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourtDTO {
    private Long id;
    private String name;
    private String description;
    private BigDecimal pricePerHour;
    private CourtStatus status;
    private Long managerId;
    private List<CourtImageDTO> images;
}