package com.vsp.expenseclaims.service;

import com.vsp.expenseclaims.config.UserContext;
import com.vsp.expenseclaims.dto.UserResponse;
import com.vsp.expenseclaims.entity.User;
import com.vsp.expenseclaims.exception.BusinessException;
import com.vsp.expenseclaims.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        Long rawId = UserContext.getUserId();
        final Long targetUserId = (rawId != null) ? rawId : 1L;
        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new BusinessException("User not found with ID: " + targetUserId));
        return new UserResponse(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getTeamMembers(Long managerId) {
        return userRepository.findByManagerId(managerId).stream()
                .map(UserResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findByActiveTrue().stream()
                .map(UserResponse::new)
                .collect(Collectors.toList());
    }
}
