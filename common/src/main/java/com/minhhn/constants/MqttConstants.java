package com.minhhn.constants;

public final class MqttConstants {
    private MqttConstants() {}

    public static final String BROKER_URL = "tcp://localhost:1883";
    public static final String CLIENT_ID = "client-1";
    public static final String TEMPERATURE = "sensor/temperature";
    public static final int RECONNECT_TIME = 3000;
}
