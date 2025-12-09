package com.example.babyhands.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.babyhands.entity.MemberEntity;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<MemberEntity, Long> {
    
    @EntityGraph(attributePaths = {"roles"})
    Optional<MemberEntity> findByLoginId(String loginId);
    
}