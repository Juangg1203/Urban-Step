package com.tiendaropa.config;

import com.tiendaropa.security.DetalleUsuarioService;
import com.tiendaropa.security.RedireccionPorRol;
import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final DetalleUsuarioService detalleUsuarioService;
    private final RedireccionPorRol redireccionPorRol;

    public SecurityConfig(DetalleUsuarioService detalleUsuarioService, RedireccionPorRol redireccionPorRol) {
        this.detalleUsuarioService = detalleUsuarioService;
        this.redireccionPorRol = redireccionPorRol;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filtros(HttpSecurity http) throws Exception {
        http
            .userDetailsService(detalleUsuarioService)
            // El chat responde por JSON; se excluye del token CSRF.
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
            .authorizeHttpRequests(reglas -> reglas
                // Necesario para que Spring Security no vuelva a evaluar los
                // forward internos hacia /WEB-INF/jsp/** (evita bucles de redireccion).
                .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()
                .requestMatchers("/", "/catalogo/**", "/producto/**", "/nosotros",
                                 "/politica-datos", "/login", "/registro",
                                 "/recursos/**", "/imagenes/**", "/api/chat/**",
                                 "/api/pagos/**", "/error").permitAll()
                .requestMatchers("/mi-cuenta/**", "/pedidos/**", "/checkout/**",
                                 "/pagos/**").hasRole("CLIENTE")
                // El carrito es publico: se puede armar sin cuenta, se pide login al confirmar.
                .requestMatchers("/carrito/**").permitAll()
                .requestMatchers("/panel/reportes/**", "/panel/historico").hasAnyRole("ADMIN", "JEFE")
                .requestMatchers("/panel/auditoria/**").hasAnyRole("JEFE", "ADMIN")
                .requestMatchers("/panel/aprobaciones/**").hasRole("JEFE")
                // El catalogo (crear, editar, retirar productos) es del JEFE, no del
                // administrador: administrar el sistema no es lo mismo que vender.
                .requestMatchers("/panel/productos/**").hasRole("JEFE")
                // Las cuentas de personal si son del administrador: es justo su trabajo.
                .requestMatchers("/panel/usuarios/**").hasRole("ADMIN")
                .requestMatchers("/panel/pedidos/**").hasAnyRole("EMPLEADO", "JEFE", "ADMIN")
                .requestMatchers("/panel/**").hasAnyRole("ADMIN", "EMPLEADO", "JEFE")
                .anyRequest().authenticated()
            )
            .formLogin(login -> login
                .loginPage("/login")
                .usernameParameter("usuario")
                .passwordParameter("clave")
                .successHandler(redireccionPorRol)
                .failureUrl("/login?error")
                .permitAll()
            )
            .logout(salir -> salir
                .logoutUrl("/salir")
                .logoutSuccessUrl("/?sesion=cerrada")
                .permitAll()
            )
            .exceptionHandling(ex -> ex.accessDeniedPage("/acceso-denegado"));

        return http.build();
    }
}
