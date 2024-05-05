package com.banking.userservice.repository;

import com.banking.userservice.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * Finds a user by their authentication ID.
     *
     * @param  authId  the authentication ID of the user
     * @return         an optional containing the user if found, otherwise empty
     */
    Optional<User> findUserByAutId(String authId);
}
