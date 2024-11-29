package com.cosmetics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddressDTO {
    private int addressID;
    private String specificAddress;
    private String phoneNumber;
    private String firstName;
    private String lastName;
    private String street;
    private String ward;
    private String district;
    private String city;
    private boolean defaultAddress;
}

