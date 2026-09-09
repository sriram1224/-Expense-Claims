package com.vsp.expenseclaims.config;

import com.vsp.expenseclaims.entity.User;
import com.vsp.expenseclaims.entity.enums.UserRole;
import com.vsp.expenseclaims.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class UserContextFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    public UserContextFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String userIdHeader = request.getHeader("X-User-Id");
            String roleHeader = request.getHeader("X-Role");

            Long userId = null;
            UserRole role = null;
            User user = null;

            if (userIdHeader != null && !userIdHeader.trim().isEmpty()) {
                try {
                    userId = Long.parseLong(userIdHeader.trim());
                    user = userRepository.findById(userId).orElse(null);
                    if (user != null) {
                        role = user.getRole(); // Authoritative: ALWAYS from database
                    }
                } catch (NumberFormatException ignored) {}
            }

            // Fallback default user only if no header provided at all (for Swagger / initial load)
            if (userId == null && user == null) {
                user = userRepository.findById(4L).orElse(null); // Default to Ram (STAFF)
                if (user != null) {
                    userId = user.getId();
                    role = user.getRole();
                }
            }

            // Enforce: role must match user in DB; non-existent users get NO role
            if (user != null) {
                role = user.getRole();
            } else {
                role = null;
            }

            UserContext.setContext(userId, role, user);
            filterChain.doFilter(request, response);
        } finally {
            UserContext.clear();
        }
    }
}
