package org.example.project_badminton.service;

import lombok.RequiredArgsConstructor;
import org.example.project_badminton.entity.User;
import org.example.project_badminton.repository.UserRepository;
import org.example.project_badminton.security.CustomUserDetails;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            User user = userRepository.findByUsername(username);
            if (user == null) {
                throw new UsernameNotFoundException("Không tìm thấy tài khoản: " + username);
            }
            return new CustomUserDetails(user);
        } catch (Exception e) {
            throw new UsernameNotFoundException("Lỗi khi tìm tài khoản", e);
        }
    }


}