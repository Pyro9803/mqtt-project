package com.minhhn.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.minhhn.constants.MqttConstants.RECONNECT_TIME;
import static com.minhhn.constants.MqttConstants.TEMPERATURE;

@Service
public class MqttSubscribeService {

    private static final Logger log = LoggerFactory.getLogger(MqttSubscribeService.class);
    private final AtomicBoolean connected = new AtomicBoolean(false);

    private final IMqttClient mqttClient;
    private final MqttConnectOptions options;
    private final ScheduledExecutorService reconnectExecutor;

    public MqttSubscribeService(IMqttClient mqttClient, MqttConnectOptions options) {
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
                try {
                    String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
                    log.info("Received temperature on topic {}: {}", topic, payload);
                } catch (Exception e) {
                    log.error("Failed to process message on topic {}: {}", topic, e.getMessage(), e);
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
        connectAndSubscribe();
    }

    private void connectAndSubscribe() {
        if (mqttClient.isConnected() || connected.get()) {
            log.debug("MQTT client already connected, subscribing to topic");
            subscribe();
            return;
        }
        try {
            mqttClient.connect(options);
            connected.set(true);
            subscribe();
            log.info("Connected to MQTT broker and subscribed to topic {}", TEMPERATURE);
        } catch (MqttException e) {
            connected.set(false);
            log.error("MQTT connection failed: {}", e.getMessage(), e);
            scheduleReconnect();
        }
    }

    private void subscribe() {
        try {
            mqttClient.subscribe(TEMPERATURE, 1);
            log.info("Subscribed to topic {}", TEMPERATURE);
        } catch (MqttException e) {
            connected.set(false);
            log.error("Failed to subscribe to topic {}: {}", TEMPERATURE, e.getMessage(), e);
            scheduleReconnect();
        }
    }

    private void scheduleReconnect() {
        if (!connected.get() && !mqttClient.isConnected()) {
            log.info("Scheduling MQTT reconnect in {} ms", RECONNECT_TIME);
            reconnectExecutor.schedule(this::connectAndSubscribe, RECONNECT_TIME, TimeUnit.MILLISECONDS);
        } else {
            log.debug("Reconnect not needed: client is connected or already scheduled");
        }
    }

    @PreDestroy
    public void shutdownSubscriber() {
        try {
            log.info("Shutting down MQTT publisher");
            if (mqttClient.isConnected()) {
                mqttClient.disconnect();
                mqttClient.close();
            }

            log.info("Clean shut down complete.");
        } catch (MqttException e) {
            log.error("Failed to cleanly shut down the MQTT publisher.");
        }
    }
}
