package org.example.project_badminton.mapper;

import org.example.project_badminton.dto.response.BookingDTO;
import org.example.project_badminton.dto.response.UserDTO;
import org.example.project_badminton.entity.Booking;
import org.example.project_badminton.entity.User;
import java.util.stream.Collectors;

public class BookingMapper {

    public static BookingDTO toDTO(Booking booking) {
        if (booking == null) return null;

        return BookingDTO.builder()
                .id(booking.getId())

                // Gọi các Mapper khác để lồng DTO vào nhau (Nested DTOs)
                .customer(UserMapper.toDTO(booking.getCustomer()))
                .court(CourtMapper.toDTO(booking.getCourt()))
                .timeSlots(booking.getTimeSlots() != null ? 
                        booking.getTimeSlots().stream()
                                .map(TimeSlotMapper::toDTO)
                                .collect(Collectors.toList()) 
                        : null)

                .bookingDate(booking.getBookingDate())
                .status(booking.getStatus().name())
                .totalPrice(booking.getTotalPrice())
                .paymentProofImageUrl(booking.getPaymentProofImageUrl())
                .rejectionReason(booking.getRejectionReason())

                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }
}
