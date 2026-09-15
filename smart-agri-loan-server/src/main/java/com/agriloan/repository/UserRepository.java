package com.agriloan.repository;

import com.agriloan.domain.Role;
import com.agriloan.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    List<User> findAllByRole(Role role);

    List<User> findAllByOrderByIdAsc();
}
