package br.edu.projetoreuso.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import br.edu.projetoreuso.usuario.UsuarioRepository;

@Configuration
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/eventos/*/inscrever", "/minhas-inscricoes", "/minhas-inscricoes/**").hasRole("ALUNO")
                        .requestMatchers("/", "/login", "/cadastro", "/eventos", "/eventos/**", "/css/**", "/js/**", "/images/**", "/erro").permitAll()
                        .requestMatchers("/centro-academico/**").hasRole("CENTRO_ACADEMICO")
                        .requestMatchers("/usuarios/**").hasAnyRole("UNIVERSIDADE", "CENTRO_ACADEMICO")
                        .requestMatchers("/minha-conta/**", "/painel").authenticated()
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/painel", true)
                        .permitAll())
                .logout(logout -> logout.logoutSuccessUrl("/").permitAll())
                .exceptionHandling(errors -> errors.accessDeniedPage("/erro?status=403"))
                .build();
    }

    @Bean
    UserDetailsService userDetailsService(UsuarioRepository usuarios) {
        return email -> usuarios.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
