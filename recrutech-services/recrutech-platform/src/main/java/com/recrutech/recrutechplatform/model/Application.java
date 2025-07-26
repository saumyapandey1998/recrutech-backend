package com.recrutech.recrutechplatform.model;

import com.recrutech.common.entity.BaseEntity;
import com.recrutech.recrutechplatform.enums.ApplicationStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="application")
@Getter
@Setter
public class Application extends BaseEntity {

    String cvFileId;
    ApplicationStatus status = ApplicationStatus.RECEIVED;
    boolean viewedByHr;
}
