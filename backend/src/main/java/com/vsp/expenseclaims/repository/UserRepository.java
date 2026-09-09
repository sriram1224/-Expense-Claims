package com.vsp.expenseclaims.repository;

import com.vsp.expenseclaims.entity.User;
import com.vsp.expenseclaims.entity.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    List<User> findByRole(UserRole role);

    List<User> findByManagerId(Long managerId);

    List<User> findByActiveTrue();
}
