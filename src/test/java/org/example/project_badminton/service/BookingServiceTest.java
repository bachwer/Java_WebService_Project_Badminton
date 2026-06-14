package org.example.project_badminton.service;

import org.example.project_badminton.dto.request.BookingRequest;
import org.example.project_badminton.dto.response.BookingDTO;
import org.example.project_badminton.entity.Booking;
import org.example.project_badminton.entity.Court;
import org.example.project_badminton.entity.TimeSlot;
import org.example.project_badminton.entity.User;
import org.example.project_badminton.exception.ConflictException;
import org.example.project_badminton.exception.ResourceNotFoundException;
import org.example.project_badminton.instance.CourtStatus;
import org.example.project_badminton.instance.Role;
import org.example.project_badminton.repository.BookingRepository;
import org.example.project_badminton.repository.CourtRepository;
import org.example.project_badminton.repository.TimeSlotRepository;
import org.example.project_badminton.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)





class BookingServiceTest {
    @InjectMocks

    private BookingService bookingService;

    @Mock

    private BookingRepository bookingRepository;

    @Mock

    private CourtRepository courtRepository;

    @Mock

    private TimeSlotRepository timeSlotRepository;

    @Mock

    private UserRepository userRepository;


    @Test
    void courtBook_Success() {

        BookingRequest request = new BookingRequest();
        request.setCourtId(1L);
        request.setBookingDate(LocalDate.now().plusDays(1));
        request.setTimeSlotIds(List.of(1L, 2L));

        User user = new User();
        user.setId(1L);
        user.setUsername("rikkei");
        user.setPassword("123456");
        user.setEmail("bach@gmail.com");
        user.setPhone("0981234567");
        user.setFullName("Nguyen Van Bach");
        user.setRole(Role.ROLE_CUSTOMER);
        user.setActive(true);




        Court court = Court.builder()
                .id(1L)
                .pricePerHour(BigDecimal.valueOf(100))
                .build();

        TimeSlot slot1 = TimeSlot.builder().id(1L).build();
        TimeSlot slot2 = TimeSlot.builder().id(2L).build();

        when(userRepository.findByUsername("customer"))
                .thenReturn(user);

        when(courtRepository.findById(1L))
                .thenReturn(Optional.of(court));

        when(timeSlotRepository.findAllById(List.of(1L, 2L)))
                .thenReturn(List.of(slot1, slot2));

        when(bookingRepository.existsConflictingBooking(
                anyLong(),
                any(),
                anyList()
        )).thenReturn(false);

        BookingDTO result =
                bookingService.courtBook(request, "customer");

        assertNotNull(result);

        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void courtBook_UserNotFound() {

        BookingRequest request = new BookingRequest();

        when(userRepository.findByUsername("customer"))
                .thenReturn(null);

        assertThrows(
                ResourceNotFoundException.class,
                () -> bookingService.courtBook(request, "customer")
        );
    }

    @Test
    void courtBook_CourtNotFound() {

        BookingRequest request = new BookingRequest();
        request.setCourtId(1L);

        User user = User.builder().build();

        when(userRepository.findByUsername("customer"))
                .thenReturn(user);

        when(courtRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> bookingService.courtBook(request, "customer")
        );
    }

    @Test
    void courtBook_TimeSlotNotFound() {

        BookingRequest request = new BookingRequest();
        request.setCourtId(1L);
        request.setTimeSlotIds(List.of(1L, 2L));

        User user = User.builder().build();
        Court court = Court.builder().id(1L).build();

        when(userRepository.findByUsername("customer"))
                .thenReturn(user);

        when(courtRepository.findById(1L))
                .thenReturn(Optional.of(court));

        when(timeSlotRepository.findAllById(anyList()))
                .thenReturn(List.of());

        assertThrows(
                ResourceNotFoundException.class,
                () -> bookingService.courtBook(request, "customer")
        );
    }




    @Test
    void courtBook_ConflictBooking() {

        BookingRequest request = new BookingRequest();
        request.setCourtId(1L);
        request.setTimeSlotIds(List.of(1L));

        User user = User.builder().build();
        Court court = Court.builder().id(1L).build();

        TimeSlot slot = TimeSlot.builder().id(1L).build();

        when(userRepository.findByUsername("customer"))
                .thenReturn(user);

        when(courtRepository.findById(1L))
                .thenReturn(Optional.of(court));

        when(timeSlotRepository.findAllById(anyList()))
                .thenReturn(List.of(slot));

        when(bookingRepository.existsConflictingBooking(
                anyLong(),
                any(),
                anyList()
        )).thenReturn(true);

        assertThrows(
                ConflictException.class,
                () -> bookingService.courtBook(request, "customer")
        );
    }



    @Test
    void displayCourt_CourtNotFound() {

        when(courtRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> bookingService.displayCourt(
                        1L,
                        LocalDate.now(),
                        PageRequest.of(0,10)
                )
        );
    }


    @Test
    void displayCourt_CourtClosed() {

        Court court = Court.builder()
                .status(CourtStatus.CLOSED)
                .build();

        when(courtRepository.findById(1L))
                .thenReturn(Optional.of(court));

        assertThrows(
                ConflictException.class,
                () -> bookingService.displayCourt(
                        1L,
                        LocalDate.now(),
                        PageRequest.of(0,10)
                )
        );
    }

    @Test
    void getHistory_Success() {

        User user = new User();
        user.setId(1L);
        user.setUsername("rikkei");
        user.setPassword("123456");
        user.setEmail("bach@gmail.com");
        user.setPhone("0981234567");
        user.setFullName("Nguyen Van Bach");
        user.setRole(Role.ROLE_CUSTOMER);
        user.setActive(true);


        Court court = Court.builder()
                .id(1L)
                .name("Court A")
                .pricePerHour(BigDecimal.valueOf(100))
                .build();

        Booking booking = Booking.builder()
                .id(1L)
                .customer(user)
                .court(court)
                .bookingDate(LocalDate.now())
                .build();

        Page<Booking> page =
                new PageImpl<>(List.of(booking));

        when(bookingRepository.findByCustomerId(
                eq(1L),
                any(Pageable.class)
        )).thenReturn(page);

        Page<BookingDTO> result =
                bookingService.getHistory(
                        1L,
                        PageRequest.of(0, 10)
                );

        assertEquals(1, result.getTotalElements());
    }

}