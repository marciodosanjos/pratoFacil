package com.example.vendeFacil.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

// Configuracao central de seguranca.
// Dois papeis: ADMIN (empreendedor) e CLIENTE (cliente final com conta).
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // CSRF desabilitado para simplificar o consumo da API por clientes HTTP
            // (Postman/app mobile) e o uso do console H2. Trade-off documentado no artigo.
            .csrf(csrf -> csrf.disable())
            // Permite o console H2, que e renderizado dentro de um <frame>.
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
            .authorizeHttpRequests(auth -> auth
                // Paginas e recursos publicos
                .requestMatchers("/", "/pratos", "/pratos/**",
                                 "/login", "/cadastro",
                                 "/css/**", "/js/**", "/images/**", "/webjars/**",
                                 "/h2-console/**",
                                 "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                // Leitura publica do cardapio pela API REST (RF01)
                .requestMatchers(HttpMethod.GET, "/api/pratos/**").permitAll()
                // Area e operacoes do empreendedor (ADMIN)
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/pratos/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/pratos/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/pratos/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/pedidos/*/status").hasRole("ADMIN")
                // Qualquer outra rota exige usuario autenticado (cliente ou admin)
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/pratos", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )
            // Permite tambem HTTP Basic, para testar os endpoints protegidos no Postman.
            .httpBasic(basic -> {});
        return http.build();
    }
}
