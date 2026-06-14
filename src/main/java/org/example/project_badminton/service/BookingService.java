package org.example.project_badminton.service;


import org.example.project_badminton.dto.request.BookingRequest;
import org.example.project_badminton.dto.request.BookingStatusUpdateRequest;
import org.example.project_badminton.instance.Role;
import org.example.project_badminton.dto.response.BookingDTO;
import org.example.project_badminton.dto.response.TimeSlotDTO;
import org.example.project_badminton.entity.Booking;
import org.example.project_badminton.entity.Court;
import org.example.project_badminton.entity.TimeSlot;
import org.example.project_badminton.entity.User;
import org.example.project_badminton.exception.ConflictException;
import org.example.project_badminton.exception.ResourceNotFoundException;
import org.example.project_badminton.instance.BookingStatus;
import org.example.project_badminton.instance.CourtStatus;
import org.example.project_badminton.mapper.BookingMapper;
import org.example.project_badminton.repository.BookingRepository;
import org.example.project_badminton.repository.CourtRepository;
import org.example.project_badminton.repository.TimeSlotRepository;
import org.example.project_badminton.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private CourtRepository courtRepository;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Page<BookingDTO> getHistory(Long id, Pageable pageable){
        Pageable sortedPageable = org.springframework.data.domain.PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                org.springframework.data.domain.Sort.by("id").descending()
        );
        Page<Booking> booking = bookingRepository.findByCustomerId(id, sortedPageable);
        return booking.map(BookingMapper::toDTO);
    }




    @Transactional(readOnly = true)
    public Page<BookingDTO> getAllBookingPending(Pageable pageable, BookingStatus status, String username){
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new ResourceNotFoundException("User not found");
        }

        Long managerId = null;
        if (user.getRole() == Role.ROLE_MANAGER) {
            managerId = user.getId();
        }

        Pageable sortedPageable = org.springframework.data.domain.PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                org.springframework.data.domain.Sort.by("id").descending()
        );
        Page<Booking> booking = bookingRepository.findByStatusAndOptionalManagerId(status, managerId, sortedPageable);
        return booking.map(BookingMapper::toDTO);
    }

    @Transactional
    public BookingDTO updateBookingStatus(Long bookingId, BookingStatusUpdateRequest request, String username) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Not found Booking ID"));

        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new ResourceNotFoundException("User not found");
        }

        // Quyền kiểm soát: ADMIN có toàn quyền, MANAGER chỉ được thao tác trên sân do mình quản lý
        if (user.getRole() != Role.ROLE_ADMIN) {
            if (user.getRole() == Role.ROLE_MANAGER) {
                if (booking.getCourt() == null || booking.getCourt().getManager() == null || !booking.getCourt().getManager().getId().equals(user.getId())) {
                    throw new ConflictException("Bạn không có quyền phê duyệt lịch đặt cho sân này");
                }
            } else {
                throw new ConflictException("Không có quyền thực hiện hành động này");
            }
        }

        BookingStatus newStatus;
        try {
            newStatus = BookingStatus.valueOf(request.getNewStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Trạng thái mới không hợp lệ");
        }

        if (newStatus == BookingStatus.REJECTED) {
            if (request.getRejectionReason() == null || request.getRejectionReason().trim().isEmpty()) {
                throw new IllegalArgumentException("Lý do từ chối không được để trống khi từ chối lịch");
            }
            booking.setRejectionReason(request.getRejectionReason());
        }

        booking.setStatus(newStatus);
        bookingRepository.save(booking);
        return BookingMapper.toDTO(booking);
    }


    @Transactional(readOnly = true)
    public Page<TimeSlotDTO> displayCourt(Long courtId, LocalDate bookingDate, Pageable pageable){


        Court court = courtRepository.findById(courtId).orElseThrow(() -> new ResourceNotFoundException("Court ID not found !"));
        if(court.getStatus() != CourtStatus.AVAILABLE){
            throw new ConflictException("Court Closed !!");
        }

        List<Booking> bookings = bookingRepository.findByBookingDate(bookingDate)
                .stream()
                .filter(booking -> booking.getCourt() != null
                        && booking.getCourt().getId().equals(courtId))
                .toList();

        Set<Long> bookedSlotIds = bookings.stream()
                .flatMap(booking -> booking.getTimeSlots().stream())
                .map(TimeSlot::getId)
                .collect(Collectors.toSet());

        Page<TimeSlot> timeSlots = timeSlotRepository.findAll(pageable);

        return timeSlots.map(slot -> TimeSlotDTO.builder()
                .id(slot.getId())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .isActive(!bookedSlotIds.contains(slot.getId()))
                .build());
    }





    @Transactional
    public BookingDTO courtBook(BookingRequest bookingRequest, String username){

        User customer = userRepository.findByUsername(username);
        if (customer == null) {
            throw new ResourceNotFoundException("User not found");
        }

        Court court = courtRepository.findById(bookingRequest.getCourtId()).orElseThrow(() -> new ResourceNotFoundException("Not found court ID !!"));

        List<TimeSlot> timeSlots = timeSlotRepository.findAllById(bookingRequest.getTimeSlotIds());
        if (timeSlots.size() != bookingRequest.getTimeSlotIds().size()) {
            throw new ResourceNotFoundException("Một hoặc nhiều khung giờ không tồn tại");
        }

        if (bookingRequest.getBookingDate().isEqual(LocalDate.now())) {
            LocalTime now = LocalTime.now();

            boolean hasExpiredSlot = timeSlots.stream()
                    .anyMatch(slot -> !slot.getEndTime().isAfter(now));

            if (hasExpiredSlot) {
                throw new ConflictException("Không thể đặt khung giờ đã qua");
            }
        }

        if (bookingRequest.getBookingDate().isBefore(LocalDate.now())) {
            throw new ConflictException("Không thể đặt sân trong ngày quá khứ");
        }

        boolean conflict = bookingRepository.existsConflictingBooking(
                court.getId(),
                bookingRequest.getBookingDate(),
                bookingRequest.getTimeSlotIds()
        );

        if (conflict) {
            throw new ConflictException("Khung giờ bạn chọn đã có người đặt, vui lòng chọn giờ khác");
        }


        // Tính tiền: pricePerHour * (số lượng slot / 2)
        BigDecimal hours = BigDecimal.valueOf(timeSlots.size()).divide(BigDecimal.valueOf(2));
        BigDecimal totalPrice = court.getPricePerHour().multiply(hours);
        Booking booking = Booking.builder()
                .customer(customer)
                .court(court)
                .bookingDate(bookingRequest.getBookingDate())
                .timeSlots(timeSlots)
                .totalPrice(totalPrice)
                .status(org.example.project_badminton.instance.BookingStatus.PENDING)
                .build();
        bookingRepository.save(booking);
        return BookingMapper.toDTO(booking);
    }
}
