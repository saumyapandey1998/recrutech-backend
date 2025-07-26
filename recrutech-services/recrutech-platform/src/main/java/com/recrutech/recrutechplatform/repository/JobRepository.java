package com.recrutech.recrutechplatform.repository;

import com.recrutech.recrutechplatform.model.Job;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobRepository extends JpaRepository<Job, String> {
}
