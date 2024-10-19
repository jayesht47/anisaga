package com.anisaga.user_service.filters;

import com.anisaga.user_service.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    UserDetailsService userDetailService;

    @Autowired
    JwtUtil jwtUtil;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        final String requestHeader = request.getHeader("Authorization");

        if (StringUtils.startsWithIgnoreCase(requestHeader, "Bearer ")) {
            String token = requestHeader.substring(7);
            try {
                String userName = jwtUtil.getUserNameFromToken(token);
                if (StringUtils.hasLength(userName) && null == SecurityContextHolder.getContext().getAuthentication()) {
                    UserDetails userDetails = userDetailService.loadUserByUsername(userName);

                    if (userDetails != null && jwtUtil.isTokenValid(token, userDetails)) {
                        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                    }
                }
            } catch (IllegalArgumentException e) {
                logger.error("Unable to fetch JWT", e);
            } catch (ExpiredJwtException e) {
                logger.error("JWT Expired", e);
            } catch (Exception e) {
                logger.error("Exception occurred in doFilterInternal", e);
            }
        } else {
            logger.warn("JWT not beginning with Bearer string");
        }
        filterChain.doFilter(request, response);

    }
}
