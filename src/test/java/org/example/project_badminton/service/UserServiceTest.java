package org.example.project_badminton.service;

import org.example.project_badminton.dto.request.UpdateUserRequest;
import org.example.project_badminton.dto.response.UserDTO;
import org.example.project_badminton.entity.User;
import org.example.project_badminton.exception.ResourceNotFoundException;
import org.example.project_badminton.instance.Role;
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

import java.util.List;
import java.util.Optional;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;


    @Test
    void getUserById_Success() {
        User user = new User();
        user.setId(1L);
        user.setUsername("rikkei");
        user.setPassword("123456");
        user.setEmail("bach@gmail.com");
        user.setPhone("0981234567");
        user.setFullName("Nguyen Van Bach");
        user.setRole(Role.ROLE_ADMIN);
        user.setActive(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));


        UserDTO result = userService.getUserById(1L);
        assertEquals("rikkei", result.getUsername());
        verify(userRepository).findById(1L);
    }

    @Test
    void getUserById_NotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(1L));
    }


    @Test
    void updateUser_Success() {
        User user = new User();
        user.setId(1L);
        user.setUsername("bach");
        user.setPassword("123456");
        user.setEmail("bach@gmail.com");
        user.setPhone("0981234567");
        user.setFullName("Nguyen Van Bach");
        user.setRole(Role.ROLE_ADMIN);
        user.setActive(true);


        UpdateUserRequest request = new UpdateUserRequest();
        request.setFullName("Nguyen Van Rikkei");
        request.setPhone("098123411");
        request.setRole(Role.ROLE_ADMIN);
        request.setIsActive(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        UserDTO result = userService.updateUser(1L, request);
        assertEquals("Nguyen Van Rikkei", user.getFullName());
        assertEquals("098123411", user.getPhone());
        assertEquals(Role.ROLE_ADMIN, user.getRole());
        assertTrue(user.isActive());
        verify(userRepository).save(user);

    }

    @Test
    void updateUser_NotFound() {
        UpdateUserRequest request = new UpdateUserRequest();
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.updateUser(1L, request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void toggleUserStatus_FromTrueToFalse() {
        User user = new User();
        user.setId(1L);
        user.setUsername("bach");
        user.setPassword("123456");
        user.setEmail("bach@gmail.com");
        user.setPhone("0981234567");
        user.setFullName("Nguyen Van Bach");
        user.setRole(Role.ROLE_ADMIN);
        user.setActive(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        userService.toggleUserStatus(1L);
        assertFalse(user.isActive());
        verify(userRepository).save(user);
    }


    @Test
    void toggleUserStatus_FromFalseToTrue() {

        User user = new User();
        user.setId(1L);
        user.setUsername("bach");
        user.setPassword("123456");
        user.setEmail("bach@gmail.com");
        user.setPhone("0981234567");
        user.setFullName("Nguyen Van Bach");
        user.setRole(Role.ROLE_ADMIN);

        user.setActive(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.toggleUserStatus(1L);

        assertTrue(user.isActive());

        verify(userRepository).save(user);
    }


    @Test
    void toggleUserStatus_NotFound() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.toggleUserStatus(1L)
        );

        verify(userRepository, never()).save(any());
    }


    @Test
    void getAllUsers_WithoutKeyword() {

        Pageable pageable = PageRequest.of(0, 10);

        User user = new User();
        user.setId(1L);
        user.setUsername("bach");
        user.setPassword("123456");
        user.setEmail("bach@gmail.com");
        user.setPhone("0981234567");
        user.setFullName("Nguyen Van Bach");
        user.setRole(Role.ROLE_ADMIN);
        user.setActive(true);

        Page<User> page =
                new PageImpl<>(List.of(user));

        when(userRepository.findAll(pageable))
                .thenReturn(page);

        Page<UserDTO> result =
                userService.getAllUsers(null, pageable);

        assertEquals(1, result.getTotalElements());

        verify(userRepository).findAll(pageable);
    }

    @Test
    void getAllUsers_WithKeyword() {

        Pageable pageable = PageRequest.of(0, 10);

        User user = new User();
        user.setId(1L);
        user.setUsername("bachquach");
        user.setPassword("123456");
        user.setEmail("bach@gmail.com");
        user.setPhone("0981234567");
        user.setFullName("Nguyen Van Bach");
        user.setRole(Role.ROLE_ADMIN);
        user.setActive(true);

        Page<User> page = new PageImpl<>(List.of(user));

        when(userRepository
                .findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                        "bachquach",
                        "bachquach",
                        pageable))
                .thenReturn(page);

        Page<UserDTO> result = userService.getAllUsers("bachquach", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        UserDTO dto = result.getContent().get(0);

        assertEquals(1L, dto.getId());
        assertEquals("bachquach", dto.getUsername());
        assertEquals("bach@gmail.com", dto.getEmail());
        assertEquals("0981234567", dto.getPhone());
        assertEquals("Nguyen Van Bach", dto.getFullName());
        assertEquals("ROLE_ADMIN", dto.getRole());

        verify(userRepository)
                .findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                        "bachquach",
                        "bachquach",
                        pageable);
    }


}