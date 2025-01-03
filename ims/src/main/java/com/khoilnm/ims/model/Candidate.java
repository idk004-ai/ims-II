package com.khoilnm.ims.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "candidates")
@SuperBuilder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Candidate extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "phone_number", length = 20, nullable = true)
    private String phone_number;

    @Column(name = "dob", nullable = true)
    private LocalDate dob;

    @Column(length = 255, nullable = true)
    private String address;

    @Column(name = "gender_id", nullable = false)
    private int gender_id;

    @Column(name = "position_id", nullable = false)
    private int position_id;

    @Lob
    @Column(name = "cv", nullable = true)
    private String cv;

    @Column(name = "cv_file_name", nullable = true)
    private String cv_file_name;

    @Column(name = "yoe", nullable = true)
    private int yoe;

    @Column(name = "highest_level_id", nullable = false)
    private int highest_level_id;

    @Column(name = "skills", nullable = false, columnDefinition = "TEXT")
    private String skill;

    @Column(name = "status_id", nullable = false)
    private int status_id;

    @Column(length = 500, nullable = true)
    private String note;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "recruiter_id", referencedColumnName = "user_id", nullable = false)
    private User recruiter;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "candidate")
    private List<Schedule> schedule_list;

//    @OneToMany(mappedBy = "candidate")
//    private List<JobApplication> jobList;
}