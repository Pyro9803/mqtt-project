package com.minhhn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@Import({com.minhhn.config.MqttClientConfig.class})
public class PublisherApplication
{
    public static void main( String[] args )
    {
        SpringApplication.run(PublisherApplication.class, args);
    }
}
