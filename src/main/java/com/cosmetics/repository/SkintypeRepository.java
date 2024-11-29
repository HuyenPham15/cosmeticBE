package com.cosmetics.repository;

import com.cosmetics.model.SkinType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SkintypeRepository extends JpaRepository<SkinType, Integer> {
    SkinType findBySkinName(String skinName);
}
