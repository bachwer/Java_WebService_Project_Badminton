package org.example.project_badminton.instance;

public enum BookingStatus {
    PENDING,    // Chờ duyệt (Khởi tạo mặc định theo UC-04)
    CONFIRMED,  // Đã xác nhận
    REJECTED,   // Bị từ chối (Admin/Manager reject)
    CANCELLED   // Khách hàng tự hủy
}
