package org.example.splitwalletserver.server.config;

import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.example.splitwalletserver.server.security.JWTUtil;
import org.example.splitwalletserver.server.security.UserDetailsImpl;
import org.example.splitwalletserver.server.services.UserDetailsServiceImpl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;


import java.io.IOException;

@Component
@AllArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsServiceImpl;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if(authHeader != null && !authHeader.trim().isEmpty() && authHeader.startsWith("Bearer ")){
            String jwt = authHeader.substring(7);
            if (jwt.trim().isEmpty()){
                response.sendError(HttpServletResponse.SC_BAD_REQUEST,"JWT token is empty");
                return;
            }

            try {
                if (!jwtUtil.isTokenValid(jwt)) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST,"Invalid JWT token");
                    return;
                }

                String name = jwtUtil.validateTokenRetrieveClaim(jwt);
                UserDetailsImpl userDetailsImpl = (UserDetailsImpl) userDetailsServiceImpl.loadUserByUsername(name);

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetailsImpl, userDetailsImpl.getPassword(),
                                userDetailsImpl.getAuthorities());

                if(SecurityContextHolder.getContext().getAuthentication() == null){
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (JWTVerificationException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST,"JWT token Verification Exception");
            } catch (UsernameNotFoundException e) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND,"JWT token Username Not Found");
            }
        }
        filterChain.doFilter(request,response);
    }
}
