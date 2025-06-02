package com.minhhn.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minhhn.dto.Temperature;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.minhhn.constants.MqttConstants.RECONNECT_TIME;
import static com.minhhn.constants.MqttConstants.TEMPERATURE;

@Service
public class MqttPublishService {

    private static final Logger log = LoggerFactory.getLogger(MqttPublishService.class);
    private final ObjectMapper mapper = new ObjectMapper();
    private final AtomicBoolean connected = new AtomicBoolean(false);
    private final ScheduledExecutorService reconnectExecutor;

    private final IMqttClient mqttClient;
    private final MqttConnectOptions options;

    public MqttPublishService(IMqttClient mqttClient, MqttConnectOptions options) {
        this.mqttClient = mqttClient;
        this.options = options;
        this.reconnectExecutor = Executors.newSingleThreadScheduledExecutor();
    }

    @PostConstruct
    public void init() {
        mqttClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                connected.set(false);
                log.warn("MQTT connection lost: {}", cause.getMessage(), cause);
                scheduleReconnect();
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
        connect();
    }

    private void connect() {
        if (mqttClient.isConnected()) {
            log.debug("MQTT client already connected, skipping connect attempt");
            connected.set(true);
            return;
        }
        try {
            mqttClient.connect(options);
            connected.set(true);
            log.info("Connected to MQTT broker");
        } catch (MqttException e) {
            log.error("MQTT connection failed: {}", e.getMessage(), e);
            connected.set(false);
            scheduleReconnect();
        }
    }

    private void scheduleReconnect () {
        if (!connected.get() && !mqttClient.isConnected()) {
            log.info("Scheduling MQTT reconnect in {} ms", RECONNECT_TIME);
            reconnectExecutor.schedule(this::connect, RECONNECT_TIME, TimeUnit.MILLISECONDS);
        } else {
            log.debug("Reconnect not needed: client is connected or already scheduled");
        }
    }

    @Scheduled(fixedRate = 5000)
    public void publish () {
        Temperature temperatureData = new Temperature(15 + Math.random() * 15);
        try {
            double formattedTemp = Math.round(temperatureData.temp() * 100.0) / 100.0;
            temperatureData = new Temperature(formattedTemp);

            String payload = mapper.writeValueAsString(temperatureData);
            log.debug("Generated temperature: {}°C", formattedTemp);

            if (!mqttClient.isConnected()) {
                log.warn("Publisher not connected. Scheduling reconnect...");
                connected.set(false);
                scheduleReconnect();
                return;
            }

            MqttMessage message = new MqttMessage(payload.getBytes());
            message.setQos(1);
            message.setRetained(true);

            mqttClient.publish(TEMPERATURE, message);
            log.info("Published temperature: {}°C", formattedTemp);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize temperature data: {}", e.getMessage());
        } catch (MqttException e) {
            connected.set(false);
            log.error("Failed to publish temperature data: {}", e.getMessage());
            scheduleReconnect();
        } catch (Exception e) {
            log.error("Unexpected error in publisher: {}", e.getMessage());
        }
    }

    @PreDestroy
    public void shutdownPublisher() {
        try {
            log.info("Shutting down MQTT publisher");
            reconnectExecutor.shutdown();
            if (mqttClient.isConnected()) {
                mqttClient.disconnect();
            }
            mqttClient.close();
            log.info("MQTT publisher shut down successfully");
        } catch (MqttException e) {
            log.error("Failed to shut down MQTT publisher: {}", e.getMessage(), e);
        } finally {
            reconnectExecutor.shutdownNow();
        }
    }
}
