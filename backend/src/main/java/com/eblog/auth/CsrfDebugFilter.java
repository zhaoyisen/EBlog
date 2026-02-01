package com.eblog.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;

public class CsrfDebugFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    
    // Only debug write requests to auth endpoints
    String path = request.getRequestURI();
    if (path.startsWith("/api/v1/auth/") && "POST".equalsIgnoreCase(request.getMethod())) {
        System.out.println("=== CSRF DEBUG: " + request.getMethod() + " " + path + " ===");
        
        // Log Headers
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            if (name.equalsIgnoreCase("X-XSRF-TOKEN") || name.equalsIgnoreCase("Cookie") || name.equalsIgnoreCase("Origin") || name.equalsIgnoreCase("Referer")) {
                System.out.println("Header [" + name + "]: " + request.getHeader(name));
            }
        }
        
        // Log Cookies
        if (request.getCookies() != null) {
            Arrays.stream(request.getCookies()).forEach(c -> 
                System.out.println("Cookie: " + c.getName() + "=" + c.getValue() + ", Path=" + c.getPath() + ", Domain=" + c.getDomain())
            );
        } else {
            System.out.println("No Cookies found in request object.");
        }
        System.out.println("==========================================");
    }

    filterChain.doFilter(request, response);
  }
}
