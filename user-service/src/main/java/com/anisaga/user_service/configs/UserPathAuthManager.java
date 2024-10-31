package com.anisaga.user_service.configs;

import com.anisaga.user_service.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;


@Slf4j
@Component
public class UserPathAuthManager implements AuthorizationManager<RequestAuthorizationContext> {
    @Autowired
    JwtUtil jwtUtil;

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, RequestAuthorizationContext object) {
        try {
            HttpServletRequest request = object.getRequest();
            String servletPath = request.getServletPath();
            String userName = getUserNameFromServletPath(servletPath);
            if (userName.isBlank()) return new AuthorizationDecision(false);
            String requestHeader = object.getRequest().getHeader("Authorization");
            String token = requestHeader.substring(7);
            String userNameFromToken = jwtUtil.getUserNameFromToken(token);
            Boolean isAdmin = request.isUserInRole("ROLE_ADMIN");
            return new AuthorizationDecision(userNameFromToken.equals(userName) || isAdmin); // allow access to specific user which is having the same username in path and to all admins
        } catch (Exception e) {
            log.error("Exception occurred in AuthorizationDecision ", e);
            return new AuthorizationDecision(false);
        }
    }

    @Override
    public void verify(Supplier<Authentication> authentication, RequestAuthorizationContext object) {
        AuthorizationManager.super.verify(authentication, object);
    }

    private String getUserNameFromServletPath(String servletPath) {
        return servletPath.split("/")[3];
    }

}
