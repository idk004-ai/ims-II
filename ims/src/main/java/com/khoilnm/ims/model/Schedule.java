package com.khoilnm.ims.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "schedules")
public class Schedule extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "schedule_title", length = 100, nullable = false)
    private String title;

    @Column(name = "schedule_time", nullable = false)
    private LocalDate schedule_time;

    @Column(name = "start_time", nullable = false)
    private LocalTime start_time;

    @Column(name = "end_time", nullable = false)
    private LocalTime end_time;

    @Column(name = "schedule_location", length = 100, nullable = true)
    private String schedule_location;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "candidate_id", referencedColumnName = "candidate_id")
    private Candidate candidate;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "job_id", referencedColumnName = "job_id")
    private Job job;

    @Column(name = "meeting_id", columnDefinition = "TEXT", nullable = true
    )
    private String meetingId;

    @Column(name = "schedule_note", columnDefinition = "TEXT")
    private String schedule_note;

    @Column(name = "status_schedule_id", nullable = false)
    private int schedule_status_id;

    @Column(name = "result_schedule_id", nullable = true)
    private Integer schedule_result_id;

    @OneToMany(mappedBy = "schedule", fetch = FetchType.EAGER)
    private List<InterviewerAssignment> schedule_assignments;

    /// Offer
    @Column(name = "offer_department", nullable = false)
    private int offer_department;

    @Column(name = "status_offer_id", nullable = false)
    private int offer_status_id;

    @Column(name = "contract_type_id", nullable = false)
    private int contract_type_id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "offer_creator", referencedColumnName = "user_id", nullable = true)
    private User offer_creator;

    // private Master department; -> get by User

    @Column(name = "due_date", nullable = false)
    private LocalDate due_date;

    @Column(name = "contract_from", nullable = false)
    private LocalDate contract_from;

    @Column(name = "contract_to", nullable = false)
    private LocalDate contract_to;

    @Column(name = "basic_salary", nullable = false)
    private Double salary;

    @Column(name = "offer_note", columnDefinition = "TEXT")
    private String offer_note;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "manager_id", referencedColumnName = "user_id")
    private User approver;

}