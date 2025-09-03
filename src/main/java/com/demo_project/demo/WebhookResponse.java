package com.demo_project.demo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WebhookResponse {
    @JsonProperty("webhook")
    private String webhookUrl;

    @JsonProperty("accessToken")
    private String accessToken;

    public String getWebhookUrl() { return webhookUrl; }
    public String getAccessToken() { return accessToken; }
}
