package org.example.project_badminton.service;

import lombok.RequiredArgsConstructor;
import org.example.project_badminton.dto.request.ChangePasswordRequest;
import org.example.project_badminton.dto.request.ForgotPasswordRequest;
import org.example.project_badminton.dto.request.ResetPasswordRequest;
import org.example.project_badminton.dto.request.LoginRequest;
import org.example.project_badminton.dto.request.RegisterRequest;
import org.example.project_badminton.dto.request.TokenRefreshRequest;
import org.example.project_badminton.dto.response.AuthResponse;
import org.example.project_badminton.entity.RefreshToken;
import org.example.project_badminton.entity.TokenBlacklist;
import org.example.project_badminton.entity.User;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import java.security.SecureRandom;
import org.example.project_badminton.exception.ConflictException;
import org.example.project_badminton.exception.ForbiddenException;
import org.example.project_badminton.exception.ResourceNotFoundException;
import org.example.project_badminton.exception.UnauthorizedException;
import org.example.project_badminton.instance.Role;
import org.example.project_badminton.mapper.UserMapper;
import org.example.project_badminton.repository.RefreshTokenRepository;
import org.example.project_badminton.repository.TokenBlacklistRepository;
import org.example.project_badminton.repository.UserRepository;
import org.example.project_badminton.security.CustomUserDetails;
import org.example.project_badminton.security.JwtService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Date;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final JavaMailSender mailSender;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("Username đã tồn tại");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email đã tồn tại");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .phone(request.getPhone())
                .fullName(request.getFullName())
                .role(Role.ROLE_CUSTOMER)
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);

        return AuthResponse.builder()
                .user(UserMapper.toDTO(savedUser))
                .build();
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            User user = userRepository.findByUsername(request.getUsername());

            if (user == null) {
                throw new UnauthorizedException("Sai tài khoản hoặc mật khẩu");
            }

            if (!user.isActive()) {
                throw new ForbiddenException("Tài khoản đã bị khóa");

            }


            CustomUserDetails userDetails = new CustomUserDetails(user);
            refreshTokenRepository.deleteByUser(user);
            String jwtToken = jwtService.generateToken(userDetails);
            String refreshToken = jwtService.generateRefreshToken(userDetails);
            saveRefreshToken(user, refreshToken);
            return AuthResponse.builder()
                    .accessToken(jwtToken)
                    .refreshToken(refreshToken)
                    .user(UserMapper.toDTO(user))
                    .build();
        } catch (Exception e) {
            throw new UnauthorizedException("Sai tài khoản hoặc mật khẩu");
        }
    }

    @Transactional
    public AuthResponse refreshToken(TokenRefreshRequest request) {
        try {
            String requestRefreshToken = request.getRefreshToken();
            RefreshToken refreshToken = refreshTokenRepository.findByToken(requestRefreshToken);
            if (refreshToken == null) {
                throw new ForbiddenException("Refresh Token không tồn tại trong hệ thống");
            }
            if (refreshToken.getExpiryDate().compareTo(Instant.now()) < 0) {
                refreshTokenRepository.delete(refreshToken);
                throw new ForbiddenException("Refresh token đã hết hạn. Vui lòng đăng nhập lại.");
            }
            User user = refreshToken.getUser();
            CustomUserDetails userDetails = new CustomUserDetails(user);
            String accessToken = jwtService.generateToken(userDetails);

            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(requestRefreshToken)
                    .user(UserMapper.toDTO(user))
                    .build();
        } catch (Exception e) {
            throw new ForbiddenException("Refresh Token không tồn tại trong hệ thống");
        }
    }

    @Transactional
    public void logout(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }

        String jwt = authHeader.substring(7);
        String username = jwtService.extractUsername(jwt);
        java.util.Date expirationDate = jwtService.extractExpiration(jwt);
        long expirationInSeconds = (expirationDate.getTime() - System.currentTimeMillis()) / 1000;

        // Lưu vào Redis với số giây sống còn lại
        if (expirationInSeconds > 0) {
            TokenBlacklist blacklistToken = TokenBlacklist.builder()
                    .token(jwt)
                    .expiration(expirationInSeconds)
                    .build();
            tokenBlacklistRepository.save(blacklistToken);
        }

        // Lấy User trực tiếp từ DB dựa theo Repository của bạn
        User user = userRepository.findByUsername(username);

        // Kiểm tra null thay vì dùng Optional.ifPresent()
        if (user != null) {
            refreshTokenRepository.deleteByUser(user);
        }
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request, String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new ResourceNotFoundException("Tài khoản không tồn tại");
        }

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new ConflictException("Mật khẩu cũ không chính xác");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Transactional
    public String forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail());
        if (user == null) {
            throw new ResourceNotFoundException("Email không tồn tại trong hệ thống");
        }

        String otp = generateOtp();
        user.setResetToken(otp);
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(5));
        userRepository.save(user);

        sendEmail(user.getEmail(), "Mã xác thực OTP đặt lại mật khẩu", "Mã OTP của bạn là: " + otp + ". Mã này có hiệu lực trong 5 phút.");

        return otp;
    }

    @Transactional
    public String resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail());
        if (user == null) {
            throw new ResourceNotFoundException("Tài khoản với email này không tồn tại");
        }

        if (user.getResetToken() == null || !user.getResetToken().equals(request.getOtp())) {
            throw new ConflictException("Mã OTP không hợp lệ");
        }

        if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            user.setResetToken(null);
            user.setResetTokenExpiry(null);
            userRepository.save(user);
            throw new ConflictException("Mã OTP đã hết hạn");
        }

        String newPassword = generateRandomPassword();
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);

        sendEmail(user.getEmail(), "Mật khẩu mới của bạn", "Mật khẩu mới đã được thiết lập lại thành công. Mật khẩu mới của bạn là: " + newPassword);

        return newPassword;
    }

    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private void sendEmail(String to, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Gửi email thất bại: " + e.getMessage(), e);
        }
    }


    private void saveRefreshToken(User user, String token) {
        Date expirationDate = jwtService.extractExpiration(token);
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(token)
                .expiryDate(expirationDate.toInstant())
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshToken);
    }
}
