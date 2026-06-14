package org.example.project_badminton.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.project_badminton.instance.Role;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {


    @NotBlank(message = "Tên Không được để Trống")
    private String fullName;

    @NotBlank(message = "Phone không được để trống")
    @Size(min = 9, max = 15, message = "invalid Phone")
    private String phone;

    @NotNull(message = "invalid Role")
    private Role role;

    @NotNull(message = "invalid isActive")
    private Boolean isActive;
}
