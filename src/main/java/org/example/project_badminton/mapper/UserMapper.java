package org.example.project_badminton.mapper;

import org.example.project_badminton.dto.response.UserDTO;
import org.example.project_badminton.entity.User;

public class UserMapper {
    public static UserDTO toDTO(User user) {
        if (user == null) {
            return null;
        }

        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .isActive(user.isActive())
                .createdAt(user.getCreatedAt())
                // Tuyệt đối KHÔNG map trường password ra đây
                .build();
    }
}
