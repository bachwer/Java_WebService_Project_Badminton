package org.example.project_badminton.repository;

import org.example.project_badminton.entity.Booking;
import org.example.project_badminton.instance.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    // Lấy lịch sử đặt sân của một khách hàng cụ thể

    @EntityGraph(attributePaths = {
            "customer",
            "court",
            "timeSlots"

    })
    Page<Booking> findByCustomerId(Long customerId, Pageable pageable);

    // Manager/Admin lấy danh sách Booking theo trạng thái (Để duyệt PENDING)
    Page<Booking> findByStatus(BookingStatus status, Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.status = :status AND (:managerId IS NULL OR b.court.manager.id = :managerId)")
    Page<Booking> findByStatusAndOptionalManagerId(
            @Param("status") BookingStatus status,
            @Param("managerId") Long managerId,
            Pageable pageable
    );






    List<Booking> findByBookingDate(LocalDate status);
    // Manager lấy danh sách Booking của các sân do mình quản lý
    @Query("SELECT b FROM Booking b WHERE b.court.manager.id = :managerId")
    List<Booking> findAllByManagerId(@Param("managerId") Long managerId);

//     QUAN TRỌNG CHỐNG CONFLICT (UC-04):
    // Kiểm tra xem tại 1 Sân, 1 Ngày, 1 Khung giờ đã có ai đặt thành công (CONFIRMED) hoặc đang chờ duyệt (PENDING) chưa.
    // Trả về true nếu đã có người chiếm chỗ.
    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Booking b JOIN b.timeSlots ts " +
            "WHERE b.court.id = :courtId " +
            "AND b.bookingDate = :date " +
            "AND ts.id IN :timeSlotIds " +
            "AND b.status IN ('PENDING', 'CONFIRMED')")
    boolean existsConflictingBooking(@Param("courtId") Long courtId,
                                     @Param("date") LocalDate date,
                                     @Param("timeSlotIds") List<Long> timeSlotIds);


}
