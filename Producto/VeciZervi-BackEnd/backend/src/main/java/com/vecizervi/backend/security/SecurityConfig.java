package com.vecizervi.backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // ── Rutas públicas (sin token) — SOLO lo estrictamente necesario
                // antes de que el usuario tenga sesión iniciada ──────────────────
                .requestMatchers(
                    "/api/usuarios/login",
                    "/api/usuarios/registro",
                    "/api/usuarios/recuperar-clave",
                    "/api/usuarios/verificar-token",
                    "/api/usuarios/nueva-clave",
                    "/ws/**"
                ).permitAll()
                // Todo lo demás (trabajos, mensajes, reseñas, postulaciones,
                // perfil de usuario) requiere estar logueado con JWT válido,
                // ya que el flujo de la app siempre pasa por login primero
                // antes de llegar a la pantalla de inicio.
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }
}