package org.example.project_badminton.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.project_badminton.dto.request.UpdateUserRequest;
import org.example.project_badminton.dto.response.ApiResponse;
import org.example.project_badminton.dto.response.UserDTO;
import org.example.project_badminton.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;


    @GetMapping
    public ResponseEntity<ApiResponse<List<UserDTO>>> getAllUser(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ){

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ?Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        List<UserDTO> user = userService.getAllUsers(keyword, pageable).getContent();



        return ResponseEntity.ok(new ApiResponse<>(true, "Lấy danh sách thành công", user));
    }


    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@Valid @PathVariable Long id){
        UserDTO userDTO = userService.getUserById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Lấy user thành công", userDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> updateUser(@Valid @PathVariable Long id, @RequestBody UpdateUserRequest r){
        UserDTO userDTO = userService.updateUser(id, r);
        return ResponseEntity.ok(new ApiResponse<>(true, "Update thành công", userDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> toggleUserStatus(@Valid @PathVariable Long id){
        userService.toggleUserStatus(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "toggleUserStatus thành công", null));
    }

}
