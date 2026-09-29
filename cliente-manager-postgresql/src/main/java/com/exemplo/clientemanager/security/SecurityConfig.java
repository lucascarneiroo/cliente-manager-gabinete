package com.exemplo.clientemanager.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Configuracao central de seguranca. Nao usa WebSecurityConfigurerAdapter
 * (descontinuado); usa a abordagem atual baseada em SecurityFilterChain.
 *
 * Todas as paginas, operacoes e APIs exigem login, exceto os recursos
 * publicos essenciais (login, estaticos, erro) e o webhook do WhatsApp,
 * que e validado por assinatura dentro do proprio controller.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(CustomUserDetailsService userDetailsService,
                                                              PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        // Nao revela se o problema foi o usuario ou a senha.
        provider.setHideUserNotFoundExceptions(true);
        return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, DaoAuthenticationProvider authenticationProvider) throws Exception {
        http
            .authenticationProvider(authenticationProvider)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/login",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/error",
                        "/webhook/whatsapp/**"
                ).permitAll()
                .requestMatchers("/usuarios/**").hasRole("ADMIN")
                .requestMatchers(new AntPathRequestMatcher("/api/**")).authenticated()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?erro")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?sair")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .sessionManagement(session -> session
                // Gera uma nova sessao a cada login, evitando fixacao de sessao.
                .sessionFixation(fixation -> fixation.migrateSession())
                .maximumSessions(1)
                .expiredUrl("/login?expirada")
            )
            .exceptionHandling(handling -> handling
                .accessDeniedPage("/acesso-negado")
            )
            // O webhook do WhatsApp precisa aceitar POSTs assinados pela Meta sem
            // formulario/token CSRF de navegador; a autenticidade e validada pela
            // assinatura HMAC (X-Hub-Signature-256) dentro do proprio controller.
            .csrf(csrf -> csrf
                .ignoringRequestMatchers(new AntPathRequestMatcher("/webhook/whatsapp/**"))
            )
            .headers(headers -> headers
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                .contentTypeOptions(contentTypeOptions -> {})
            );

        return http.build();
    }
}
