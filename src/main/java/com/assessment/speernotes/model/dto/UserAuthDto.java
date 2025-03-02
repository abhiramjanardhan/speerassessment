package com.assessment.speernotes.model.dto;

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

    public UserAuthDto(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }
}
