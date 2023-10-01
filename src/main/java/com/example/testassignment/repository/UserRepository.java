package com.example.testassignment.repository;

import com.example.testassignment.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    void deleteByEmail(String email);

    List<User> findAllByEmail(String email);

    List<User> findAllByBirthDateBetween(LocalDate from, LocalDate to);
}
