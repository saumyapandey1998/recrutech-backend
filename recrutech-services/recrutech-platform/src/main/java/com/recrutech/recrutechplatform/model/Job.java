package com.recrutech.recrutechplatform.model;

import com.recrutech.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "job")
@Getter
@Setter
@NoArgsConstructor
public class Job extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(length = 255)
    private String location;

    @Column(name = "created_by")
    private String createdBy;

    private boolean active;

    @Builder
    public Job(String id, String title, String description, String location, LocalDateTime createdAt, String createdBy, boolean active) {
        this.setId(id);
        this.title = title;
        this.description = description;
        this.location = location;
        this.setCreatedAt(createdAt);
        this.createdBy = createdBy;
        this.active = active;
    }

    @PrePersist
    protected void onCreate() {
        initializeEntity();
    }
}
