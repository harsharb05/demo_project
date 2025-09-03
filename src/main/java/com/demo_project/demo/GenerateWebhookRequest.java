package com.demo_project.demo;

public class GenerateWebhookRequest {
    private final String name;
    private final String regNo;
    private final String email;

    public GenerateWebhookRequest(String name, String regNo, String email) {
        this.name = name;
        this.regNo = regNo;
        this.email = email;
    }

    public String getName() { return name; }
    public String getRegNo() { return regNo; }
    public String getEmail() { return email; }
}
