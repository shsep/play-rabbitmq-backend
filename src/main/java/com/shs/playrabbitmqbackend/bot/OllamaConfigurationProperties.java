package com.shs.playrabbitmqbackend.bot;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "ollama")
public class OllamaConfigurationProperties {
    private String model;
}
