package com.example.babyhands.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "SL_LEARN")
@EntityListeners(AuditingEntityListener.class)
@ToString
@Builder
public class SLLearnEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column(nullable = false)
    private LocalDate learnDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(nullable = false)
    private SignLanguageEntity sl;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(nullable = false)
    private MemberEntity member;

}
