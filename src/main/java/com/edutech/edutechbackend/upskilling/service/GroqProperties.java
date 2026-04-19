package com.edutech.edutechbackend.upskilling.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@ConfigurationProperties(prefix = "groq")
public class GroqProperties {
    private String apiKey;
    private String baseUrl = "https://api.groq.com/openai";
    private String model = "llama-3.3-70b-versatile";
}