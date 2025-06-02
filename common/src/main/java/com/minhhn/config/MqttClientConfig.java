package com.minhhn.config;

import com.minhhn.properties.ConnectionProperties;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.minhhn.constants.MqttConstants.BROKER_URL;
import static com.minhhn.constants.MqttConstants.CLIENT_ID;

@Configuration
public class MqttClientConfig {

    private final ConnectionProperties connectionProperties;

    public MqttClientConfig(ConnectionProperties connectionProperties) {
        this.connectionProperties = connectionProperties;
    }

    @Bean
    public IMqttClient mqttClient() throws MqttException {
        return new MqttClient(BROKER_URL, CLIENT_ID, new MemoryPersistence());
    }

    @Bean
    public MqttConnectOptions createMqttConnectionOptions() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(connectionProperties.getAutomaticReconnect());
        options.setCleanSession(connectionProperties.getCleanSession());
        options.setConnectionTimeout(connectionProperties.getConnectionTimeout());
        options.setKeepAliveInterval(connectionProperties.getKeepAliveInterval());
        options.setMaxInflight(connectionProperties.getMaxInFlight());

        return options;
    }
}
