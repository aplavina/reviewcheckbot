package com.aplavina.reviewcheckbot.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name = "file_check")
public class FileCheck {
    @Id
    @Column(name = "file_key", nullable = false)
    private String fileKey;
    @Column(name = "chat_id", nullable = false)
    private String chatId;
    @Column(name = "count")
    private Long reviewsCount;
    @OneToMany(mappedBy = "file", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewCheck> reviewChecks;
}
