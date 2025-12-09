package com.example.babyhands.repository;

import com.example.babyhands.entity.AttendanceEntity;
import com.example.babyhands.entity.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface AttendanceRepository extends JpaRepository<AttendanceEntity, Long> {
    boolean existsByLoginDateAndMemberId(LocalDate now, Long id);
}
