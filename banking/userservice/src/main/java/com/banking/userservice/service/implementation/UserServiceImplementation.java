package com.banking.userservice.service.implementation;

import com.banking.userservice.exception.EmptyFields;
import com.banking.userservice.exception.ResourceConflictException;
import com.banking.userservice.exception.ResourceNotFound;
import com.banking.userservice.external.AccountService;
import com.banking.userservice.mapper.UserMapper;
import com.banking.userservice.model.Status;
import com.banking.userservice.model.dto.CreateUser;
import com.banking.userservice.model.dto.UserDto;
import com.banking.userservice.model.dto.UserUpdate;
import com.banking.userservice.model.dto.UserUpdateStatus;
import com.banking.userservice.model.dto.response.Response;
import com.banking.userservice.model.entity.User;
import com.banking.userservice.model.entity.UserProfile;
import com.banking.userservice.model.external.Account;
import com.banking.userservice.service.KeycloakService;
import com.banking.userservice.service.UserService;
import com.banking.userservice.repository.UserRepository;
import com.banking.userservice.utils.FieldChecker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImplementation implements UserService {

    private final UserRepository userRepository;
    private final KeycloakService keycloakService;
    private final AccountService accountService;
    private UserMapper userMapper = new UserMapper();

    @Value("${spring.application.success}")
    private String responseCodeSuccess;
    @Value("${spring.application.not_found}")
    private String responseCodeNotFound;


    /**
     * Creates a new user.
     *
     * @param createUser The user data transfer object containing user information.
     * @return A response indicating the result of the user creation.
     * @throws ResourceConflictException If the emailId is already registered as a user.
     * @throws RuntimeException          If the user with identification number is not found.
     */
    @Override
    public Response createUser(CreateUser createUser) {
        List<UserRepresentation> userRepresentations = keycloakService.readUserByEmail(createUser.getEmailId());
        if (userRepresentations.size() > 0) {
            log.error("This emailId is already registered as a user");
            throw new ResourceConflictException("This emailId is already registered as a user");
        }
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setUsername(createUser.getEmailId());
        userRepresentation.setFirstName(createUser.getFirstName());
        userRepresentation.setLastName(createUser.getLastName());
        userRepresentation.setEmailVerified(false);
        userRepresentation.setEnabled(false);
        userRepresentation.setEmail(createUser.getEmailId());

        CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
        credentialRepresentation.setValue(createUser.getPassword());
        credentialRepresentation.setTemporary(false);
        userRepresentation.setCredentials(Collections.singletonList(credentialRepresentation));

        Integer userCreationResponse = keycloakService.createUser(userRepresentation);
        if (userCreationResponse.equals(201)) {
            List<UserRepresentation> representations = keycloakService.readUserByEmail(createUser.getEmailId());
            UserProfile userProfile = UserProfile.builder()
                    .firstName(createUser.getFirstName())
                    .lastName(createUser.getLastName()).build();
            User user = User.builder()
                    .emailId(createUser.getEmailId())
                    .contactNo(createUser.getContactNumber())
                    .authId(representations.get(0).getId())
                    .identificationNumber(UUID.randomUUID().toString())
                    .userProfile(userProfile)
                    .status(Status.PENDING)
                    .build();
            userRepository.save(user);
            return Response.builder()
                    .responseCode(responseCodeSuccess)
                    .responseMessage("User Created Successfully").build();
        }
        throw new RuntimeException("User with Identification number not found");
    }

    @Override
    public List<UserDto> readAllUsers() {
        List<User> users = userRepository.findAll();
        Map<String, UserRepresentation> userRepresentationMap =
                keycloakService.readUsers(users.stream().map(user -> user.getAuthId()).collect(Collectors.toList()))
                        .stream().collect(Collectors.toMap(UserRepresentation::getId, Function.identity()));


        return users.stream().map(
                user -> {
                    UserDto userDto = userMapper.convertToDto(user);
                    UserRepresentation userRepresentation = userRepresentationMap.get(user.getAuthId());
                    userDto.setUserId(user.getUserId());
                    userDto.setEmailId(userRepresentation.getEmail());
                    userDto.setIdentificationNumber(user.getIdentificationNumber());
                    return userDto;
                }
        ).collect(Collectors.toList());
    }

    /**
     * Reads a user from the database using the provided authId.
     *
     * @param authenticationId the authentication id of the user
     * @return the UserDto object representing the user
     * @throws ResourceNotFound if the user is not found on the server
     */
    @Override
    public UserDto readUser(String authenticationId) {
        User user = userRepository.findUserByAutId(authenticationId).orElseThrow(() -> new ResourceNotFound("User Not Found"));
        UserRepresentation userRepresentation = keycloakService.readUser(authenticationId);
        UserDto userDto = userMapper.convertToDto(user);
        userDto.setEmailId(userRepresentation.getEmail());
        return userDto;
    }

    /**
     * Updates the status of a user.
     *
     * @param id               The ID of the user.
     * @param userUpdateStatus The updated user status.
     * @return The response indicating the success of the update.
     * @throws ResourceNotFound If the user is not found.
     * @throws EmptyFields      If the user has empty fields.
     */
    @Override
    public Response updateUserStatus(Long id, UserUpdateStatus userUpdateStatus) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFound("User Not Found"));
        if (FieldChecker.hasEmptyFields(user)) {
            log.error("User is not updated completely");
            throw new EmptyFields("please updated the user", responseCodeNotFound);
        }
        if (userUpdateStatus.getStatus().equals(Status.APPROVED)) {
            UserRepresentation userRepresentation = keycloakService.readUser(user.getAuthId());
            userRepresentation.setEnabled(true);
            userRepresentation.setEmailVerified(true);
            keycloakService.updateUser(userRepresentation);
        }
        user.setStatus(userUpdateStatus.getStatus());
        userRepository.save(user);

        return Response.builder()
                .responseMessage("User Updated Successfully")
                .responseCode(responseCodeSuccess).build();
    }

    @Override
    public Response updateUser(Long id, UserUpdate userUpdate) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFound("User Not Found"));
        user.setContactNo(userUpdate.getContactNo());
        BeanUtils.copyProperties(userUpdate, user.getUserProfile());
        userRepository.save(user);

        return Response.builder()
                .responseCode(responseCodeSuccess)
                .responseMessage("User Updated Sucessfully").build();
    }

    @Override
    public UserDto readUserById(Long userId) {

        return userRepository.findById(userId).map(user -> userMapper.convertToDto(user)).orElseThrow(
                () -> new ResourceNotFound("User Not Found on the Server")
        );
    }

    @Override
    public UserDto readUserByAccountId(String accountId) {
        ResponseEntity<Account> response = accountService.readByAccountNumber(accountId);
        if (Objects.isNull(response.getBody())) {
            throw new ResourceNotFound("account not found on the server");
        }
        Long userId = response.getBody().getUserId();

        return userRepository.findById(userId).map(user -> userMapper.convertToDto(user))
                .orElseThrow(() -> new ResourceNotFound("User Not Found"));
    }
}
