package com.vsp.expenseclaims.controller;

import com.vsp.expenseclaims.config.UserContext;
import com.vsp.expenseclaims.dto.UserResponse;
import com.vsp.expenseclaims.entity.User;
import com.vsp.expenseclaims.entity.enums.UserRole;
import com.vsp.expenseclaims.exception.BusinessException;
import com.vsp.expenseclaims.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "User management and context endpoints")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user profile")
    public ResponseEntity<UserResponse> getCurrentUser() {
        return ResponseEntity.ok(userService.getCurrentUser());
    }

    @GetMapping("/team")
    @Operation(summary = "Get team members reporting to current manager")
    public ResponseEntity<List<UserResponse>> getTeamMembers() {
        User user = UserContext.getUser();
        if (user == null || user.getRole() != UserRole.MANAGER) {
            throw new BusinessException("Only managers can access team members", HttpStatus.FORBIDDEN);
        }
        return ResponseEntity.ok(userService.getTeamMembers(user.getId()));
    }

    @GetMapping("/all")
    @Operation(summary = "Get all users (for persona switcher demo)")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
}
