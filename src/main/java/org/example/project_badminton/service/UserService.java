package org.example.project_badminton.service;


import lombok.RequiredArgsConstructor;
import org.example.project_badminton.dto.request.UpdateUserRequest;
import org.example.project_badminton.dto.response.UserDTO;
import org.example.project_badminton.entity.User;
import org.example.project_badminton.exception.ConflictException;
import org.example.project_badminton.exception.ResourceNotFoundException;
import org.example.project_badminton.instance.Role;
import org.example.project_badminton.mapper.UserMapper;
import org.example.project_badminton.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;


    public Page<UserDTO> getAllUsers(String keyword, Pageable pageable) {
        Page<User> users;
        if (keyword != null && !keyword.trim().isEmpty()) {
            users = userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(keyword, keyword, pageable);
        } else {
            users = userRepository.findAll(pageable);
        }
        return users.map(UserMapper::toDTO);
    }

    public UserDTO getUserById(Long id){
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not Found !"));
        return UserMapper.toDTO(user);
    }


    @Transactional
    public UserDTO updateUser(Long id, UpdateUserRequest request){
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not Found !"));

        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setRole(request.getRole());
        user.setActive(request.getIsActive());
        userRepository.save(user);

        return UserMapper.toDTO(user);
    }


    @Transactional
    public void toggleUserStatus(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not Found !"));
        user.setActive(!user.isActive());
        userRepository.save(user);

    }
}
