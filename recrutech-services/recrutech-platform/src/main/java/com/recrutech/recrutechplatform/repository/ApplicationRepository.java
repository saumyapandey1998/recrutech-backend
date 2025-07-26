package com.recrutech.recrutechplatform.repository;

import com.recrutech.recrutechplatform.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for Application entities
 */
@Repository
public interface ApplicationRepository extends JpaRepository<Application, String> {
    // Add custom query methods if needed
}