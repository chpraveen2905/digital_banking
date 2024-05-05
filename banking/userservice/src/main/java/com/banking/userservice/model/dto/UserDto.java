package com.banking.userservice.model.dto;

import com.banking.userservice.model.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto {
    private Long userId;
    private String emailId;
    private String password;
    private String identificationNumber;
    private String authId;
    private Status status;
    private UserProfileDto userProfileDto;

}
