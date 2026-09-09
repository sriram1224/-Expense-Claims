package com.vsp.expenseclaims.config;

import com.vsp.expenseclaims.entity.User;
import com.vsp.expenseclaims.entity.enums.UserRole;

public class UserContext {

    private static final ThreadLocal<Long> currentUserId = new ThreadLocal<>();
    private static final ThreadLocal<UserRole> currentUserRole = new ThreadLocal<>();
    private static final ThreadLocal<User> currentUser = new ThreadLocal<>();

    public static void setContext(Long userId, UserRole role, User user) {
        currentUserId.set(userId);
        currentUserRole.set(role);
        currentUser.set(user);
    }

    public static Long getUserId() {
        return currentUserId.get();
    }

    public static UserRole getUserRole() {
        return currentUserRole.get();
    }

    public static User getUser() {
        return currentUser.get();
    }

    public static void clear() {
        currentUserId.remove();
        currentUserRole.remove();
        currentUser.remove();
    }
}
