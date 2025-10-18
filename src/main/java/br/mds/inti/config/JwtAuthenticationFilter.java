package br.mds.inti.config;

import java.io.IOException;

import br.mds.inti.service.JwtService;
import br.mds.inti.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);// pega token
        String email = jwtService.validateToken(token);// retorna email cadastrado

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {// se for email e n tiver
                                                                                              // autenticação em curso
            var userdetails = userDetailsService.loadUserByUsername(email);// carrega user
            var authToken = new UsernamePasswordAuthenticationToken(userdetails, null, userdetails.getAuthorities());// usuario
                                                                                                                     // autenticado
            SecurityContextHolder.getContext().setAuthentication(authToken);// informa qual o user autenticado na req
        }
        filterChain.doFilter(request, response); // continua o filtro
    }
}
// rev