package com.khoilnm.ims.model;

import java.util.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.LocalDate;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.userdetails.UserDetails;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@SuperBuilder
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
public class User extends BaseEntity implements UserDetails, Principal {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true)
    private String email;

    @Column(name = "username")
    private String _username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, name = "full_name", length = 100)
    private String fullName;

    @Column(name = "department_id", nullable = false)
    private int departmentId;

    @Column(name = "role_id", nullable = false)
    private int roleId;

    @Column(name = "status_id", nullable = false)
    private int status;

    @Column(name = "gender_id", nullable = false)
    private int gender;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = true)
    private LocalDate dob;

    @Column(nullable = true)
    private String address;

    @Column(nullable = false)
    private boolean enabled = false;

    @Column(nullable = true, columnDefinition = "TEXT")
    private String note;

    @OneToMany(mappedBy = "interviewer", fetch = FetchType.LAZY)
    private Set<InterviewerAssignment> scheduleAssignments;

    @OneToMany(mappedBy = "createdBy")
    private List<Schedule> schedules;

    @OneToMany(mappedBy = "createdBy")
    private List<Candidate> candidates;

    @OneToMany(mappedBy = "createdBy")
    private List<Schedule> offers;

    @OneToMany(mappedBy = "approver")
    private List<Schedule> approvedOffers;

    @OneToMany(mappedBy = "createdBy")
    private List<Job> jobs;

    // reset password properties
    @Column(name = "reset_password_token", length = 500, unique = true)
    private String resetPasswordToken;

    @Column(name = "reset_password_token_expiry_date")
    private Date resetPasswordTokenExpiryDate;

    private boolean usedResetPasswordToken;

    @Override
    public String getName() {
        return this.fullName;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        switch (roleId) {
            case 1:
                authorities.add(new SimpleGrantedAuthority("ADMIN"));
                break;
            case 2:
                authorities.add(new SimpleGrantedAuthority("MANAGER"));
                break;
            case 3:
                authorities.add(new SimpleGrantedAuthority("RECRUITER"));
                break;
            case 4:
                authorities.add(new SimpleGrantedAuthority("INTERVIEWER"));
                break;
        }
        return authorities;
    }

}