package com.khoilnm.ims.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@SuperBuilder
public class Job extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "job_title", nullable = false, length = 100)
    private String title;

    @Column(name = "job_description", nullable = true, columnDefinition = "TEXT")
    private String description;

    @Column(name = "salary_from", nullable = false)
    private Double salary_from;

    @Column(name = "salary_to", nullable = false)
    private Double salary_to;

    @Column(name = "start_date", nullable = false)
    private LocalDate start_date;

    @Column(name = "end_date", nullable = false)
    private LocalDate end_date;

    @Column(name = "level", nullable = false, columnDefinition = "TEXT")
    private String level;

    @Column(name = "benefits", nullable = false, columnDefinition = "TEXT")
    private String benefits;

    @Column(name = "skills", nullable = false, columnDefinition = "TEXT")
    private String skills;

    @Column(name = "working_address", nullable = false, columnDefinition = "TEXT")
    private String working_address;

    @Column(name = "status_job_id", nullable = false)
    private int job_status_id;

//    @OneToMany(mappedBy = "job")
//    List<JobApplication> jobApplications;

}
