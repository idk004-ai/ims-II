package com.khoilnm.ims.configuration;

import com.khoilnm.ims.model.Master;
import com.khoilnm.ims.service.MasterService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InitConfig {
    @Bean
    public CommandLineRunner commandLineRunner(MasterService masterService) {
        return (args) -> {
            addMasterIfNotExists(masterService, "USER_ROLE", "ADMIN");
            addMasterIfNotExists(masterService, "USER_ROLE", "MANAGER");
            addMasterIfNotExists(masterService, "USER_ROLE", "RECRUITER");
            addMasterIfNotExists(masterService, "USER_ROLE", "INTERVIEWER");

            addMasterIfNotExists(masterService, "USER_GENDER", "Male");
            addMasterIfNotExists(masterService, "USER_GENDER", "Female");
            addMasterIfNotExists(masterService, "USER_GENDER", "Other");

            addMasterIfNotExists(masterService, "USER_STATUS", "ACTIVE");
            addMasterIfNotExists(masterService, "USER_STATUS", "INACTIVE");

            addMasterIfNotExists(masterService, "DEPARTMENT", "IT");
            addMasterIfNotExists(masterService, "DEPARTMENT", "HR");
            addMasterIfNotExists(masterService, "DEPARTMENT", "Finance");
            addMasterIfNotExists(masterService, "DEPARTMENT", "Communication");
            addMasterIfNotExists(masterService, "DEPARTMENT", "Marketing");
            addMasterIfNotExists(masterService, "DEPARTMENT", "Accounting");

            addMasterIfNotExists(masterService, "SKILLS", "Java");
            addMasterIfNotExists(masterService, "SKILLS", "Nodejs");
            addMasterIfNotExists(masterService, "SKILLS", ".net");
            addMasterIfNotExists(masterService, "SKILLS", "C++");
            addMasterIfNotExists(masterService, "SKILLS", "Business Analyst");
            addMasterIfNotExists(masterService, "SKILLS", "Communication");

            addMasterIfNotExists(masterService, "HIGHEST_LEVEL", "High School");
            addMasterIfNotExists(masterService, "HIGHEST_LEVEL", "Bachelor's Degree");
            addMasterIfNotExists(masterService, "HIGHEST_LEVEL", "Master's Degree");
            addMasterIfNotExists(masterService, "HIGHEST_LEVEL", "PhD");

            addMasterIfNotExists(masterService, "SCHEDULE_STATUS", "Open");
            addMasterIfNotExists(masterService, "SCHEDULE_STATUS", "Invited");
            addMasterIfNotExists(masterService, "SCHEDULE_STATUS", "Interviewed");
            addMasterIfNotExists(masterService, "SCHEDULE_STATUS", "Closed");
            addMasterIfNotExists(masterService, "SCHEDULE_STATUS", "Cancelled");

            addMasterIfNotExists(masterService, "SCHEDULE_RESULT", "N/A");
            addMasterIfNotExists(masterService, "SCHEDULE_RESULT", "Passed");
            addMasterIfNotExists(masterService, "SCHEDULE_RESULT", "Failed");

            addMasterIfNotExists(masterService, "CANDIDATE_STATUS", "Open");
            addMasterIfNotExists(masterService, "CANDIDATE_STATUS", "Banned");
            addMasterIfNotExists(masterService, "CANDIDATE_STATUS", "Waiting for interview");
            addMasterIfNotExists(masterService, "CANDIDATE_STATUS", "Waiting for approval");
            addMasterIfNotExists(masterService, "CANDIDATE_STATUS", "Waiting for response");
            addMasterIfNotExists(masterService, "CANDIDATE_STATUS", "Passed Interview");
            addMasterIfNotExists(masterService, "CANDIDATE_STATUS", "Approved Offer");
            addMasterIfNotExists(masterService, "CANDIDATE_STATUS", "Rejected Offer");
            addMasterIfNotExists(masterService, "CANDIDATE_STATUS", "Accepted Offer");
            addMasterIfNotExists(masterService, "CANDIDATE_STATUS", "Declined Offer");
            addMasterIfNotExists(masterService, "CANDIDATE_STATUS", "Cancelled Offer");
            addMasterIfNotExists(masterService, "CANDIDATE_STATUS", "Failed Interview");
            addMasterIfNotExists(masterService, "CANDIDATE_STATUS", "Cancelled interview");

            addMasterIfNotExists(masterService, "CONTRACT_TYPE", "Trial 2 months");
            addMasterIfNotExists(masterService, "CONTRACT_TYPE", "Trainee 3 months");
            addMasterIfNotExists(masterService, "CONTRACT_TYPE", "1 year");
            addMasterIfNotExists(masterService, "CONTRACT_TYPE", "3 years");
            addMasterIfNotExists(masterService, "CONTRACT_TYPE", "Unlimited");

            addMasterIfNotExists(masterService, "OFFER_STATUS", "Waiting for approval");
            addMasterIfNotExists(masterService, "OFFER_STATUS", "Approved offer");
            addMasterIfNotExists(masterService, "OFFER_STATUS", "Rejected offer");
            addMasterIfNotExists(masterService, "OFFER_STATUS", "Waiting for response");
            addMasterIfNotExists(masterService, "OFFER_STATUS", "Accepted offer");
            addMasterIfNotExists(masterService, "OFFER_STATUS", "Declined offer");
            addMasterIfNotExists(masterService, "OFFER_STATUS", "Cancelled");

            addMasterIfNotExists(masterService, "POSITION", "Backend Developer");
            addMasterIfNotExists(masterService, "POSITION", "Business Analyst");
            addMasterIfNotExists(masterService, "POSITION", "Tester");
            addMasterIfNotExists(masterService, "POSITION", "HR");
            addMasterIfNotExists(masterService, "POSITION", "Project Manager");
            addMasterIfNotExists(masterService, "POSITION", "Not available");

            addMasterIfNotExists(masterService, "BENEFIT", "Lunch");
            addMasterIfNotExists(masterService, "BENEFIT", "25-day leave");
            addMasterIfNotExists(masterService, "BENEFIT", "Healthcare insurance");
            addMasterIfNotExists(masterService, "BENEFIT", "Hybrid working");
            addMasterIfNotExists(masterService, "BENEFIT", "Travel");

            addMasterIfNotExists(masterService, "LEVEL", "Fresher");
            addMasterIfNotExists(masterService, "LEVEL", "Junior");
            addMasterIfNotExists(masterService, "LEVEL", "Senior");
            addMasterIfNotExists(masterService, "LEVEL", "Leader");
            addMasterIfNotExists(masterService, "LEVEL", "Manager");
            addMasterIfNotExists(masterService, "LEVEL", "Vice Head");

            addMasterIfNotExists(masterService, "JOB_STATUS", "Draft");
            addMasterIfNotExists(masterService, "JOB_STATUS", "Open");
            addMasterIfNotExists(masterService, "JOB_STATUS", "Closed");
        };
    }

    private void addMasterIfNotExists(MasterService masterService, String category, String categoryValue) {
        if (masterService.findByCategoryAndValue(category, categoryValue) == null) {
            int nextCategoryId = masterService.findMaxCategoryId(category) + 1;

            Master master = Master.builder()
                    .category(category)
                    .categoryId(nextCategoryId)
                    .categoryValue(categoryValue)
                    .build();

            masterService.createCategory(master);
        }
    }
}
