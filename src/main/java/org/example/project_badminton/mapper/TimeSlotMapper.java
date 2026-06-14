package org.example.project_badminton.mapper;

import org.example.project_badminton.dto.response.TimeSlotDTO;
import org.example.project_badminton.entity.TimeSlot;

public class TimeSlotMapper {

    public static TimeSlotDTO toDTO(TimeSlot timeSlot) {
        if (timeSlot == null) return null;

        return TimeSlotDTO.builder()
                .id(timeSlot.getId())
                .startTime(timeSlot.getStartTime())
                .endTime(timeSlot.getEndTime())
                .isActive(timeSlot.isActive())
                .build();
    }
}
