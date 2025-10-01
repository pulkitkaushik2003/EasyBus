package com.example.ticket.configuration;

import com.example.ticket.Admin.Service.AdminService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class Userconfig {

    private final AdminService adminService;

    public Userconfig(AdminService adminService) {
        this.adminService = adminService;
    }

    // ---------------- Password Encoder ----------------
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ---------------- Auth Provider ----------------
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(adminService); // AdminService implements UserDetailsService
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    // ---------------- Security Filter Configuration ----------------
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // ✅ CSRF ignore only for APIs (needed for JS fetch POST)
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))

            .authenticationProvider(authenticationProvider())

            .authorizeHttpRequests(auth -> auth
                // ✅ Public Pages
                .requestMatchers("/register", "/saveUser", "/login", "/resetPassword").permitAll()

                // ✅ Static Assets
                .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()

                // ✅ Admin-only Pages
                .requestMatchers("/admin/**", "/addRoute", "/editRoute/**",
                                 "/deleteRoute/**", "/AddRoute", "/viewRoutes", "/Showbooking")
                .hasRole("ADMIN")

                // ✅ User Pages and APIs
                .requestMatchers("/dashboard", "/userroutes", "/booking/**",
                                 "/booking-confirmation", "/my-bookings", "/payment/booking/**",
                                 "/api/**")
                .hasRole("USER")

                .anyRequest().authenticated()
            )

            // ✅ Login Page
            .formLogin(form -> form
                .loginPage("/")
                .defaultSuccessUrl("/default", true)
                .permitAll()
            )

            // ✅ Logout
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );

        return http.build();
    }
}
