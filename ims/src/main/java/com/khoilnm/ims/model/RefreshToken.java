package com.khoilnm.ims.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@SuperBuilder
@Table(name = "refresh_tokens")
public class RefreshToken extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String email;

    @Column(nullable = false, unique = true, length = 500)
    private String token;

    private boolean isRevoke;

    @Column(nullable = true)
    private Date revokedAt;

    @Column(nullable = true, length = 500)
    private String revokeReason;

    private boolean isRememberMe;

    private Date lastUsedAt;

    @Column(nullable = false)
    private Date expiryDate;

    @Version
    private Long version;
}
