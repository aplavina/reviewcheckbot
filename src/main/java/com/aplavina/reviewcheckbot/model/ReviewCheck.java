package com.aplavina.reviewcheckbot.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Cacheable
@Setter
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name = "review_check")
public class ReviewCheck {
    @Id
    @Column(name = "id")
    private String id;
    @Column(name = "text", nullable = false)
    private String text;
    @Column(name = "is_fake")
    private Boolean isFake;
    @Column(name = "fake_score_percentage")
    private Float fakeScorePercentage;
    @ManyToOne
    @JoinColumn(name = "file_id")
    private FileCheck file;
}
