package com.khoilnm.ims.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@AllArgsConstructor
@NoArgsConstructor
@MappedSuperclass
@Data
@SuperBuilder
@EntityListeners({AuditingEntityListener.class})
public abstract class BaseEntity {

    @CreatedDate
    @Column(nullable = false, updatable = true)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime modifiedDate;

    @CreatedBy
    @Column(nullable = false, updatable = true)
    private Integer createdBy;

    @LastModifiedBy
    @Column(nullable = false)
    private Integer modifiedBy;

    @Column(nullable = false)
    private Boolean deleteFlag = false;
}


