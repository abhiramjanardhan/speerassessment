package com.assessment.speernotes.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserAuthDto {
    private String username;
    @NotNull
    @Email
    private String email;
    private String password;
}
