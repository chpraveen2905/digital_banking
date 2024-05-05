package com.banking.userservice.controller;

import com.banking.userservice.model.dto.CreateUser;
import com.banking.userservice.model.dto.UserDto;
import com.banking.userservice.model.dto.UserUpdate;
import com.banking.userservice.model.dto.UserUpdateStatus;
import com.banking.userservice.model.dto.response.Response;
import com.banking.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Creates a new user.
     *
     * @param createUser the user data transfer object
     * @return the response entity containing the response
     */
    @PostMapping("/register")
    public ResponseEntity<Response> createUser(@RequestBody CreateUser createUser) {
        log.info("Creating user with {}", createUser.toString());
        return ResponseEntity.ok(userService.createUser(createUser));
    }

    /**
     * Retrieves all users.
     *
     * @return The list of user DTOs
     */
    @GetMapping
    public ResponseEntity<List<UserDto>> readAllUsers() {
        log.info("Request Received to read All Users");
        return ResponseEntity.ok(userService.readAllUsers());
    }

    /**
     * Retrieves a user by their authentication ID.
     *
     * @param authId The authentication ID of the user.
     * @return The response entity containing the user DTO.
     */
    @GetMapping("auth/{authId}")
    public ResponseEntity<UserDto> readUserByAuthId(@PathVariable String authId) {
        log.info("Reading User by Auth Id");
        return ResponseEntity.ok(userService.readUser(authId));
    }

    /**
     * Updates the status of a user.
     *
     * @param id     The ID of the user to update.
     * @param status The updated status of the user.
     * @return The response entity containing the updated user and HTTP status.
     */
    @PatchMapping("/{id}")
    public ResponseEntity<Response> updateUserStatus(@PathVariable Long id, @RequestBody UserUpdateStatus status) {
        log.info("Updating user status with : {}", status.toString());
        return new ResponseEntity<Response>(userService.updateUserStatus(id, status), HttpStatus.OK);
    }

    /**
     * Updates a user with the given ID.
     *
     * @param id         The ID of the user to update.
     * @param userUpdate The updated user information.
     * @return The response with the updated user information.
     */

    @PutMapping("/{id}")
    public ResponseEntity<Response> userUpdate(@PathVariable Long id, @RequestBody UserUpdate userUpdate) {
        log.info("Updating user with : {}", userUpdate.toString());
        return new ResponseEntity<Response>(userService.updateUser(id, userUpdate), HttpStatus.OK);
    }

    /**
     * Retrieves a user by their ID.
     *
     * @param userId the ID of the user to retrieve
     * @return the user details as a ResponseEntity
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> readUserById(@PathVariable Long userId) {
        log.info("Read user with : {}", userId);
        return ResponseEntity.ok(userService.readUserById(userId));
    }

    /**
     * Retrieves the user with the specified account ID.
     *
     * @param accountId The account ID of the user to retrieve.
     * @return The user DTO associated with the account ID.
     */
    @GetMapping("/accounts/{accountId}")
    public ResponseEntity<UserDto> readUserByAccountId(@PathVariable String accountId) {
        log.info("Read user with Account Id: {}", accountId);
        return ResponseEntity.ok(userService.readUserByAccountId(accountId));
    }

}
