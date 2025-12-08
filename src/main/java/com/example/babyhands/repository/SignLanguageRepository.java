package com.example.babyhands.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.babyhands.entity.SignLanguageEntity;
import java.util.Optional;

@Repository
public interface SignLanguageRepository extends JpaRepository<SignLanguageEntity, Long> {
    
    Optional<SignLanguageEntity> findById(Long id);
    
}

