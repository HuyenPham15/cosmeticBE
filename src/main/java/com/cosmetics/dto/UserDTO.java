package com.cosmetics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private int id;
    private String email;
    private boolean verified;
    private String role;
    private Set<String> authorities;
    private List<AddressDTO> addresses;
    private String lastName;
    private String firstName;
    private String phoneNumber;
    private String password;
    private String avatar;
    private boolean gender;
    private float total_point;
    private int login_attempts;
}
