package org.example.project_badminton.mapper;

import org.example.project_badminton.dto.response.CourtDTO;
import org.example.project_badminton.dto.response.CourtImageDTO;
import org.example.project_badminton.dto.response.UserDTO;
import org.example.project_badminton.entity.Court;
import org.example.project_badminton.entity.CourtImage;

import java.util.stream.Collectors;

public class CourtMapper {

    public static CourtImageDTO toImageDTO(CourtImage image){
        if(image == null){
            return  null;
        }

        return CourtImageDTO.builder()
                .id(image.getId())
                .imageUrl(image.getImageUrl())
                .build();

    }
    public static CourtDTO toDTO(Court court){
        return CourtDTO.builder()
                .id(court.getId())
                .name(court.getName())
                .description(court.getDescription())
                .pricePerHour(court.getPricePerHour())
                .status(court.getStatus())
                .managerId(court.getManager() != null ? court.getManager().getId() : null)
                // Dùng Stream API để map List<CourtImage> sang List<CourtImageDTO>
                .images(court.getImages() != null ?
                        court.getImages().stream()
                                .map(CourtMapper::toImageDTO)
                                .collect(Collectors.toList())
                        : null)
                .build();

    }



}
