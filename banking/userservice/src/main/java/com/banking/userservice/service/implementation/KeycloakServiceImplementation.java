package com.banking.userservice.service.implementation;

import com.banking.userservice.config.KeyCloakManager;
import com.banking.userservice.service.KeycloakService;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KeycloakServiceImplementation implements KeycloakService {
    private final KeyCloakManager keyCloakManager;

    /**
     * Creates a new user in the KeyCloak system.
     *
     * @param  userRepresentation  the user representation object containing the user details
     * @return                     the status code indicating the success or failure of the user creation
     */
    @Override
    public Integer createUser(UserRepresentation userRepresentation) {
        return keyCloakManager.getKeyCloakInstanceWithRealm().users().create(userRepresentation).getStatus();
    }
    /**
     * Retrieves a list of UserRepresentation objects based on the provided email ID.
     *
     * @param  emailId  the email ID of the user
     * @return          a list of UserRepresentation objects
     */
    @Override
    public List<UserRepresentation> readUserByEmail(String emailId) {
        return keyCloakManager.getKeyCloakInstanceWithRealm().users().search(emailId);
    }
    /**
     * Retrieves a list of user representations based on the provided authentication IDs.
     *
     * @param  authIds  a list of authentication IDs
     * @return          a list of user representations
     */
    @Override
    public List<UserRepresentation> readUsers(List<String> authIds) {
        return authIds.stream().map(authId-> {
            UserResource userResource = keyCloakManager.getKeyCloakInstanceWithRealm().users().get(authId);
            return userResource.toRepresentation();
        }).collect(Collectors.toList());
    }

    @Override
    public UserRepresentation readUser(String authId) {
        UserResource userResource = keyCloakManager.getKeyCloakInstanceWithRealm().users().get(authId);
        return userResource.toRepresentation();
    }
    /**
     * Updates the user with the provided user representation.
     *
     * @param  userRepresentation  the user representation to update the user with
     */
    @Override
    public void updateUser(UserRepresentation userRepresentation) {
        keyCloakManager.getKeyCloakInstanceWithRealm().users().get(userRepresentation.getId()).update(userRepresentation);
    }
}
