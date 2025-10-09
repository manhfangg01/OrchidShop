package com.orchid_backend.config.security;

import java.util.Collections;
import java.util.Optional;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.orchid_backend.repository.UserRepository;

@Component("userDetailService")
public class CustomUserDetails implements UserDetailsService {
    private final UserRepository userRepository;

    public CustomUserDetails(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("=== TRYING TO LOAD USER: " + username + " ===");

        return userRepository.findByEmail(username)
                .map(user -> {
                    System.out.println("=== USER FOUND ===");
                    System.out.println("Email: " + user.getEmail());
                    System.out.println("CreatedAt: " + user.getCreatedAt());

                    String roleName = Optional.ofNullable(user.getRole())
                            .map(r -> "ROLE_" + r.getName())
                            .orElse("ROLE_USER");

                    return new org.springframework.security.core.userdetails.User(
                            user.getEmail(),
                            user.getPassword() != null ? user.getPassword() : "",
                            Collections.singletonList(new SimpleGrantedAuthority(roleName)));
                })
                .orElseThrow(() -> {
                    System.out.println("=== USER NOT FOUND IN DB ===");
                    return new UsernameNotFoundException("User not found");
                });
    }

}
