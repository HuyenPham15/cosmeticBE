package com.cosmetics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignupDTO {
    private String first_name;
    private String last_name;
    private String email;
    private String password;
    private String confirmPassword;

    private boolean verified=false;
    // Getters and setters
}

