package com.cosmetics.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "employee")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int employeeID;
    private String lastName;
    private String firstName;
    private String email;
    private String address;
    private String password;
    private String phoneNumber;
    private String avatar;
    private boolean gender;
    private boolean verified;
    private int login_attempts;
    private String aseKey;

    @JsonIgnore
    @JsonManagedReference
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Authorities> authorities;
}
