package com.cosmetics.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "roles")
public class Roles {
    @Id
    String roleID;
    String roleName;
    String roleDescription;
    @JsonManagedReference
    @OneToMany(mappedBy = "role")
    private List<Authorities> authorities;

}
