package com.aplavina.reviewcheckbot.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
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
    @OneToMany(mappedBy = "file")
    private List<ReviewCheck> reviewChecks;
}
