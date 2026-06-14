package org.example.project_badminton.controller;


import jakarta.persistence.Entity;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.project_badminton.dto.request.ChangePasswordRequest;
import org.example.project_badminton.dto.request.ForgotPasswordRequest;
import org.example.project_badminton.dto.request.ResetPasswordRequest;
import org.example.project_badminton.dto.request.LoginRequest;
import org.example.project_badminton.dto.request.RegisterRequest;
import org.example.project_badminton.dto.request.TokenRefreshRequest;
import org.example.project_badminton.dto.response.ApiResponse;
import org.example.project_badminton.dto.response.AuthResponse;
import org.example.project_badminton.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;



    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request){
        AuthResponse response = authService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Đăng ký thành công", response));

    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request){
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Đăng nhập thành công", response));

    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody TokenRefreshRequest tokenRefreshRequest){
        AuthResponse response = authService.refreshToken(tokenRefreshRequest);

        return ResponseEntity.ok( new ApiResponse<>(true, "Refresh token thành công", response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        final String authHeader = request.getHeader("Authorization");
        authService.logout(authHeader);
        return ResponseEntity.ok(new ApiResponse<>(true, "Đăng xuất thành công, Token đã bị hủy", null));
    }

    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {
        
        String username = authentication.getName();
        authService.changePassword(request, username);
        
        return ResponseEntity.ok(new ApiResponse<>(true, "Đổi mật khẩu thành công", null));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Mã OTP đã được gửi về email của bạn.", null));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        String newPassword = authService.resetPassword(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Đặt lại mật khẩu thành công. Mật khẩu mới của bạn là: " + newPassword, newPassword));
    }
}
