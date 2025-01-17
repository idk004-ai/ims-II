package com.khoilnm.ims.common;

import lombok.Getter;

@Getter
public enum EmailTemplate {
    RESET_PASSWORD("reset_password");
    private final String name;

    EmailTemplate(String name) {
        this.name = name;
    }
}
