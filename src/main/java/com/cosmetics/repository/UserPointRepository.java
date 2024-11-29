package com.cosmetics.repository;

import com.cosmetics.model.UserPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPointRepository extends JpaRepository<UserPoint, Integer> {
}
