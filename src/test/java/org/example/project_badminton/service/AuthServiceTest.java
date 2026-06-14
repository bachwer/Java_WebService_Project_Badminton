package org.example.project_badminton.service;

import lombok.RequiredArgsConstructor;
import org.example.project_badminton.dto.request.ChangePasswordRequest;
import org.example.project_badminton.dto.request.LoginRequest;
import org.example.project_badminton.dto.request.RegisterRequest;
import org.example.project_badminton.dto.response.AuthResponse;
import org.example.project_badminton.entity.User;
import org.example.project_badminton.exception.ConflictException;
import org.example.project_badminton.exception.UnauthorizedException;
import org.example.project_badminton.instance.Role;
import org.example.project_badminton.repository.RefreshTokenRepository;
import org.example.project_badminton.repository.TokenBlacklistRepository;
import org.example.project_badminton.repository.UserRepository;
import org.example.project_badminton.security.CustomUserDetails;
import org.example.project_badminton.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Date;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@RequiredArgsConstructor
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthService authService;

    @Mock
    private PasswordEncoder passwordEncoder;



    @Mock

    private AuthenticationManager authenticationManager;



    @Mock

    private JwtService jwtService;
    @Mock

    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private TokenBlacklistRepository tokenBlacklistRepository;

    @Mock
    private org.springframework.mail.javamail.JavaMailSender mailSender;

    @Test
    void register_Success() {


        RegisterRequest request = new RegisterRequest();
        request.setEmail("dinhthanh413@gmail.com");
        request.setPhone("0988765412");
        request.setPassword("0988765412");
        request.setFullName("Dinh Ding Thanh");
        request.setUsername("dinhthanh");


        // Mock các kiểm tra tồn tại

        when(userRepository.existsByEmail("dinhthanh413@gmail.com"))

                .thenReturn(false);

        when(userRepository.existsByUsername("dinhthanh"))

                .thenReturn(false);

        // Mock encode password

        when(passwordEncoder.encode("0988765412"))

                .thenReturn("encoded_password");

        // Mock save

        when(userRepository.save(any(User.class)))

                .thenAnswer(invocation -> invocation.getArgument(0));

        authService.register(request);


        ArgumentCaptor<User> captor =

                ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(captor.capture());

        User savedUser = captor.getValue();
        assertEquals("Dinh Ding Thanh", savedUser.getFullName());

        assertEquals("dinhthanh413@gmail.com", savedUser.getEmail());

        assertEquals("0988765412", savedUser.getPhone());

        assertEquals("dinhthanh", savedUser.getUsername());

        assertEquals("encoded_password", savedUser.getPassword());

        assertTrue(savedUser.isActive());

        verify(userRepository).existsByEmail("dinhthanh413@gmail.com");

        verify(userRepository).existsByUsername("dinhthanh");

        verify(userRepository).save(any(User.class));


    }

    @Test
    void register_existEmail() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("dinhthanh413@gmail.com");
        request.setUsername("dinhthanh");
        request.setPassword("123456");
        request.setPhone("0988765412");
        request.setFullName("Dinh Ding Thanh");
        when(userRepository.existsByEmail("dinhthanh413@gmail.com")).thenReturn(true);
        assertThrows(ConflictException.class, () -> authService.register(request));
        verify(userRepository).existsByEmail("dinhthanh413@gmail.com");
    }

    @Test
    void register_existUsername() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("dinhthanh413@gmail.com");
        request.setUsername("dinhthanh");
        request.setPassword("123456");
        request.setPhone("0988765412");
        request.setFullName("Dinh Ding Thanh");
        when(userRepository.existsByUsername("dinhthanh")).thenReturn(true);
        assertThrows(ConflictException.class, () -> authService.register(request));
        verify(userRepository).existsByUsername("dinhthanh");
    }


    @Test
    void login_success() {
        LoginRequest request = new LoginRequest();
        request.setUsername("dinhthanh");
        request.setPassword("123456");
        User user = User.builder()
                .id(1L)
                .username("dinhthanh")
                .password("encodedPassword")
                .email("dinhthanh413@gmail.com")
                .role(Role.ROLE_CUSTOMER)
                .isActive(true)
                .build();
        when(userRepository.findByUsername("dinhthanh")).thenReturn(user);
        when(jwtService.generateToken(any(CustomUserDetails.class))).thenReturn("access_token");
        when(jwtService.generateRefreshToken(any(CustomUserDetails.class))).thenReturn("refresh_token");
        when(jwtService.extractExpiration("refresh_token")).thenReturn(new Date(System.currentTimeMillis() + 86400000));
        AuthResponse response = authService.login(request);
        assertNotNull(response);
        assertEquals("access_token", response.getAccessToken());
        assertEquals("refresh_token", response.getRefreshToken());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(refreshTokenRepository).deleteByUser(user);

    }
    @Test
    void login_wrongPassword() {
        LoginRequest request = new LoginRequest();
        request.setUsername("dinhthanh");
        request.setPassword("wrong_password");

        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> authService.login(request)
        );

        assertEquals("Sai tài khoản hoặc mật khẩu", exception.getMessage()
        );
        verify(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));
    }



    @Test
    void logout_success() {
        String jwt = "jwt_token";
        String authHeader = "Bearer " + jwt;

        User user = User.builder()
                .id(1L)
                .username("dinhthanh")
                .build();

        Long expirationDate = System.currentTimeMillis() + 3600000;

        when(jwtService.extractUsername(jwt))
                .thenReturn("dinhthanh");
        when(jwtService.extractExpiration(jwt))
                .thenReturn(new Date(expirationDate));
        when(userRepository.findByUsername("dinhthanh"))
                .thenReturn(user);

        authService.logout(authHeader);

        verify(tokenBlacklistRepository).save(
                argThat((org.example.project_badminton.entity.TokenBlacklist token) ->
                        token.getToken().equals(jwt)
                                && token.getExpiration().equals(expirationDate)
                )
        );

        verify(refreshTokenRepository).deleteByUser(user);
    }

    @Test
    void changePassword() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("old1234");
        request.setNewPassword("new1234");
        User user = User.builder()
                .id(1L)
                .username("dinhthanh")
                .password("encodedOldPassword")
                .build();
        when(userRepository.findByUsername("dinhthanh"))
                .thenReturn(user);

        when(passwordEncoder.matches(
                "old1234",
                "encodedOldPassword"))
                .thenReturn(true);

        when(passwordEncoder.encode("new1234"))
                .thenReturn("encodedNewPassword");

        authService.changePassword(request, "dinhthanh");

        assertEquals("encodedNewPassword", user.getPassword());

        verify(userRepository).save(user);
    }

    @Test
    void forgotPassword_Success() {
        org.example.project_badminton.dto.request.ForgotPasswordRequest request = new org.example.project_badminton.dto.request.ForgotPasswordRequest("dinhthanh413@gmail.com");
        User user = User.builder()
                .id(1L)
                .username("dinhthanh")
                .email("dinhthanh413@gmail.com")
                .build();
        when(userRepository.findByEmail("dinhthanh413@gmail.com")).thenReturn(user);

        String token = authService.forgotPassword(request);

        assertNotNull(token);
        assertEquals(6, token.length());
        assertEquals(token, user.getResetToken());
        assertNotNull(user.getResetTokenExpiry());
        verify(userRepository).save(user);
        verify(mailSender).send(any(org.springframework.mail.SimpleMailMessage.class));
    }

    @Test
    void resetPassword_Success() {
        org.example.project_badminton.dto.request.ResetPasswordRequest request = new org.example.project_badminton.dto.request.ResetPasswordRequest("dinhthanh413@gmail.com", "123456");
        User user = User.builder()
                .id(1L)
                .username("dinhthanh")
                .email("dinhthanh413@gmail.com")
                .resetToken("123456")
                .resetTokenExpiry(java.time.LocalDateTime.now().plusMinutes(10))
                .build();
        when(userRepository.findByEmail("dinhthanh413@gmail.com")).thenReturn(user);
        when(passwordEncoder.encode(any(String.class))).thenReturn("encodedNewPassword");

        String newPassword = authService.resetPassword(request);

        assertNotNull(newPassword);
        assertEquals(8, newPassword.length());
        assertEquals("encodedNewPassword", user.getPassword());
        assertNull(user.getResetToken());
        assertNull(user.getResetTokenExpiry());
        verify(userRepository).save(user);
        verify(mailSender).send(any(org.springframework.mail.SimpleMailMessage.class));
    }
}