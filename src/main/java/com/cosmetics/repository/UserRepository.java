package com.cosmetics.repository;

import com.cosmetics.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<Users, Integer> {
    List<Users> findByAuthorities_Role_RoleIDIn(List<String> roleIDs);
    Optional<Users> findByEmail(String email);
    @Query("SELECT COALESCE(MAX(u.userID), 0) FROM Users u")
    Integer findMaxUserId();
    Optional<Users> findByFirstNameAndLastName(String firstName, String lastName);
    Users findByUserID(int userId);
}
