package org.example.project_badminton.repository;

import org.example.project_badminton.entity.User;
import org.example.project_badminton.instance.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public  interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
    User findByEmail(String email);
    User findByResetToken(String resetToken);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    List<User> findByRole(Role role);
    Page<User> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                String name,
                String manufacturer,
                Pageable pageable
        );

}
