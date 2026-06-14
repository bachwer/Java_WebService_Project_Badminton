package org.example.project_badminton.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.project_badminton.dto.request.BookingRequest;
import org.example.project_badminton.dto.request.BookingStatusUpdateRequest;
import org.example.project_badminton.dto.response.ApiResponse;
import org.example.project_badminton.dto.response.BookingDTO;
import org.example.project_badminton.dto.response.TimeSlotDTO;
import org.example.project_badminton.instance.BookingStatus;
import org.example.project_badminton.security.CustomUserDetails;
import org.example.project_badminton.service.BookingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping
    public ResponseEntity<ApiResponse<BookingDTO>> createBooking(
            @Valid @RequestBody BookingRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        BookingDTO bookingDTO = bookingService.courtBook(request, username);
        return ResponseEntity.ok(new ApiResponse<>(true, "Đặt sân thành công", bookingDTO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<List<TimeSlotDTO>>> checkCourt( @Valid @PathVariable Long id,
            @RequestParam LocalDate date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<TimeSlotDTO> result = bookingService.displayCourt(id, date, pageable).getContent();
        return ResponseEntity.ok(new ApiResponse<>(true, "Lấy danh sách khung giờ thành công", result));
    }


    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<BookingDTO>>> getBookingHistory(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUser().getId();
        Pageable pageable = PageRequest.of(page, size);
        List<BookingDTO> history = bookingService.getHistory(userId, pageable).getContent();
        return ResponseEntity.ok(new ApiResponse<>(true, "Lấy lịch sử đặt sân thành công", history));
    }


    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<BookingDTO>>> getBookingStatusPending(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ){
        Pageable pageable = PageRequest.of(page, size);
        String username = authentication.getName();
        List<BookingDTO> bookingDTO = bookingService.getAllBookingPending(pageable, BookingStatus.PENDING, username).getContent();
        return ResponseEntity.ok(new ApiResponse<>(true, "Lấy danh sách bookingPending thành công !", bookingDTO ));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<BookingDTO>> updateBookingStatus(
            @PathVariable Long id,
            @Valid @RequestBody BookingStatusUpdateRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        BookingDTO bookingDTO = bookingService.updateBookingStatus(id, request, username);
        return ResponseEntity.ok(new ApiResponse<>(true, "Cập nhật trạng thái booking thành công", bookingDTO));
    }
}
