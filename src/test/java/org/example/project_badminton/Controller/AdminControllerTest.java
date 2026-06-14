package org.example.project_badminton.Controller;

import org.example.project_badminton.controller.AdminController;
import org.example.project_badminton.dto.request.UpdateUserRequest;
import org.example.project_badminton.dto.response.ApiResponse;
import org.example.project_badminton.dto.response.UserDTO;
import org.example.project_badminton.entity.User;
import org.example.project_badminton.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;



@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private AdminController adminController;


    private UserDTO userDTO;

    @BeforeEach

    void setUp() {
        userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setUsername("thanhdinh143");
        userDTO.setEmail("thanhdinh143@gmail.com");
        userDTO.setPhone("0912412412");
        userDTO.setFullName("Dinh Dinh Thanh");
        userDTO.setRole("ROLE_ADMIN");
        userDTO.setActive(true);
    }

    @Test
    void getAllUser() {
        List<UserDTO> users = List.of(userDTO);
        Page<UserDTO> page = new PageImpl<>(users, PageRequest.of(0,5), users.size());
        when(userService.getAllUsers(anyString(), any(Pageable.class))).thenReturn(page);
        ResponseEntity<ApiResponse<List<UserDTO>>> response =
                adminController.getAllUser(
                        "",
                        0,
                        5,
                        "id",
                        "desc"

                );
        assertEquals(200, response.getStatusCode().value());

        assertTrue(response.getBody().isSuccess());

        assertEquals("Lấy danh sách thành công",

                response.getBody().getMessage());

        assertEquals(1, response.getBody().getData().size());

        verify(userService, times(1)).getAllUsers(anyString(), any(Pageable.class));

    }




    @Test

    void updateUser() {

        Long userId = 1L;

        UpdateUserRequest request = new UpdateUserRequest();

        when(userService.updateUser(eq(userId), any(UpdateUserRequest.class)))

                .thenReturn(userDTO);

        ResponseEntity<ApiResponse<UserDTO>> response =

                adminController.updateUser(userId, request);

        assertEquals(200, response.getStatusCode().value());

        assertTrue(response.getBody().isSuccess());

        assertEquals("Update thành công",

                response.getBody().getMessage());

        assertEquals(userDTO,

                response.getBody().getData());

        verify(userService, times(1))

                .updateUser(userId, request);

    }

    @Test
    void getUserById() {
        Long userId = 1L;

        when(userService.getUserById(userId)).thenReturn(userDTO);


        ResponseEntity<ApiResponse<UserDTO>> response =

                adminController.getUserById(userId);

        assertEquals(200, response.getStatusCode().value());

        assertTrue(response.getBody().isSuccess());

        assertEquals("Lấy user thành công",

                response.getBody().getMessage());

        assertEquals(userDTO,

                response.getBody().getData());

                verify(userService, times(1))
                .getUserById(userId);

    }
    @Test

    void toggleUserStatus() {

        Long userId = 1L;

        doNothing().when(userService)

                .toggleUserStatus(userId);

        ResponseEntity<ApiResponse<Void>> response =

                adminController.toggleUserStatus(userId);

        assertEquals(200, response.getStatusCode().value());

        assertTrue(response.getBody().isSuccess());

        assertEquals("toggleUserStatus thành công",

                response.getBody().getMessage());

        assertNull(response.getBody().getData());

        verify(userService, times(1))

                .toggleUserStatus(userId);
    }

}
