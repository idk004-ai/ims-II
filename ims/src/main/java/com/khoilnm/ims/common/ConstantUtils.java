package com.khoilnm.ims.common;

import org.springframework.beans.factory.annotation.Value;

public class ConstantUtils {
    // category
    public static final String USER_ROLE = "USER_ROLE";
    public static final String USER_STATUS = "USER_STATUS";
    public static final String DEPARTMENT = "DEPARTMENT";
    public static final String USER_GENDER = "USER_GENDER";

    // category value
    public static final String ADMIN = "ADMIN";
    public static final String MANAGER = "MANAGER";
    public static final String RECRUITER = "RECRUITER";
    public static final String INTERVIEWER = "INTERVIEWER";

    // category id
    public static final int IT = 1;
    public static final int MALE = 1;
    public static final int ADMIN_ROLE = 1;

    // JWT
    public static final String JWT_ACCESS_TOKEN = "access_token";
    public static final String JWT_REFRESH_TOKEN = "refresh_token";

    // extra field
    public static final String FIRST_USERNAME = "KHOILNM2004";


    // User status
    public static final int ACTIVE = 1;
    public static final int INACTIVE = 0;
}
