package com.cosmetics.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "authorities")
public class Authorities {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int authId;

    // Quan hệ với Users (có thể null nếu Employee được thiết lập)
    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "userid", referencedColumnName = "userID", nullable = true)
    private Users users;

    // Quan hệ với Employee (có thể null nếu Users được thiết lập)
    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "employee_id", referencedColumnName = "employeeID", nullable = true)
    private Employee employee;

    // Quan hệ với Roles (bắt buộc)
    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "roleid", referencedColumnName = "roleID", nullable = false)
    private Roles role;
}
