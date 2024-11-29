package com.cosmetics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeDTO {
    private int id;
    private String email;
    private boolean verified;
    private String address;
    private String role;
    private Set<String> authorities;
    private String lastName;
    private String firstName;
    private String phoneNumber;
    private String password;
    private String avatar;
    private boolean gender;
    private int login_attempts;
}
