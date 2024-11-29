package com.cosmetics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PasswordResetDTO {
    private String email;
    private String oldPassword;
    private String newPassword;
    private String confirmNewPassword;

}
