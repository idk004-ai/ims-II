package com.khoilnm.ims.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

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
    @Column(nullable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime modifiedDate;

    @CreatedBy
    @Column(nullable = false)
    private Integer createdBy;

    @LastModifiedBy
    @Column(nullable = false)
    private Integer modifiedBy;

    @Column(nullable = false)
    private Boolean deleteFlag = false;

    @Transient
    private boolean manuallySet = false;

    @Transient
    private Integer storedCreatedBy;

    public void setCreatedBy(Integer createdBy) {
        if (this.createdBy == null || this.createdBy == 0 || manuallySet) {
            this.createdBy = createdBy;
            this.storedCreatedBy = createdBy;
            manuallySet = true;
        }
    }

    @PrePersist
    protected void onCreate() {
        if (manuallySet) {
            this.createdBy = this.storedCreatedBy;
            manuallySet = false;
        }
        this.modifiedDate = this.createdDate != null ? this.createdDate : LocalDateTime.now();
        this.modifiedBy = this.createdBy;
    }
}


