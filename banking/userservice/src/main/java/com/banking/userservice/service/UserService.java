package com.banking.userservice.service;

import com.banking.userservice.model.dto.CreateUser;
import com.banking.userservice.model.dto.UserDto;
import com.banking.userservice.model.dto.UserUpdate;
import com.banking.userservice.model.dto.UserUpdateStatus;
import com.banking.userservice.model.dto.response.Response;

import java.util.List;

public interface UserService {

    Response createUser(CreateUser createUser);
    List<UserDto> readAllUsers();

    UserDto readUser(String authenticationId);

    Response updateUserStatus(Long id, UserUpdateStatus userUpdateStatus);
    Response updateUser(Long id, UserUpdate userUpdate);

    UserDto readUserById(Long userId);
    UserDto readUserByAccountId(String accountId);
}
