package com.cosmetics.repository;

import com.cosmetics.model.Authorities;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuthoritiesRepository extends JpaRepository<Authorities, Integer> {

     List<Authorities> findByEmployee_EmployeeID(Integer employeeID);
}
