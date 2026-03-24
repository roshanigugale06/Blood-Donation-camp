package com.bloodbank.config;

import com.bloodbank.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Admin Security Configuration
    @Configuration
    @Order(1)
    public static class AdminSecurityConfig {
        @Bean
        public SecurityFilterChain adminFilterChain(HttpSecurity http) throws Exception {
            http.securityMatcher("/admin/**")
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/admin/login", "/css/**").permitAll()
                    .anyRequest().hasRole("ADMIN")
                )
                .formLogin(form -> form
                    .loginPage("/admin/login")
                    .loginProcessingUrl("/admin/login")
                    .defaultSuccessUrl("/admin/dashboard", true)
                    .failureUrl("/admin/login?error=true")
                    .permitAll()
                )
                .logout(logout -> logout
                    .logoutUrl("/admin/logout")
                    .logoutSuccessUrl("/admin/login?logout=true")
                    .deleteCookies("JSESSIONID")
                    .permitAll()
                )
                .csrf(csrf -> csrf.disable());
            
            return http.build();
        }
    }

    // User Security Configuration
    @Configuration
    @Order(2)
    public static class UserSecurityConfig {
        @Bean
        public SecurityFilterChain userFilterChain(HttpSecurity http) throws Exception {
            http.securityMatcher("/user/**")
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/user/login", "/user/register", "/css/**").permitAll()
                    .anyRequest().hasRole("USER")
                )
                .formLogin(form -> form
                    .loginPage("/user/login")
                    .loginProcessingUrl("/user/login")
                    .defaultSuccessUrl("/user/dashboard", true)
                    .failureUrl("/user/login?error=true")
                    .permitAll()
                )
                .logout(logout -> logout
                    .logoutUrl("/user/logout")
                    .logoutSuccessUrl("/user/login?logout=true")
                    .deleteCookies("JSESSIONID")
                    .permitAll()
                )
                .csrf(csrf -> csrf.disable());
            
            return http.build();
        }
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }
}