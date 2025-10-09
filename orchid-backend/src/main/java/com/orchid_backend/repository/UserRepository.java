package com.orchid_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.orchid_backend.model.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    public Optional<User> findByEmail(String email);

    public Optional<User> findByFullName(String fullName);

    public Boolean existsByEmail(String email);

    public Boolean existsByFullName(String fullName);

}
