package com.minhhn.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "common")
public class ConnectionProperties {
    private String automaticReconnect;
    private String cleanSession;
    private int connectionTimeout;
    private int keepAliveInterval;
    private int maxInFlight;
}
