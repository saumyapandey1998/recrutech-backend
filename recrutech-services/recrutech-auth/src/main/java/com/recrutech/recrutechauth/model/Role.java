package com.recrutech.recrutechauth.model;

import com.recrutech.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing a role in the system.
 * This class extends BaseEntity to inherit common fields like ID and creation timestamp.
 */
@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
public class Role extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    @Column
    private String description;

    @ManyToMany(mappedBy = "roles")
    private Set<User> users = new HashSet<>();

    /**
     * Constructor with name parameter.
     *
     * @param name the name of the role
     */
    public Role(String name) {
        this.name = name;
    }

    /**
     * Constructor with name and description parameters.
     *
     * @param name the name of the role
     * @param description the description of the role
     */
    public Role(String name, String description) {
        this.name = name;
        this.description = description;
    }

    /**
     * Initializes the entity by ensuring it has an ID and creation timestamp.
     * This method should be called before persisting the entity.
     */
    @PrePersist
    public void prePersist() {
        initializeEntity();
    }

    /**
     * Updates the entity before it's updated in the database.
     * This method is called automatically by JPA before an update operation.
     */
    @PreUpdate
    public void preUpdate() {
        // No additional update logic needed at this time
    }
}