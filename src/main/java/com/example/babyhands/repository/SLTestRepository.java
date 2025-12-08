package com.example.babyhands.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.babyhands.entity.SLTestEntity;

@Repository
public interface SLTestRepository extends JpaRepository<SLTestEntity, Long> {
    
}

