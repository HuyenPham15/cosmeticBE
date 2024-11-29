package com.cosmetics.repository;

import com.cosmetics.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Integer> {

     Optional<Employee> findByEmail(String email);

    @Query("SELECT COALESCE(MAX(e.employeeID), 0) FROM Employee e")
    Integer findMaxEmployeeId();

    List<Employee> findByAuthorities_Role_RoleIDIn(List<String> roles);
}
